package com.lira.field.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import com.lira.field.core.model.DriverEvent
import com.lira.field.core.model.IncidentGroup
import com.lira.field.core.model.groupByIncident
import com.lira.field.core.model.EventType
import com.lira.field.ui.components.LiraIcons
import com.lira.field.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTimelineScreen(
    viewModel: ActivityTimelineViewModel,
    onBack: () -> Unit
) {
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val colors = LiraTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RECOVERY ACTIVITY TIMELINE",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = LiraIcons.ArrowBack, contentDescription = "Back", tint = colors.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadEvents() }) {
                        Icon(imageVector = LiraIcons.Refresh, contentDescription = "Refresh", tint = colors.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
            )
        },
        containerColor = colors.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceElevated)
                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "LIRA ORCHESTRATION & RECOVERY AUDIT LOG",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "End-to-end event chain from document ingestion and QpiAI solver to field execution and ESP32 physical verification.",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }
            } else if (events.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No timeline events logged yet in current session.", color = colors.textSecondary)
                        }
                    }
                }
            } else {
                val groups = events.groupByIncident()
                items(groups) { group ->
                    IncidentGroupCard(group)
                }
            }
        }
    }
}

@Composable
private fun IncidentGroupCard(group: IncidentGroup) {
    val colors = LiraTheme.colors
    var expanded by remember { mutableStateOf(true) }

    val originChipColor = when (group.origin.uppercase()) {
        "LIVE CALL", "LIVE_CALL" -> Color(0xFF10B981)
        "COPILOT" -> Color(0xFF8B5CF6)
        "STRESS", "STRESS_PROMOTED" -> Color(0xFFEF4444)
        "STATION PLAN B", "STATION_BRANCH" -> Color(0xFFF59E0B)
        else -> Color(0xFF0284C7)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(originChipColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .border(1.dp, originChipColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = group.origin.replace("_", " ").uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = originChipColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = group.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.textPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${group.events.size} events",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) LiraIcons.Close else LiraIcons.ChevronRight,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    group.events.forEach { event ->
                        TimelineEventCard(event)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineEventCard(event: DriverEvent) {
    val colors = LiraTheme.colors

    // 14 Canonical Events per PRD Screen G
    val (color, icon) = when (event.type) {
        EventType.DOCUMENT_INGESTED -> colors.primary to LiraIcons.DocumentText
        EventType.BLOCKER_IDENTIFIED -> colors.error to LiraIcons.ReportProblem
        EventType.VERIFICATION_REQUESTED -> colors.warning to LiraIcons.PhoneCall
        EventType.RECOVERY_GENERATED -> colors.primary to LiraIcons.Psychology
        EventType.PLAN_APPROVED -> colors.success to LiraIcons.CheckCircle
        EventType.PLAN_DISPATCHED -> colors.primary to LiraIcons.Send
        EventType.DRIVER_ACKNOWLEDGED -> colors.success to LiraIcons.ThumbUp
        EventType.DRIVER_CANNOT_COMPLY -> colors.error to LiraIcons.Cancel
        EventType.ETA_UPDATED -> colors.primary to LiraIcons.Schedule
        EventType.EDGE_RFID_VERIFIED, EventType.STAGING_EVIDENCED -> colors.success to LiraIcons.Verified
        EventType.EDGE_MISMATCH, EventType.PHYSICAL_MISMATCH -> colors.error to LiraIcons.AlertCircle
        EventType.RECOVERY_WAVE_TRIGGERED, EventType.RECOVERY_STARTED -> colors.warning to LiraIcons.Autorenew
        EventType.NEW_PLAN_DISPATCHED, EventType.PLAN_REPLACED -> colors.warning to LiraIcons.SwapHoriz
        EventType.PLAN_COMPLETED -> colors.success to LiraIcons.DoneAll
        EventType.BRANCH_ACTIVATED -> colors.warning to LiraIcons.AltRoute
        EventType.PLAN_SUPERSEDED -> colors.error to LiraIcons.SwapHoriz
        EventType.CALL_COMPLETED -> colors.success to LiraIcons.PhoneCallback
        EventType.CALL_FAILED -> colors.error to LiraIcons.PhoneMissed
        EventType.VOICE_REPORT_SENT -> colors.primary to LiraIcons.Mic
        EventType.INTERCHANGE_COMMITTED -> colors.success to LiraIcons.Verified
        EventType.INTERCHANGE_HOLD -> colors.error to LiraIcons.AlertCircle
        EventType.RECONCILED_STALE -> colors.warning to LiraIcons.Autorenew
        else -> colors.textSecondary to LiraIcons.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.type,
                        color = color,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Distinguish server vs local pending events (PRD Screen G)
                        if (event.isLocalPending) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.warningSurface)
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text("LOCAL PENDING", color = colors.warning, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.successSurface)
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text("SERVER SYNCED", color = colors.success, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = "#${event.sequence}",
                            color = colors.textTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Actor: ${event.sourceActor}",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (event.assignmentId != null) {
                        Text(
                            text = "Ref: ${event.assignmentId}",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Render structured payload attributes
                if (!event.payload.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.surfaceElevated)
                            .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            event.payload.forEach { (k, v) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "$k:", color = colors.textSecondary, fontSize = 11.sp)
                                    Text(text = v?.toString() ?: "null", color = colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = event.createdAt,
                    color = colors.textTertiary,
                    fontSize = 10.sp
                )
            }
        }
    }
}
