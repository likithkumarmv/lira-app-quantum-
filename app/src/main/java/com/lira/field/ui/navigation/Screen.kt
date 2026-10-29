package com.lira.field.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "Sign In")
    object Home : Screen("home", "My Today")
    object AssignmentDetails : Screen("assignment_details/{assignmentId}", "Assignment Details") {
        fun createRoute(assignmentId: String) = "assignment_details/$assignmentId"
    }
    object UpdateEta : Screen("update_eta", "Update ETA")
    object RiskAlerts : Screen("risk_alerts", "Risk Alerts")
    object Checkpoint : Screen("checkpoint/{assignmentId}", "Checkpoint Status") {
        fun createRoute(assignmentId: String) = "checkpoint/$assignmentId"
    }
    object Activity : Screen("activity", "Activity Timeline")
    object Settings : Screen("settings", "Offline & Settings")
}

