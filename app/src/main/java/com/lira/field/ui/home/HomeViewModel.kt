package com.lira.field.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lira.field.core.model.*
import com.lira.field.core.network.LiraNetworkResult
import com.lira.field.core.network.LiraSseClient
import com.lira.field.core.network.NetworkClient
import com.lira.field.core.session.SessionRepository
import com.lira.field.data.LiraRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.lira.field.core.util.PnrAlertTracker
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeViewModel(
    val repository: LiraRepository,
    val sessionRepo: SessionRepository
) : ViewModel() {

    private val _assignments = MutableStateFlow<List<Assignment>>(emptyList())
    val assignments: StateFlow<List<Assignment>> = _assignments.asStateFlow()

    private val _v13Assignments = MutableStateFlow<List<AssignmentV13>>(emptyList())
    val v13Assignments: StateFlow<List<AssignmentV13>> = _v13Assignments.asStateFlow()

    private val _bayBoard = MutableStateFlow<BayBoard?>(null)
    val bayBoard: StateFlow<BayBoard?> = _bayBoard.asStateFlow()

    private val _interchanges = MutableStateFlow<List<Interchange>>(emptyList())
    val interchanges: StateFlow<List<Interchange>> = _interchanges.asStateFlow()

    private val _calls = MutableStateFlow<List<CallV13>>(emptyList())
    val calls: StateFlow<List<CallV13>> = _calls.asStateFlow()

    private val _activeWhySheet = MutableStateFlow<WhySheet?>(null)
    val activeWhySheet: StateFlow<WhySheet?> = _activeWhySheet.asStateFlow()

    private val _takeoverAssignment = MutableStateFlow<AssignmentV13?>(null)
    val takeoverAssignment: StateFlow<AssignmentV13?> = _takeoverAssignment.asStateFlow()

    val pnrAlertTracker = PnrAlertTracker()
    private val _pnrAlertEvent = MutableSharedFlow<PnrStatus>(extraBufferCapacity = 1)
    val pnrAlertEvent: SharedFlow<PnrStatus> = _pnrAlertEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSubmittingAck = MutableStateFlow(false)
    val isSubmittingAck: StateFlow<Boolean> = _isSubmittingAck.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val session = sessionRepo.session
    val mockMode = sessionRepo.mockModeEnabled
    val useLabFixture = sessionRepo.useLabFixture
    val userLanguage = sessionRepo.language
    val lastSyncTime = repository.lastSyncTime
    val themeMode = sessionRepo.themeMode
    val wave2Alert = repository.wave2Alert

    var sseClient: LiraSseClient? = null
        private set

    fun toggleTheme() {
        val current = sessionRepo.themeMode.value
        val next = if (current == "LIGHT") "DARK" else "LIGHT"
        sessionRepo.setThemeMode(next)
    }

    private var pollingJob: Job? = null
    private var bayBoardJob: Job? = null

    init {
        refreshAssignments()
        startPollingEvents()
        startBayBoardPolling()
        initSseClient()
    }

    private fun initSseClient() {
        try {
            val okClient = NetworkClient.createOkHttpClient({ sessionRepo.getToken() })
            sseClient = LiraSseClient(
                okHttpClient = okClient,
                getBaseUrl = { sessionRepo.baseUrl.value },
                coroutineScope = viewModelScope
            ).apply {
                onTriggerRefetch = {
                    refreshAssignments()
                }
                start()
            }

            viewModelScope.launch {
                sseClient?.eventsFlow?.collect { sseEvent ->
                    // On ANY event: immediately refetch assignments and why sheet
                    refreshAssignments()

                    if (sseEvent.type == "plan_superseded") {
                        val oldVer = sseEvent.planVersion ?: 1
                        val newVer = oldVer + 1
                        try {
                            com.lira.field.core.util.NotificationHelper.notifyPlanSuperseded(
                                context = com.lira.field.LiraApp.instance.applicationContext,
                                oldVersion = oldVer,
                                newVersion = newVer
                            )
                        } catch (_: Exception) {}
                    }

                    if (shouldTriggerTakeover(sseEvent.type)) {
                        val target = _v13Assignments.value.firstOrNull {
                            it.assignmentId == sseEvent.assignmentId
                        } ?: _v13Assignments.value.firstOrNull { it.assignmentKind == com.lira.field.core.model.AssignmentKind.ACTIVE }
                            ?: _v13Assignments.value.firstOrNull()
                        if (target != null) {
                            _takeoverAssignment.value = target
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    companion object {
        fun shouldTriggerTakeover(eventType: String): Boolean {
            return eventType in listOf("live_incident", "plan_superseded", "branch_activated")
        }
    }

    fun dismissTakeover() {
        _takeoverAssignment.value = null
    }

    fun triggerTakeover(assignment: AssignmentV13) {
        _takeoverAssignment.value = assignment
    }

    fun postReceipt(assignmentId: String, planVersion: Int) {
        viewModelScope.launch {
            repository.postReceipt(assignmentId, planVersion)
        }
    }

    fun refreshAssignments() {
        viewModelScope.launch {
            _isLoading.value = true
            val truckId = session.value?.truckId ?: ""
            val items = repository.getAssignments(truckId)
            _assignments.value = items

            // V13 sync
            val v13Items = repository.getAssignmentsV13(truckId)
            _v13Assignments.value = v13Items

            val active = v13Items.firstOrNull { it.assignmentKind == AssignmentKind.ACTIVE && it.status == AssignmentStatusV13.APPROVED_ACTIVE }
                ?: v13Items.firstOrNull { it.status == AssignmentStatusV13.APPROVED_ACTIVE }
            if (active != null) {
                if (pnrAlertTracker.shouldTriggerImminentAlert(active.pnrStatus)) {
                    _pnrAlertEvent.tryEmit(active.pnrStatus ?: PnrStatus.PNR_IMMINENT)
                }
                if (_activeWhySheet.value == null) {
                    val whyRes = repository.getWhySheet(active.assignmentId)
                    if (whyRes is LiraNetworkResult.Success) {
                        _activeWhySheet.value = whyRes.data
                    }
                }
            }

            // Refresh bay board, interchanges, calls
            when (val bbRes = repository.getBayBoard()) {
                is LiraNetworkResult.Success -> _bayBoard.value = bbRes.data
                else -> {}
            }

            when (val ixRes = repository.getInterchanges()) {
                is LiraNetworkResult.Success -> _interchanges.value = ixRes.data
                else -> {}
            }

            when (val callRes = repository.getCalls()) {
                is LiraNetworkResult.Success -> _calls.value = callRes.data
                else -> {}
            }

            _isLoading.value = false
        }
    }

    private fun startBayBoardPolling() {
        bayBoardJob?.cancel()
        bayBoardJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000) // Poll every 30s per v13 spec §4
                try {
                    when (val res = repository.getBayBoard()) {
                        is LiraNetworkResult.Success -> _bayBoard.value = res.data
                        else -> {}
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun openWhySheet(assignmentId: String) {
        viewModelScope.launch {
            when (val res = repository.getWhySheet(assignmentId)) {
                is LiraNetworkResult.Success -> _activeWhySheet.value = res.data
                is LiraNetworkResult.Error -> {
                    _activeWhySheet.value = WhySheet(assignmentId = assignmentId, insufficientEvidence = true)
                }
                else -> {}
            }
        }
    }

    fun closeWhySheet() {
        _activeWhySheet.value = null
    }

    fun confirmVoiceReport(report: VoiceReport) {
        viewModelScope.launch {
            val req = VoiceReportConfirmRequest(
                idempotencyKey = java.util.UUID.randomUUID().toString(),
                accepted = true,
                correctedReasonCode = report.interpretation?.reasonCode,
                correctedEtaMinutes = report.interpretation?.etaMinutes
            )
            repository.confirmVoiceReport(report.reportId, req)
            _message.value = "Voice report confirmed and sent to dispatch (T4)."
            refreshAssignments()
        }
    }

    private fun startPollingEvents() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(5000) // Poll every 5 seconds per spec
                try {
                    val events = repository.pollRecentEvents()
                    val shouldRefresh = events.any {
                        it.type.startsWith("PLAN_") ||
                        it.type.startsWith("PHYSICAL_") ||
                        it.type.startsWith("RECOVERY_") ||
                        it.type.startsWith("NEW_PLAN_")
                    }
                    if (shouldRefresh) {
                        val truckId = session.value?.truckId ?: ""
                        _assignments.value = repository.getAssignments(truckId)
                    }
                } catch (_: Exception) {
                    // Silent polling retry
                }
            }
        }
    }

    fun submitAck(assignmentId: String, planVersion: Int, action: String = "ACK") {
        acknowledge(assignmentId = assignmentId, planVersion = planVersion)
        postReceipt(assignmentId, planVersion)
    }

    fun acknowledge(assignmentId: String, notes: String? = null, planVersion: Int? = null) {
        if (_isSubmittingAck.value) return // Prevent double tap
        viewModelScope.launch {
            _isSubmittingAck.value = true
            when (val res = repository.acknowledgeAssignment(assignmentId, "ACKNOWLEDGE", null, notes, planVersion)) {
                is LiraNetworkResult.Success -> {
                    _message.value = "Assignment Acknowledged. Slot confirmed."
                    refreshAssignments()
                }
                is LiraNetworkResult.OfflineQueued -> {
                    _message.value = res.message
                    refreshAssignments()
                }
                is LiraNetworkResult.Error -> {
                    if (res.code == "409" || (res.message.contains("STALE_PLAN", ignoreCase = true))) {
                        _message.value = "Plan superseded (409 STALE_PLAN). Re-fetching latest dispatch..."
                        // Lock old plan
                        _v13Assignments.value = _v13Assignments.value.map {
                            if (it.assignmentId == assignmentId) it.copy(status = AssignmentStatusV13.SUPERSEDED) else it
                        }
                        refreshAssignments()
                    } else {
                        _message.value = "Failed: ${res.message}"
                    }
                }
            }
            _isSubmittingAck.value = false
        }
    }

    fun cannotComply(assignmentId: String, reason: String, notes: String? = null, planVersion: Int? = null) {
        if (_isSubmittingAck.value) return
        viewModelScope.launch {
            _isSubmittingAck.value = true
            when (val res = repository.acknowledgeAssignment(assignmentId, "CANNOT_COMPLY", reason, notes, planVersion)) {
                is LiraNetworkResult.Success -> {
                    _message.value = "Status updated: CANNOT COMPLY ($reason). Recovery triggered."
                    refreshAssignments()
                }
                is LiraNetworkResult.OfflineQueued -> {
                    _message.value = res.message
                    refreshAssignments()
                }
                is LiraNetworkResult.Error -> {
                    if (res.code == "409" || (res.message.contains("STALE_PLAN", ignoreCase = true))) {
                        _message.value = "Plan superseded (409 STALE_PLAN). Re-fetching latest dispatch..."
                        _v13Assignments.value = _v13Assignments.value.map {
                            if (it.assignmentId == assignmentId) it.copy(status = AssignmentStatusV13.SUPERSEDED) else it
                        }
                        refreshAssignments()
                    } else {
                        _message.value = "Failed: ${res.message}"
                    }
                }
            }
            _isSubmittingAck.value = false
        }
    }

    fun clearWaveAlert() {
        repository.clearWaveAlert()
    }

    fun respondToInvestigationTask(assignmentId: String, taskId: String, answer: String, notes: String? = null) {
        viewModelScope.launch {
            repository.respondToInvestigationTask(assignmentId, taskId, answer, notes)
            _message.value = "Investigation response recorded: $answer"
            refreshAssignments()
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        bayBoardJob?.cancel()
        sseClient?.stop()
    }
}
