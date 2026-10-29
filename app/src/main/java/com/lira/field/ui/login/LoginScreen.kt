package com.lira.field.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lira.field.ui.components.LiraBrandLogo
import com.lira.field.ui.components.LiraIcons
import com.lira.field.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val authMode by viewModel.authMode.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val selectedDriver by viewModel.selectedDriver.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()

    val colors = LiraTheme.colors

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            viewModel.resetSuccess()
            onLoginSuccess()
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Theme Toggle at top right
        IconButton(
            onClick = { viewModel.toggleTheme() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (colors.isDark) LiraIcons.Sun else LiraIcons.Moon,
                contentDescription = if (colors.isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                tint = colors.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LiraBrandLogo(size = 72)

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "LIRA FIELD",
                color = colors.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Text(
                text = "Freight Exception Resolution & Field Dispatch",
                color = colors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Auth Mode Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (authMode == AuthMode.SUPABASE_AUTH) colors.primary else colors.surfaceElevated)
                        .clickable { viewModel.setAuthMode(AuthMode.SUPABASE_AUTH) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SUPABASE AUTH",
                        color = if (authMode == AuthMode.SUPABASE_AUTH) colors.onPrimary else colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (authMode == AuthMode.DEV_BYPASS) colors.primary else colors.surfaceElevated)
                        .clickable { viewModel.setAuthMode(AuthMode.DEV_BYPASS) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DEV TEST BYPASS",
                        color = if (authMode == AuthMode.DEV_BYPASS) colors.onPrimary else colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Credential Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(colors.border))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    if (authMode == AuthMode.SUPABASE_AUTH) {
                        Text(
                            text = "AUTHENTICATED SERVER CREDENTIALS",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Authenticates against Supabase Auth. Server derives driver claims and isolates assigned truck.",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { viewModel.setEmail(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Driver Email / Account") },
                            leadingIcon = {
                                Icon(LiraIcons.Email, contentDescription = null, tint = colors.primary)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { viewModel.setPassword(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Driver Password") },
                            leadingIcon = {
                                Icon(LiraIcons.Lock, contentDescription = null, tint = colors.primary)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { viewModel.performLogin() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )
                    } else {
                        // Dev Bypass Mode with prominent notice
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.warningSurface)
                                .border(1.dp, colors.warningBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(LiraIcons.AlertTriangle, contentDescription = null, tint = colors.warning, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "TESTING & EVALUATION MODE ONLY",
                                        color = colors.warning,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Non-production bypass. For live operations, switch to Supabase Auth above.",
                                        color = colors.textPrimary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "ASSIGNED DRIVER / TRUCK IDENTIFIER",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = selectedDriver,
                            onValueChange = { viewModel.selectDriver(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter Truck ID (e.g. T07)") },
                            leadingIcon = {
                                Icon(LiraIcons.Badge, contentDescription = null, tint = colors.primary)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "QUICK SELECT PRESET",
                            color = colors.textTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("T09", "T07", "T04").forEach { code ->
                                val isSelected = selectedDriver.equals(code, ignoreCase = true)
                                val label = when (code) {
                                    "T09" -> "T09\n(DRV-102)"
                                    "T07" -> "T07\n(DRV-101)"
                                    else -> code
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) colors.primaryContainer else colors.surfaceElevated)
                                        .border(1.dp, if (isSelected) colors.primary else colors.border, RoundedCornerShape(8.dp))
                                        .clickable { viewModel.selectDriver(code) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) colors.primary else colors.textPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = errorMessage!!,
                            color = colors.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.performLogin() },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = colors.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (authMode == AuthMode.SUPABASE_AUTH) "SIGN IN VIA SUPABASE" else "ENTER TERMINAL AS $selectedDriver",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
