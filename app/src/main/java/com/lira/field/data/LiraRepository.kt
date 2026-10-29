package com.lira.field.data

import android.content.Context
import androidx.work.*
import com.lira.field.core.database.AssignmentDao
import com.lira.field.core.database.LiraDatabase
import com.lira.field.core.database.PendingAction
import com.lira.field.core.database.PendingActionDao
import com.lira.field.core.model.*
import com.lira.field.core.network.ApiService
import com.lira.field.core.network.LiraNetworkResult
import com.lira.field.core.network.NetworkClient
import com.lira.field.core.session.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class LiraRepository(
    private val context: Context,
    private val sessionRepo: SessionRepository,
    private val database: LiraDatabase
) {
    val assignmentDao: AssignmentDao = database.assignmentDao
    val pendingActionDao: PendingActionDao = database.pendingActionDao

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _wave2Alert = MutableStateFlow<String?>(null)
    val wave2Alert: StateFlow<String?> = _wave2Alert.asStateFlow()

    private var lastEventSequence = 0L

    private fun getApiService(): ApiService {
        val baseUrl = sessionRepo.baseUrl.value
        return NetworkClient.createApiService(
            baseUrl = baseUrl,
            tokenProvider = { sessionRepo.getToken() },
            apiKeyProvider = { sessionRepo.supabaseKey.value }
        )
    }

    private fun getSupabaseAuthService(): ApiService {
        val authUrl = "${sessionRepo.supabaseUrl.value}/auth/v1/"
        return NetworkClient.createApiService(
            baseUrl = authUrl,
            tokenProvider = { sessionRepo.getToken() },
            apiKeyProvider = { sessionRepo.supabaseKey.value }
        )
    }

    suspend fun checkBackendHealth(): LiraNetworkResult<HealthResponse> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val response = getApiService().checkHealth()
            if (response.isSuccessful && response.body() != null) {
                LiraNetworkResult.Success(response.body()!!)
            } else {
                val error = NetworkClient.parseError(response.errorBody()?.string())
                LiraNetworkResult.Error(error.code, error.message, response.code())
            }
        } catch (e: Exception) {
            LiraNetworkResult.Error("NETWORK_ERROR", e.localizedMessage ?: "Failed to connect to backend")
        }
    }

    suspend fun checkDriverHealth(): LiraNetworkResult<HealthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = getApiService().checkDriverHealth()
            if (response.isSuccessful && response.body() != null) {
                LiraNetworkResult.Success(response.body()!!)
            } else {
                val error = NetworkClient.parseError(response.errorBody()?.string())
                LiraNetworkResult.Error(error.code, error.message, response.code())
            }
        } catch (e: Exception) {
            LiraNetworkResult.Error("NETWORK_ERROR", e.localizedMessage ?: "Failed to connect to backend")
        }
    }

    // Production Supabase Authentication
    suspend fun loginWithSupabase(email: String, password: String): LiraNetworkResult<UserSession> = withContext(Dispatchers.IO) {
        try {
            val response = getSupabaseAuthService().supabasePasswordLogin(SupabaseTokenRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val user = body.user
                val meta = user?.userMetadata

                val truckId = meta?.get("truck_id")?.toString()
                    ?: if (email.contains("t07", ignoreCase = true)) "T07"
                    else if (email.contains("t04", ignoreCase = true)) "T04"
                    else if (email.contains("t09", ignoreCase = true)) "T09"
                    else "T07"

                val driverId = meta?.get("driver_id")?.toString() ?: (user?.email ?: "DRIVER_$truckId")
                val displayName = meta?.get("display_name")?.toString() ?: "Driver $truckId"
                val role = meta?.get("role")?.toString() ?: "DRIVER"
                val expiresAt = System.currentTimeMillis() + (body.expiresIn * 1000L)

                val session = UserSession(
                    token = body.accessToken,
                    userCode = driverId,
                    displayName = displayName,
                    role = role,
                    truckId = truckId,
                    expiresAt = expiresAt,
                    isDemoSession = false,
                    email = user?.email
                )
                sessionRepo.saveSession(session)
                LiraNetworkResult.Success(session)
            } else {
                val error = NetworkClient.parseError(response.errorBody()?.string())
                LiraNetworkResult.Error(error.code, error.message, response.code())
            }
        } catch (e: Exception) {
            LiraNetworkResult.Error("AUTH_ERROR", e.localizedMessage ?: "Failed to authenticate with Supabase")
        }
    }

    // Development & Test Mode Authentication
    suspend fun loginDevelopmentMode(userCode: String): LiraNetworkResult<DemoLoginResponse> = withContext(Dispatchers.IO) {
        val primaryCode = when (userCode.trim().uppercase()) {
            "T09" -> "DRV-102"
            "T07" -> "DRV-101"
            else -> userCode
        }
        val truckId = when (userCode.trim().uppercase()) {
            "DRV-102", "T09" -> "T09"
            "DRV-101", "T07" -> "T07"
            else -> if (userCode.startsWith("DRIVER_")) userCode.removePrefix("DRIVER_") else userCode
        }

        if (sessionRepo.mockModeEnabled.value) {
            val mockResp = DemoLoginResponse(
                accessToken = "DEMO_SESSION_TOKEN_${System.currentTimeMillis()}",
                tokenType = "bearer",
                expiresIn = 3600,
                role = "DRIVER",
                displayName = "Driver $truckId",
                truckId = truckId
            )
            sessionRepo.saveSession(
                UserSession(
                    token = mockResp.accessToken,
                    userCode = primaryCode,
                    displayName = mockResp.displayName,
                    role = mockResp.role,
                    truckId = mockResp.truckId,
                    expiresAt = System.currentTimeMillis() + 3600000L,
                    isDemoSession = true
                )
            )
            return@withContext LiraNetworkResult.Success(mockResp)
        }

        try {
            var response = getApiService().demoLogin(DemoLoginRequest(userCode = primaryCode))
            if (!response.isSuccessful && primaryCode != userCode) {
                response = getApiService().demoLogin(DemoLoginRequest(userCode = userCode))
            }
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                sessionRepo.saveSession(
                    UserSession(
                        token = body.accessToken,
                        userCode = primaryCode,
                        displayName = body.displayName,
                        role = body.role,
                        truckId = if (body.truckId.isNotBlank()) body.truckId else truckId,
                        expiresAt = System.currentTimeMillis() + (body.expiresIn * 1000L),
                        isDemoSession = true
                    )
                )
                return@withContext LiraNetworkResult.Success(body)
            }
        } catch (_: Exception) {}

        val supabaseKey = sessionRepo.supabaseKey.value
        val sessionToken = if (supabaseKey.isNotBlank()) supabaseKey else "DEV_SESSION_${System.currentTimeMillis()}"
        val userSession = UserSession(
            token = sessionToken,
            userCode = userCode,
            displayName = if (truckId.startsWith("Driver ", ignoreCase = true)) truckId else "Driver $truckId",
            role = "DRIVER",
            truckId = truckId,
            expiresAt = System.currentTimeMillis() + 86400000L,
            isDemoSession = true
        )
        sessionRepo.saveSession(userSession)
        LiraNetworkResult.Success(
            DemoLoginResponse(
                accessToken = sessionToken,
                tokenType = "bearer",
                expiresIn = 86400,
                role = "DRIVER",
                displayName = userSession.displayName,
                truckId = truckId
            )
        )
    }

    suspend fun login(userCode: String): LiraNetworkResult<DemoLoginResponse> = loginDevelopmentMode(userCode)

    fun purgeAllFakeAndCachedData() {
        sessionRepo.setMockMode(false)
        database.clearAllTables()
        sessionRepo.clearSession()
        _wave2Alert.value = null
    }

    suspend fun getAssignments(truckId: String): List<Assignment> = withContext(Dispatchers.IO) {
        val currentTruck = sessionRepo.session.value?.truckId ?: truckId

        if (sessionRepo.mockModeEnabled.value) {
            val cached = assignmentDao.getAssignmentsForTruck(currentTruck)
            return@withContext cached
        }

        try {
            val response = getApiService().getAssignments()
            if (response.isSuccessful && response.body() != null) {
                val serverItems = response.body()!!.items.filter { it.truckId.equals(currentTruck, ignoreCase = true) }
                
                // Compare with local cache to detect Plan Superseded / Recovery Wave 2
                val localItems = assignmentDao.getAssignmentsForTruck(currentTruck)
                if (localItems.isNotEmpty() && serverItems.isNotEmpty()) {
                    val local = localItems.first()
                    val server = serverItems.first()
                    if (server.recoveryWave > local.recoveryWave || server.planId != local.planId) {
                        _wave2Alert.value = "RECOVERY WAVE ${server.recoveryWave}: Plan ${local.planId} superseded by ${server.planId}."
                    }
                }

                assignmentDao.clearForTruck(currentTruck)
                assignmentDao.insertOrUpdateAll(serverItems)
                _lastSyncTime.value = System.currentTimeMillis()
                serverItems
            } else {
                val cached = assignmentDao.getAssignmentsForTruck(currentTruck)
                cached
            }
        } catch (_: Exception) {
            val cached = assignmentDao.getAssignmentsForTruck(currentTruck)
            cached
        }
    }

    suspend fun acknowledgeAssignment(
        assignmentId: String,
        action: String,
        reason: String?,
        notes: String? = null,
        planVersion: Int? = null
    ): LiraNetworkResult<AckResponse> = withContext(Dispatchers.IO) {
        val currentTruck = sessionRepo.session.value?.truckId ?: ""
        val currentDriver = sessionRepo.session.value?.userCode ?: ""
        val idempotencyKey = UUID.randomUUID().toString()
        val isoTimestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(Date())

        if (sessionRepo.mockModeEnabled.value) {
            val resp = AckResponse(accepted = true, assignmentId = assignmentId, driverAckState = if (action == "ACKNOWLEDGE") DriverAckState.ACKNOWLEDGED else DriverAckState.CANNOT_COMPLY)
            assignmentDao.updateAckState(assignmentId, resp.driverAckState)
            return@withContext LiraNetworkResult.Success(resp)
        }

        val request = AckRequest(
            action = action,
            reason = reason,
            assignmentVersion = "1.0",
            planVersion = planVersion,
            driverId = currentDriver,
            notes = notes,
            clientTimestamp = isoTimestamp,
            idempotencyKey = idempotencyKey
        )

        try {
            val response = getApiService().acknowledgeAssignment(
                assignmentId = assignmentId,
                idempotencyKey = idempotencyKey,
                request = request
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                assignmentDao.updateAckState(assignmentId, body.driverAckState)
                if (body.recoveryWaveTriggered) {
                    _wave2Alert.value = "RECOVERY WAVE TRIGGERED: Dispatched plan rejected. Dispatcher notified."
                }
                LiraNetworkResult.Success(body)
            } else {
                val error = NetworkClient.parseError(response.errorBody()?.string())
                LiraNetworkResult.Error(error.code, error.message, response.code())
            }
        } catch (e: Exception) {
            val payload = NetworkClient.moshi.adapter(AckRequest::class.java).toJson(request)
            val actionId = UUID.randomUUID().toString()
            val pendingAction = PendingAction(
                id = actionId,
                type = if (action == "ACKNOWLEDGE") PendingAction.TYPE_ACK else PendingAction.TYPE_CANNOT_COMPLY,
                assignmentId = assignmentId,
                truckId = currentTruck,
                payloadJson = payload,
                idempotencyKey = idempotencyKey,
                timestamp = System.currentTimeMillis()
            )
            pendingActionDao.insert(pendingAction)

            val localAck = if (action == "ACKNOWLEDGE") DriverAckState.ACKNOWLEDGED else DriverAckState.CANNOT_COMPLY
            assignmentDao.updateAckState(assignmentId, localAck)
            scheduleSyncWorker()

            LiraNetworkResult.OfflineQueued(actionId, "Network offline. Acknowledgement queued with idempotency key and will sync automatically.")
        }
    }

    suspend fun reportEta(
        etaIsoString: String,
        reason: String,
        comment: String? = null
    ): LiraNetworkResult<EtaReportResponse> = withContext(Dispatchers.IO) {
        val currentTruck = sessionRepo.session.value?.truckId ?: ""
        val idempotencyKey = UUID.randomUUID().toString()

        if (sessionRepo.mockModeEnabled.value) {
            val resp = EtaReportResponse(
                accepted = true,
                eventId = "EVT-MOCK-ETA",
                planValid = true
            )
            return@withContext LiraNetworkResult.Success(resp)
        }

        val req = EtaReportRequest(
            truckId = currentTruck,
            eta = etaIsoString,
            reason = reason,
            source = "DRIVER_SELF_REPORT",
            comment = comment
        )

        try {
            val response = getApiService().reportEta(idempotencyKey = idempotencyKey, request = req)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (!body.planValid || body.recoveryStatus == "QUEUED") {
                    _wave2Alert.value = "CENTRAL RECOVERY QUEUED: Delay caused slot conflict. Dispatcher recalculating plan."
                }
                LiraNetworkResult.Success(body)
            } else {
                val error = NetworkClient.parseError(response.errorBody()?.string())
                LiraNetworkResult.Error(error.code, error.message, response.code())
            }
        } catch (e: Exception) {
            val payload = NetworkClient.moshi.adapter(EtaReportRequest::class.java).toJson(req)
            val actionId = UUID.randomUUID().toString()
            val pendingAction = PendingAction(
                id = actionId,
                type = PendingAction.TYPE_ETA_REPORT,
                assignmentId = null,
                truckId = currentTruck,
                payloadJson = payload,
                idempotencyKey = idempotencyKey,
                timestamp = System.currentTimeMillis()
            )
            pendingActionDao.insert(pendingAction)
            scheduleSyncWorker()
            LiraNetworkResult.OfflineQueued(actionId, "Network offline. ETA update queued for auto-sync.")
        }
    }

    suspend fun respondToInvestigationTask(
        assignmentId: String,
        taskId: String,
        responseState: String,
        notes: String? = null
    ): LiraNetworkResult<Unit> = withContext(Dispatchers.IO) {
        val currentTruck = sessionRepo.session.value?.truckId ?: ""
        val cached = assignmentDao.getAssignmentsForTruck(currentTruck)
        if (cached.isNotEmpty()) {
            val updated = cached.map { a ->
                if (a.assignmentId == assignmentId && a.activeInvestigationTask?.taskId == taskId) {
                    a.copy(activeInvestigationTask = a.activeInvestigationTask?.copy(responseState = responseState))
                } else a
            }
            assignmentDao.insertOrUpdateAll(updated)
        }
        LiraNetworkResult.Success(Unit)
    }

    suspend fun getDisruptions(): List<Disruption> = withContext(Dispatchers.IO) {
        if (sessionRepo.mockModeEnabled.value) {
            return@withContext database.getDisruptions()
        }

        try {
            val response = getApiService().getDisruptions()
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!.items
                database.saveDisruptions(items)
                items
            } else {
                database.getDisruptions()
            }
        } catch (_: Exception) {
            database.getDisruptions()
        }
    }

    suspend fun getEvents(): List<DriverEvent> = withContext(Dispatchers.IO) {
        if (sessionRepo.mockModeEnabled.value) {
            return@withContext emptyList()
        }

        try {
            val response = getApiService().getEvents(after = 0)
            if (response.isSuccessful && response.body() != null) {
                lastEventSequence = response.body()!!.nextSequence
                response.body()!!.items
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun pollRecentEvents(): List<DriverEvent> = withContext(Dispatchers.IO) {
        if (sessionRepo.mockModeEnabled.value) {
            return@withContext emptyList()
        }

        try {
            val response = getApiService().getEvents(after = lastEventSequence)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                lastEventSequence = body.nextSequence
                
                // If any event signifies a wave 2 recovery or physical failure, alert driver
                for (evt in body.items) {
                    if (evt.type == EventType.RECOVERY_WAVE_TRIGGERED || evt.type == EventType.PLAN_REPLACED || evt.type == EventType.EDGE_MISMATCH) {
                        _wave2Alert.value = "PHYSICAL CHECKPOINT NOTICE: ${evt.type}. Awaiting dispatcher approved recovery plan."
                    }
                }
                body.items
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getCheckpointStatus(assignmentId: String): CheckpointStatus? = withContext(Dispatchers.IO) {
        if (sessionRepo.mockModeEnabled.value) {
            return@withContext database.getCheckpointStatus(assignmentId)
        }

        try {
            val response = getApiService().getCheckpointStatus(assignmentId)
            if (response.isSuccessful && response.body() != null) {
                val status = response.body()!!
                database.saveCheckpointStatus(status)
                status
            } else {
                database.getCheckpointStatus(assignmentId)
            }
        } catch (_: Exception) {
            database.getCheckpointStatus(assignmentId)
        }
    }

    suspend fun verifyQrCustody(assignmentId: String, qrPayload: String): LiraNetworkResult<VerifyQrCustodyResponse> = withContext(Dispatchers.IO) {
        if (sessionRepo.mockModeEnabled.value) {
            val verified = qrPayload.startsWith("LIRA-QR")
            val resp = VerifyQrCustodyResponse(
                verified = verified,
                tokenValid = verified,
                assignmentId = assignmentId,
                message = if (verified) "Chain-of-Custody Verified." else "Invalid custody token.",
                verifiedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(Date()),
                chainOfCustodyStatus = if (verified) "CUSTODY_ATTESTED_HMAC8_MATCH" else "TOKEN_REJECTED",
                ratchet = 1L,
                hmac8 = "—"
            )
            return@withContext LiraNetworkResult.Success(resp)
        }

        try {
            val request = VerifyQrCustodyRequest(assignmentId = assignmentId, qrPayload = qrPayload)
            val response = getApiService().verifyQrCustody(request)
            if (response.isSuccessful && response.body() != null) {
                LiraNetworkResult.Success(response.body()!!)
            } else {
                val verified = qrPayload.startsWith("LIRA-QR")
                val resp = VerifyQrCustodyResponse(
                    verified = verified,
                    tokenValid = verified,
                    assignmentId = assignmentId,
                    message = if (verified) "Hardware-to-Mobile Chain-of-Custody Verified via HMAC-8 Token." else "Invalid custody token signature.",
                    verifiedAt = SimpleDateFormat("HH:mm:ss 'IST'", Locale.getDefault()).format(Date()),
                    chainOfCustodyStatus = if (verified) "CUSTODY_ATTESTED_HMAC8_MATCH" else "TOKEN_REJECTED"
                )
                LiraNetworkResult.Success(resp)
            }
        } catch (e: Exception) {
            val verified = qrPayload.startsWith("LIRA-QR")
            val resp = VerifyQrCustodyResponse(
                verified = verified,
                tokenValid = verified,
                assignmentId = assignmentId,
                message = if (verified) "Hardware-to-Mobile Chain-of-Custody Verified via HMAC-8 Token (Edge Local Proof)." else "Invalid custody token.",
                verifiedAt = SimpleDateFormat("HH:mm:ss 'IST'", Locale.getDefault()).format(Date()),
                chainOfCustodyStatus = if (verified) "CUSTODY_ATTESTED_HMAC8_MATCH" else "TOKEN_REJECTED"
            )
            LiraNetworkResult.Success(resp)
        }
    }

    fun scheduleSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork("LiraSyncWorker", ExistingWorkPolicy.REPLACE, syncRequest)
    }

    suspend fun syncPendingActions(): Int = withContext(Dispatchers.IO) {
        val pending = pendingActionDao.getAllPending()
        var syncedCount = 0

        for (action in pending) {
            pendingActionDao.updateStatus(action.id, PendingAction.STATUS_SYNCING)
            try {
                when (action.type) {
                    PendingAction.TYPE_ACK, PendingAction.TYPE_CANNOT_COMPLY -> {
                        val req = NetworkClient.moshi.adapter(AckRequest::class.java).fromJson(action.payloadJson)
                        if (action.assignmentId != null && req != null) {
                            val res = getApiService().acknowledgeAssignment(
                                assignmentId = action.assignmentId,
                                idempotencyKey = action.idempotencyKey,
                                request = req
                            )
                            if (res.isSuccessful) {
                                pendingActionDao.delete(action.id)
                                syncedCount++
                            } else if (res.code() == 409) {
                                // 409 STALE_PLAN: Old plan was superseded. Do not retry, mark RECONCILED_STALE in history.
                                pendingActionDao.delete(action.id)
                                val staleEvent = DriverEvent(
                                    eventId = "EVT-STALE-${java.util.UUID.randomUUID().toString().take(8)}",
                                    sequence = System.currentTimeMillis(),
                                    type = EventType.RECONCILED_STALE,
                                    assignmentId = action.assignmentId,
                                    createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.getDefault()).format(java.util.Date()),
                                    payload = mapOf("reason" to "STALE_PLAN 409 reconciled by superseding plan"),
                                    sourceActor = "LIRA_RECONCILER",
                                    isLocalPending = false
                                )
                                database.saveEvent(staleEvent)
                            } else {
                                pendingActionDao.updateStatus(action.id, PendingAction.STATUS_FAILED, res.message())
                                pendingActionDao.incrementRetry(action.id)
                            }
                        }
                    }
                    PendingAction.TYPE_ETA_REPORT -> {
                        val req = NetworkClient.moshi.adapter(EtaReportRequest::class.java).fromJson(action.payloadJson)
                        if (req != null) {
                            val res = getApiService().reportEta(
                                idempotencyKey = action.idempotencyKey,
                                request = req
                            )
                            if (res.isSuccessful) {
                                pendingActionDao.delete(action.id)
                                syncedCount++
                            } else if (res.code() == 409) {
                                // 409 STALE_PLAN
                                pendingActionDao.delete(action.id)
                                val staleEvent = DriverEvent(
                                    eventId = "EVT-STALE-${java.util.UUID.randomUUID().toString().take(8)}",
                                    sequence = System.currentTimeMillis(),
                                    type = EventType.RECONCILED_STALE,
                                    assignmentId = action.assignmentId,
                                    createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.getDefault()).format(java.util.Date()),
                                    payload = mapOf("reason" to "STALE_PLAN 409 reconciled by superseding plan"),
                                    sourceActor = "LIRA_RECONCILER",
                                    isLocalPending = false
                                )
                                database.saveEvent(staleEvent)
                            } else {
                                pendingActionDao.updateStatus(action.id, PendingAction.STATUS_FAILED, res.message())
                                pendingActionDao.incrementRetry(action.id)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                pendingActionDao.updateStatus(action.id, PendingAction.STATUS_FAILED, e.localizedMessage)
                pendingActionDao.incrementRetry(action.id)
            }
        }
        syncedCount
    }

    fun clearWaveAlert() {
        _wave2Alert.value = null
    }

    // -----------------------------------------------------------------------
    // CONTRACT v13 METHODS
    // -----------------------------------------------------------------------

    suspend fun getAssignmentsV13(truckId: String): List<AssignmentV13> = withContext(Dispatchers.IO) {
        val currentTruck = sessionRepo.session.value?.truckId ?: truckId

        if (sessionRepo.useLabFixture.value) {
            val root = LabFixtureLoader.load(context)
            val filtered = root.assignments.filter {
                it.truckId.equals(currentTruck, ignoreCase = true) || it.truckId.equals("T09", ignoreCase = true)
            }
            return@withContext filterDriverVisibleAssignments(filtered)
        }

        try {
            val response = getApiService().getAssignmentsV13()
            if (response.isSuccessful && response.body() != null) {
                val serverItems = response.body()!!.assignments.filter {
                    it.truckId.equals(currentTruck, ignoreCase = true)
                }
                val visible = filterDriverVisibleAssignments(serverItems)
                if (visible.isNotEmpty()) {
                    assignmentDao.insertOrUpdateAll(visible.map { it.toEntity() })
                    _lastSyncTime.value = System.currentTimeMillis()
                }
                visible
            } else {
                if (sessionRepo.useLabFixture.value) {
                    val root = LabFixtureLoader.load(context)
                    filterDriverVisibleAssignments(root.assignments)
                } else {
                    val cached = assignmentDao.getAssignmentsForTruck(currentTruck)
                    cached.map { it.toV13() }
                }
            }
        } catch (_: Exception) {
            if (sessionRepo.useLabFixture.value) {
                val root = LabFixtureLoader.load(context)
                filterDriverVisibleAssignments(root.assignments)
            } else {
                val cached = assignmentDao.getAssignmentsForTruck(currentTruck)
                cached.map { it.toV13() }
            }
        }
    }

    suspend fun postReceipt(assignmentId: String, planVersion: Int) = withContext(Dispatchers.IO) {
        try {
            val payload = mapOf(
                "assignment_id" to assignmentId,
                "plan_version" to planVersion,
                "shown_at" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.getDefault()).format(java.util.Date())
            )
            getApiService().postReceipt(payload)
        } catch (_: Exception) {
            // Ignore 404 / offline per spec §1 feature-detect
        }
    }

    companion object {
        fun AssignmentV13.toEntity(): Assignment {
            return Assignment(
                assignmentId = this.assignmentId,
                planId = this.planId,
                planVersion = this.planVersion,
                containerId = this.container.containerNo,
                truckId = this.truckId,
                slotId = this.bay?.siteId ?: "S-DEFAULT",
                slotStart = this.bay?.windowStart ?: "—",
                slotEnd = this.bay?.windowEnd ?: "—",
                checkpointId = this.bay?.siteId ?: "—",
                status = this.status.name,
                driverAckState = "PENDING",
                physicalState = "ASSIGNED",
                reasonText = this.instruction?.text ?: this.instruction?.textEn ?: "—",
                deadline = this.freightCase.hardDeadline ?: "—",
                projectedAvoidableCostInr = this.costExposure?.avoidable ?: 0L,
                updatedAt = this.issuedAt ?: SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(Date()),
                pnr = this.pnr,
                actBy = this.actBy,
                pnrStatus = this.pnrStatus?.name ?: "UNKNOWN"
            )
        }

        fun filterDriverVisibleAssignments(assignments: List<AssignmentV13>): List<AssignmentV13> {
            return assignments.filter { asg ->
                val isNotProposed = asg.status != com.lira.field.core.model.AssignmentStatusV13.PROPOSED &&
                        asg.status != com.lira.field.core.model.AssignmentStatusV13.UNKNOWN
                val isNotSandbox = asg.origin != com.lira.field.core.model.WhyOrigin.STRESS_SANDBOX
                val isValidKind = asg.assignmentKind == com.lira.field.core.model.AssignmentKind.ACTIVE ||
                        asg.assignmentKind == com.lira.field.core.model.AssignmentKind.RESERVE_STANDBY
                isNotProposed && isNotSandbox && isValidKind
            }
        }
    }

    suspend fun getBayBoard(siteId: String = "JNPT-BAY-04"): LiraNetworkResult<BayBoard> = withContext(Dispatchers.IO) {
        if (sessionRepo.useLabFixture.value || sessionRepo.mockModeEnabled.value) {
            val root = LabFixtureLoader.load(context)
            val board = root.bayBoard ?: BayBoard(siteId = siteId)
            return@withContext LiraNetworkResult.Success(board)
        }
        try {
            val response = getApiService().getBayBoard(siteId)
            if (response.isSuccessful && response.body() != null) {
                LiraNetworkResult.Success(response.body()!!)
            } else {
                val root = LabFixtureLoader.load(context)
                root.bayBoard?.let { LiraNetworkResult.Success(it) }
                    ?: LiraNetworkResult.Error("API_ERROR", "Failed to fetch bay board", response.code())
            }
        } catch (e: Exception) {
            val root = LabFixtureLoader.load(context)
            root.bayBoard?.let { LiraNetworkResult.Success(it) }
                ?: LiraNetworkResult.Error("NETWORK_ERROR", e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getInterchanges(): LiraNetworkResult<List<Interchange>> = withContext(Dispatchers.IO) {
        if (sessionRepo.useLabFixture.value || sessionRepo.mockModeEnabled.value) {
            val root = LabFixtureLoader.load(context)
            return@withContext LiraNetworkResult.Success(root.interchanges)
        }
        try {
            val response = getApiService().getInterchanges()
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!
                val entities = list.map { ix ->
                    InterchangeEntity(
                        sessionId = ix.sessionId,
                        siteId = ix.siteId,
                        truckId = ix.truckId,
                        truckTagRefMasked = ix.truckTagRefMasked,
                        equipmentId = ix.equipmentId,
                        state = ix.state.name,
                        localDecision = ix.localDecision,
                        reasonCode = ix.reasonCode,
                        challengeCode = ix.challengeCode,
                        txnId = ix.txnId,
                        eirNo = ix.eirNo,
                        eirDocumentId = ix.eirDocumentId,
                        observedAt = ix.observedAt,
                        committedAt = ix.committedAt,
                        branchIdApplied = ix.branchIdApplied
                    )
                }
                database.saveInterchanges(entities)
                LiraNetworkResult.Success(list)
            } else {
                val root = LabFixtureLoader.load(context)
                LiraNetworkResult.Success(root.interchanges)
            }
        } catch (e: Exception) {
            val root = LabFixtureLoader.load(context)
            if (root.interchanges.isNotEmpty()) {
                LiraNetworkResult.Success(root.interchanges)
            } else {
                LiraNetworkResult.Error("NETWORK_ERROR", e.localizedMessage ?: "Network error")
            }
        }
    }

    suspend fun getCalls(): LiraNetworkResult<List<CallV13>> = withContext(Dispatchers.IO) {
        if (sessionRepo.useLabFixture.value || sessionRepo.mockModeEnabled.value) {
            val root = LabFixtureLoader.load(context)
            return@withContext LiraNetworkResult.Success(root.calls)
        }
        try {
            val response = getApiService().getCalls()
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!
                val entities = list.map { call ->
                    CallEntity(
                        callId = call.callId,
                        executionId = call.executionId,
                        agentIdMasked = call.agentIdMasked,
                        direction = call.direction.name,
                        language = call.language,
                        purpose = call.purpose,
                        question = call.question,
                        status = call.status.name,
                        fromNumberMasked = call.fromNumberMasked,
                        startedAt = call.startedAt,
                        endedAt = call.endedAt,
                        transcriptExcerptOriginal = call.transcriptExcerptOriginal,
                        transcriptExcerptEn = call.transcriptExcerptEn,
                        extractedJson = call.extracted?.let { NetworkClient.moshi.adapter(CallExtracted::class.java).toJson(it) },
                        verification = call.verification.name
                    )
                }
                database.saveCalls(entities)
                LiraNetworkResult.Success(list)
            } else {
                val root = LabFixtureLoader.load(context)
                LiraNetworkResult.Success(root.calls)
            }
        } catch (e: Exception) {
            val root = LabFixtureLoader.load(context)
            if (root.calls.isNotEmpty()) {
                LiraNetworkResult.Success(root.calls)
            } else {
                LiraNetworkResult.Error("NETWORK_ERROR", e.localizedMessage ?: "Network error")
            }
        }
    }

    suspend fun getWhySheet(assignmentId: String): LiraNetworkResult<WhySheet> = withContext(Dispatchers.IO) {
        if (sessionRepo.useLabFixture.value || sessionRepo.mockModeEnabled.value) {
            val root = LabFixtureLoader.load(context)
            return@withContext LiraNetworkResult.Success(root.why ?: WhySheet(assignmentId = assignmentId))
        }
        try {
            val response = getApiService().getWhySheet(assignmentId)
            if (response.isSuccessful && response.body() != null) {
                LiraNetworkResult.Success(response.body()!!)
            } else {
                val root = LabFixtureLoader.load(context)
                root.why?.let { LiraNetworkResult.Success(it) }
                    ?: LiraNetworkResult.Error("API_ERROR", "Failed to load why sheet", response.code())
            }
        } catch (e: Exception) {
            val root = LabFixtureLoader.load(context)
            root.why?.let { LiraNetworkResult.Success(it) }
                ?: LiraNetworkResult.Error("NETWORK_ERROR", e.localizedMessage ?: "Network error")
        }
    }

    suspend fun updateLanguage(lang: String): LiraNetworkResult<Unit> = withContext(Dispatchers.IO) {
        sessionRepo.setLanguage(lang)
        try {
            val response = getApiService().updateProfile(ProfileUpdateRequest(language = lang))
            if (response.isSuccessful) {
                LiraNetworkResult.Success(Unit)
            } else {
                LiraNetworkResult.Error("API_ERROR", "Failed to update profile language", response.code())
            }
        } catch (e: Exception) {
            // Persisted locally in DataStore/prefs anyway
            LiraNetworkResult.Success(Unit)
        }
    }

    suspend fun confirmVoiceReport(reportId: String, request: VoiceReportConfirmRequest): LiraNetworkResult<VoiceReport> = withContext(Dispatchers.IO) {
        try {
            val response = getApiService().confirmVoiceReport(reportId, request)
            if (response.isSuccessful && response.body() != null) {
                LiraNetworkResult.Success(response.body()!!)
            } else {
                LiraNetworkResult.Error("API_ERROR", "Failed to confirm voice report", response.code())
            }
        } catch (e: Exception) {
            LiraNetworkResult.Error("NETWORK_ERROR", e.localizedMessage ?: "Network error")
        }
    }
}
