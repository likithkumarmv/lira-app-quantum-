package com.lira.field.ui.eta

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lira.field.ui.components.LiraIcons
import com.lira.field.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateEtaScreen(
    viewModel: UpdateEtaViewModel,
    onBack: () -> Unit
) {
    val selectedMinutes by viewModel.selectedMinutesOffset.collectAsState()
    val customMinutes by viewModel.customMinutes.collectAsState()
    val comment by viewModel.comment.collectAsState()
    val selectedReason by viewModel.selectedReason.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val resultMessage by viewModel.resultMessage.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val recoveryTriggered by viewModel.recoveryTriggered.collectAsState()
    val isPlanValid by viewModel.isPlanValid.collectAsState()

    val colors = LiraTheme.colors

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(resultMessage) {
        resultMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "REPORT ESTIMATED ARRIVAL (ETA)",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = colors.background
    ) { padding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Notice: Driver self-report disclaimer (PRD Screen D)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = LiraIcons.Info,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "DRIVER SELF-REPORTED FACT — NOT AUTHORITATIVE GPS",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Authenticated truck: ${viewModel.truckId}. This input is explicitly categorized as manual driver reporting, separate from verified hardware GPS telemetry.",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Post-submission recovery trigger alert
            if (recoveryTriggered) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.errorSurface)
                        .border(1.dp, colors.errorBorder, RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = LiraIcons.AlertTriangle, contentDescription = null, tint = colors.error, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CENTRAL RECOVERY TRIGGERED",
                                color = colors.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "The reported delay breaches your appointment cutoff window. Central LIRA orchestrator has queued re-optimization (Wave 2) to preserve gate slot priority.",
                            color = colors.textPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Quick Offset & Custom Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PROJECTED ARRIVAL DELAY",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 30, 45, 60).forEach { mins ->
                            val isSelected = selectedMinutes == mins && customMinutes.isEmpty()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.primary else colors.surfaceElevated)
                                    .border(1.dp, if (isSelected) colors.primary else colors.border, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setOffset(mins) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$mins min",
                                    color = if (isSelected) colors.onPrimary else colors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Custom minutes input field
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { viewModel.setCustomMinutes(it) },
                        label = { Text("Or Enter Custom Delay (Minutes)", color = colors.textSecondary) },
                        placeholder = { Text("e.g. 90", color = colors.textTertiary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceElevated)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Calculated Target ETA:", color = colors.textSecondary, fontSize = 12.sp)
                            Text(
                                text = viewModel.calculateEtaIso().take(19).replace("T", " "),
                                color = colors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Reason Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PRIMARY DELAY REASON",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    listOf(
                        "TRAFFIC" to "Heavy Traffic / Highway Slowdown",
                        "WEATHER" to "Heavy Rain / Visibility Loss",
                        "BREAKDOWN" to "Mechanical Issue / Tire Problem",
                        "PORT_QUEUE" to "Terminal Gate Congestion",
                        "OTHER" to "Customs Inspection Hold / Document Check"
                    ).forEach { (code, desc) ->
                        val isSelected = selectedReason == code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) colors.surfaceElevated else colors.surface)
                                .clickable { viewModel.setReason(code) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setReason(code) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = colors.primary,
                                    unselectedColor = colors.textSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = code,
                                    color = if (isSelected) colors.primary else colors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = desc, color = colors.textSecondary, fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            // Optional Driver Comment Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ADDITIONAL DRIVER NOTES (OPTIONAL)",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { viewModel.setComment(it) },
                        placeholder = { Text("e.g. Stuck at toll plaza kilometer 42", color = colors.textTertiary) },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                }
            }

            // Submit Button
            Button(
                onClick = { viewModel.submitEta() },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colors.onPrimary, strokeWidth = 3.dp)
                } else {
                    Text(
                        text = "SUBMIT ETA UPDATE TO LIRA",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
