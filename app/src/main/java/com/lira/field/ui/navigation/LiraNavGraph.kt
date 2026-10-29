package com.lira.field.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.lira.field.LiraApp
import com.lira.field.ui.activity.ActivityTimelineScreen
import com.lira.field.ui.activity.ActivityTimelineViewModel
import com.lira.field.ui.assignments.AssignmentDetailsScreen
import com.lira.field.ui.checkpoint.CheckpointScreen
import com.lira.field.ui.checkpoint.CheckpointViewModel
import com.lira.field.ui.components.LiraIcons
import com.lira.field.ui.disruptions.RiskAlertsScreen
import com.lira.field.ui.disruptions.RiskAlertsViewModel
import com.lira.field.ui.eta.UpdateEtaScreen
import com.lira.field.ui.eta.UpdateEtaViewModel
import com.lira.field.ui.home.HomeScreen
import com.lira.field.ui.home.HomeViewModel
import com.lira.field.ui.login.LoginScreen
import com.lira.field.ui.login.LoginViewModel
import com.lira.field.ui.settings.SettingsScreen
import com.lira.field.ui.settings.SettingsViewModel
import com.lira.field.ui.theme.*

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun LiraNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val app = LiraApp.instance
    val session by app.sessionRepository.session.collectAsState()
    val startDestination = if (session != null) Screen.Home.route else Screen.Login.route

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(session) {
        if (session == null && currentRoute != null && currentRoute != Screen.Login.route) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home.route, "My Job", LiraIcons.Job),
        BottomNavItem(Screen.RiskAlerts.route, "Exceptions", LiraIcons.Exceptions),
        BottomNavItem(Screen.Activity.route, "History", LiraIcons.History),
        BottomNavItem(Screen.Settings.route, "Settings", LiraIcons.Settings)
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    val colors = LiraTheme.colors

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = colors.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = if (selected) colors.primary else colors.textSecondary
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    color = if (selected) colors.primary else colors.textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = colors.primaryContainer,
                                selectedIconColor = colors.primary,
                                selectedTextColor = colors.primary,
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary
                            )
                        )
                    }
                }
            }
        },
        containerColor = colors.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                val loginVm = remember {
                    LoginViewModel(app.repository, app.sessionRepository)
                }
                LoginScreen(
                    viewModel = loginVm,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                val homeVm = remember {
                    HomeViewModel(app.repository, app.sessionRepository)
                }
                HomeScreen(
                    viewModel = homeVm,
                    onNavigateToDetails = { assignmentId ->
                        navController.navigate(Screen.AssignmentDetails.createRoute(assignmentId))
                    },
                    onNavigateToEta = {
                        navController.navigate(Screen.UpdateEta.route)
                    },
                    onNavigateToCheckpoint = { assignmentId ->
                        navController.navigate(Screen.Checkpoint.createRoute(assignmentId))
                    },
                    onNavigateToDisruptions = {
                        navController.navigate(Screen.RiskAlerts.route)
                    }
                )
            }

            composable(
                route = Screen.AssignmentDetails.route,
                arguments = listOf(navArgument("assignmentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val assignmentId = backStackEntry.arguments?.getString("assignmentId") ?: ""
                val assignment = remember(assignmentId) {
                    app.database.assignmentDao.getAssignmentById(assignmentId)
                }
                AssignmentDetailsScreen(
                    assignment = assignment,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.UpdateEta.route) {
                val etaVm = remember {
                    UpdateEtaViewModel(app.repository, app.sessionRepository)
                }
                UpdateEtaScreen(
                    viewModel = etaVm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.RiskAlerts.route) {
                val riskVm = remember {
                    RiskAlertsViewModel(app.repository)
                }
                RiskAlertsScreen(
                    viewModel = riskVm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Checkpoint.route,
                arguments = listOf(navArgument("assignmentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val assignmentId = backStackEntry.arguments?.getString("assignmentId") ?: ""
                val checkpointVm = remember(assignmentId) {
                    CheckpointViewModel(app.repository, assignmentId)
                }
                CheckpointScreen(
                    viewModel = checkpointVm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Activity.route) {
                val activityVm = remember {
                    ActivityTimelineViewModel(app.repository)
                }
                ActivityTimelineScreen(
                    viewModel = activityVm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                val settingsVm = remember {
                    SettingsViewModel(app.repository, app.sessionRepository)
                }
                SettingsScreen(
                    viewModel = settingsVm,
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

