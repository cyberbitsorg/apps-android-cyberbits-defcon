package com.cyberbits.defcon.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.cyberbits.defcon.MainActivity

class DefconNotifier(private val context: Context) {

    companion object {
        const val CHANNEL_DEFCON_ALERTS = "defcon_alerts"
        const val CHANNEL_NEW_ARTICLES = "new_articles"
    }

    init { ensureChannels() }

    private fun ensureChannels() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_DEFCON_ALERTS, "DEFCON alerts", NotificationManager.IMPORTANCE_HIGH))
        nm.createNotificationChannel(NotificationChannel(
            CHANNEL_NEW_ARTICLES, "New articles", NotificationManager.IMPORTANCE_LOW))
    }

    private fun canPost(): Boolean = ContextCompat.checkSelfPermission(
        context, Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED

    fun postDefconLevel(level: Int, label: String) {
        if (!canPost()) return
        val intent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(context, CHANNEL_DEFCON_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("DEFCON $label")
            .setContentText("Level $level — tap to review")
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(1001, n)
    }

    fun postNewArticles(count: Int) {
        if (!canPost() || count <= 0) return
        val intent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(context, CHANNEL_NEW_ARTICLES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Cyberbits DEFCON")
            .setContentText("$count new article${if (count == 1) "" else "s"}")
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(1002, n)
    }
}
