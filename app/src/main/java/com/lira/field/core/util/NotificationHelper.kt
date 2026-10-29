package com.lira.field.core.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lira.field.MainActivity
import com.lira.field.R

object NotificationHelper {

    const val CHANNEL_PLAN_CHANGES = "channel_plan_changes"
    private const val CHANNEL_NAME = "Plan changes"
    const val CHANNEL_URGENT_DISPATCH = "channel_urgent_dispatch"
    private const val CHANNEL_URGENT_NAME = "Urgent dispatch"

    private const val NOTIFICATION_ID_SUPERSEDE = 1001
    private const val NOTIFICATION_ID_BRANCH = 1002
    private const val NOTIFICATION_ID_TAKEOVER = 2001
    private const val NOTIFICATION_ID_PNR_IMMINENT = 2002

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_PLAN_CHANGES,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical real-time dispatch updates and superseded plan alerts"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
        ensureUrgentChannel(context)
    }

    fun ensureUrgentChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_URGENT_DISPATCH,
                CHANNEL_URGENT_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent dispatch takeover and emergency driver reassignment"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun notifyUrgentTakeover(
        context: Context,
        assignmentId: String,
        title: String,
        body: String
    ) {
        ensureUrgentChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_TAKEOVER", true)
            putExtra("EXTRA_ASSIGNMENT_ID", assignmentId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_URGENT_DISPATCH)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        try {
            manager?.notify(NOTIFICATION_ID_TAKEOVER, notification)
        } catch (_: SecurityException) {
        }
    }

    fun notifyPnrImminent(
        context: Context,
        title: String,
        body: String
    ) {
        ensureUrgentChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            3,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_URGENT_DISPATCH)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        try {
            manager?.notify(NOTIFICATION_ID_PNR_IMMINENT, notification)
        } catch (_: SecurityException) {
        }
    }

    fun notifyPlanSuperseded(context: Context, oldVersion: Int, newVersion: Int) {
        ensureChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PLAN_CHANGES)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Plan Replaced by Dispatch")
            .setContentText("Plan replaced by dispatch: v$oldVersion → v$newVersion. Tap to review.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        try {
            manager?.notify(NOTIFICATION_ID_SUPERSEDE, notification)
        } catch (_: SecurityException) {
            // In case POST_NOTIFICATIONS runtime permission not yet granted by user
        }
    }

    fun notifyBranchActivated(context: Context, branchId: String, windowText: String) {
        ensureChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PLAN_CHANGES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("PLAN $branchId ACTIVE")
            .setContentText(windowText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        try {
            manager?.notify(NOTIFICATION_ID_BRANCH, notification)
        } catch (_: SecurityException) {
            // In case POST_NOTIFICATIONS runtime permission not yet granted by user
        }
    }
}

