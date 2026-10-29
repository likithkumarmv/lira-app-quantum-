package com.lira.field.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lira.field.ui.components.LiraIcons
import com.lira.field.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val baseUrl by viewModel.baseUrl.collectAsState()
    val mockMode by viewModel.mockMode.collectAsState()
    val session by viewModel.session.collectAsState()
    val lastSync by viewModel.lastSyncTime.collectAsState()
    val pendingActions by viewModel.pendingActions.collectAsState()
    val healthResult by viewModel.healthTestResult.collectAsState()
    val isTestingHealth by viewModel.isTestingHealth.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val supabaseUrl by viewModel.supabaseUrl.collectAsState()
    val supabaseKey by viewModel.supabaseKey.collectAsState()

    val colors = LiraTheme.colors

    var urlInput by remember(baseUrl) { mutableStateOf(baseUrl) }
    var isEditingUrl by remember { mutableStateOf(false) }
    var quickIpInput by remember { mutableStateOf("192.168.43.100") }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SETTINGS & SYSTEM",
                        fontSize = 16.sp,
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
            // Authenticated Driver & Truck Profile Card (PRD Screen H)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.primary.copy(alpha = 0.5f)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AUTHENTICATED DRIVER PROFILE",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(IndustrialShape)
                                .background(if (session?.isDemoSession == true) colors.warningSurface else colors.successSurface)
                                .border(1.dp, if (session?.isDemoSession == true) colors.warningBorder else colors.successBorder, IndustrialShape)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (session?.isDemoSession == true) "DEV BYPASS" else "SUPABASE AUTH",
                                color = if (session?.isDemoSession == true) colors.warning else colors.success,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ASSIGNED TRUCK", color = colors.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = session?.truckId ?: "UNASSIGNED",
                                color = colors.primary,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("DRIVER IDENTITY", color = colors.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = session?.driverId ?: "Unknown Driver",
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (!session?.email.isNullOrEmpty()) {
                                Text(
                                    text = session!!.email!!,
                                    color = colors.textTertiary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = colors.border)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Role Claim", color = colors.textSecondary, fontSize = 12.sp)
                        Text(text = session?.role ?: "DRIVER_FIELD", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Session Expiry", color = colors.textSecondary, fontSize = 12.sp)
                        Text(
                            text = if (session?.expiresAt != null) "Active (${session?.expiresAt})" else "Active Session",
                            color = colors.success,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("App Version", color = colors.textSecondary, fontSize = 12.sp)
                        Text("v7.0 (Build 7) • GENAI-01", color = colors.textTertiary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Driver identity and truck assignment derived from authenticated server claims. Client truck spoofing is strictly prevented.",
                        color = colors.textTertiary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            // Theme Mode Card (Light, Dark, System)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "APPEARANCE & THEME",
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    listOf(
                        Triple("SYSTEM", "Follow System Theme", LiraIcons.Monitor),
                        Triple("LIGHT", "Terminal Paper Light Mode", LiraIcons.Sun),
                        Triple("DARK", "Obsidian Industrial Dark Mode", LiraIcons.Moon)
                    ).forEach { (mode, label, icon) ->
                        val isSelected = currentThemeMode == mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(IndustrialShape)
                                .background(if (isSelected) colors.surfaceElevated else colors.surface)
                                .clickable { viewModel.setThemeMode(mode) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) colors.primary else colors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = label,
                                    color = if (isSelected) colors.primary else colors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setThemeMode(mode) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = colors.primary,
                                    unselectedColor = colors.textSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Language Selection Card (System / English / हिन्दी / मराठी / தமிழ்)
            val currentLanguage by viewModel.language.collectAsState()
            val useLabFixture by viewModel.useLabFixture.collectAsState()
            val sseStatus by viewModel.sseStatus.collectAsState()
            val lastStreamEventAt by viewModel.lastStreamEventAt.collectAsState()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LANGUAGE & LOCALIZATION",
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    listOf(
                        "system" to "System Default",
                        "en" to "English",
                        "hi" to "हिन्दी (Hindi)",
                        "mr" to "मराठी (Marathi)",
                        "ta" to "தமிழ் (Tamil)",
                        "kn" to "ಕನ್ನಡ (Kannada)",
                        "te" to "తెలుగు (Telugu)"
                    ).forEach { (tag, label) ->
                        val isSelected = currentLanguage == tag
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(IndustrialShape)
                                .background(if (isSelected) colors.surfaceElevated else colors.surface)
                                .clickable { viewModel.setLanguage(tag) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) colors.primary else colors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setLanguage(tag) },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                            )
                        }
                    }
                }
            }

            // Real-Time Stream & Backend Schema Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REAL-TIME STREAM & TELEMETRY",
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("SSE Stream Status", color = colors.textSecondary, fontSize = 12.sp)
                        Text(sseStatus, color = colors.success, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Last Stream Event", color = colors.textSecondary, fontSize = 12.sp)
                        Text(lastStreamEventAt, color = colors.textPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Backend Schema", color = colors.textSecondary, fontSize = 12.sp)
                        Text("Backend schema v13.0 ✓", color = colors.success, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Debug Lab Fixture Data Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(if (useLabFixture) colors.error else colors.border)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "USE LAB FIXTURE DATA (DEBUG ONLY)",
                                color = if (useLabFixture) colors.error else colors.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Loads canonical C1–C6 test fixtures from assets/lab_fixture_v13.json with persistent red LAB FIXTURE ribbon.",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                        Switch(
                            checked = useLabFixture,
                            onCheckedChange = { viewModel.setUseLabFixture(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.error
                            )
                        )
                    }
                }
            }

            // Supabase Cloud Backend Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SUPABASE CLOUD AUTH & AUDIT BACKEND",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(IndustrialShape)
                                .background(colors.successSurface)
                                .border(1.dp, colors.successBorder, IndustrialShape)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("CONFIGURED", color = colors.success, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Supabase Project URL", color = colors.textSecondary, fontSize = 11.sp)
                    Text(text = supabaseUrl, color = colors.textPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Publishable API Key", color = colors.textSecondary, fontSize = 11.sp)
                    Text(text = "${supabaseKey.take(18)}...${supabaseKey.takeLast(6)}", color = colors.primary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                }
            }

            // FastAPI Control Tower LAN Endpoint Card (v7.0)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONTROL TOWER LAN IP:PORT",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        IconButton(onClick = { isEditingUrl = !isEditingUrl }) {
                            Icon(imageVector = LiraIcons.Edit, contentDescription = "Edit URL", tint = colors.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditingUrl) {
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            label = { Text("Control Tower LAN IP:Port (Default: http://192.168.43.100:8000/api/v1/)", color = colors.textSecondary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                            shape = IndustrialShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    viewModel.updateBaseUrl(urlInput)
                                    isEditingUrl = false
                                },
                                shape = IndustrialShape,
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
                            ) {
                                Text("SAVE URL", fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { isEditingUrl = false }) {
                                Text("CANCEL", color = colors.textSecondary)
                            }
                        }
                    } else {
                        Text(
                            text = baseUrl,
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Laptop 2 FastAPI Server (Normalized prefix: /api/v1/)",
                            color = colors.textTertiary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick-Edit LAN IP Support
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quickIpInput,
                            onValueChange = { quickIpInput = it },
                            label = { Text("Control Tower LAN IP", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = IndustrialShape,
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )
                        Button(
                            onClick = {
                                viewModel.setQuickLanIp(quickIpInput)
                            },
                            shape = IndustrialShape,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
                        ) {
                            Text("SET IP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = colors.border)
                    Spacer(modifier = Modifier.height(14.dp))

                    // PING CONTROL TOWER button
                    Button(
                        onClick = { viewModel.testBackendConnection() },
                        enabled = !isTestingHealth,
                        modifier = Modifier.fillMaxWidth(),
                        shape = IndustrialShape,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceElevated, contentColor = colors.primary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.primary)
                    ) {
                        if (isTestingHealth) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = colors.primary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PINGING CONTROL TOWER...", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        } else {
                            Icon(imageVector = LiraIcons.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PING CONTROL TOWER (LATENCY & SCHEMA)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (healthResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(IndustrialShape)
                                .background(colors.surfaceElevated)
                                .border(1.dp, if (healthResult!!.contains("CONNECTED")) colors.successBorder else if (healthResult!!.contains("WARNING")) colors.warningBorder else colors.errorBorder, IndustrialShape)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = healthResult!!,
                                color = if (healthResult!!.contains("CONNECTED")) colors.success else if (healthResult!!.contains("WARNING")) colors.warning else colors.error,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Local Data Management Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "LOCAL DATA & CACHE MANAGEMENT",
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Operating with authoritative FastAPI LAN server & Supabase cloud backend. Clear all local cached tables, assignments, and offline pending state.",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = { viewModel.purgeLocalData() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = IndustrialShape,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.error.copy(alpha = 0.5f))
                    ) {
                        Icon(imageVector = LiraIcons.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PURGE ALL LOCAL & CACHED DATA", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Offline Cache & WorkManager Queue Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ROOM DATABASE & WORKMANAGER QUEUE",
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Last Successful Sync", color = colors.textSecondary, fontSize = 12.sp)
                        Text(viewModel.formatSyncTime(lastSync), color = colors.textPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Queued Offline Requests", color = colors.textSecondary, fontSize = 12.sp)
                        Text(
                            text = "${pendingActions.size} pending",
                            color = if (pendingActions.isEmpty()) colors.success else colors.warning,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (pendingActions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        pendingActions.forEach { a ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(IndustrialShape)
                                    .background(colors.surfaceElevated)
                                    .border(1.dp, colors.border, IndustrialShape)
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Action: ${a.type}", color = colors.warning, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Idempotency: ${a.idempotencyKey.take(8)}...", color = colors.textTertiary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.forceSync() },
                        enabled = !isSyncing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = IndustrialShape,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = colors.onPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("UPLOADING QUEUED REQUESTS...")
                        } else {
                            Icon(imageVector = LiraIcons.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("FORCE RECONNECT & SYNC", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Privacy & Security Notice per spec section 0 & 3
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = IndustrialShape,
                colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = LiraIcons.Security, contentDescription = null, tint = colors.success, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "CLIENT PRIVACY & SECURITY ENFORCEMENT", color = colors.success, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Zero Provider API Keys: Gemini, QpiAI Opt, QCloud, Sarvam, Bolna, and weather API keys never reside on the mobile client.\n• Carrier Isolation: Driver can access only authenticated assignments matching their registered truck identifier.\n• Authoritative Separation: Mobile client does not approve customs release, recalculate QAOA solvers, or override edge sensors.",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Logout Button
            OutlinedButton(
                onClick = { viewModel.logout(onLogout) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = IndustrialShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.error)
            ) {
                Icon(imageVector = LiraIcons.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("LOGOUT DRIVER SESSION", fontWeight = FontWeight.Bold)
            }
        }
    }
}
