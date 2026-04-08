package com.londontubeai.navigator.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.londontubeai.navigator.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TubeNotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_DISRUPTIONS = "disruptions"
        const val CHANNEL_COMMUTE = "commute"
        const val CHANNEL_AI_TIPS = "ai_tips"

        private const val DISRUPTION_NOTIFICATION_ID = 1001
        private const val COMMUTE_NOTIFICATION_ID = 1002
        private const val AI_TIP_NOTIFICATION_ID = 1003
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val disruptionChannel = NotificationChannel(
            CHANNEL_DISRUPTIONS,
            "Disruption Alerts",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Real-time alerts when your commute lines are disrupted"
            enableVibration(true)
        }

        val commuteChannel = NotificationChannel(
            CHANNEL_COMMUTE,
            "Commute Reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Time-to-leave reminders and commute insights"
        }

        val aiChannel = NotificationChannel(
            CHANNEL_AI_TIPS,
            "AI Tips",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Smart carriage tips and crowd predictions"
        }

        manager.createNotificationChannels(listOf(disruptionChannel, commuteChannel, aiChannel))
    }

    fun showDisruptionAlert(lineName: String, description: String, alternativeRoute: String? = null) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val text = buildString {
            append(description)
            if (alternativeRoute != null) {
                append("\n\nSuggested: $alternativeRoute")
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_DISRUPTIONS)
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentTitle("⚠️ $lineName Disrupted")
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(DISRUPTION_NOTIFICATION_ID, notification)
    }

    fun showCommuteReminder(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_COMMUTE)
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(COMMUTE_NOTIFICATION_ID, notification)
    }

    fun showAiTip(title: String, tip: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_AI_TIPS)
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentTitle(title)
            .setContentText(tip)
            .setStyle(NotificationCompat.BigTextStyle().bigText(tip))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(AI_TIP_NOTIFICATION_ID, notification)
    }
}
