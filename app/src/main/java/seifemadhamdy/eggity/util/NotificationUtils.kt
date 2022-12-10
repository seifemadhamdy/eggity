package seifemadhamdy.eggity.util

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import seifemadhamdy.eggity.R
import seifemadhamdy.eggity.ui.core.CoreActivity

private const val NOTIFICATION_ID = 0

@SuppressLint("UnspecifiedImmutableFlag")
fun NotificationManager.sendNotification(applicationContext: Context) {
    NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.egg_notification_channel_id)
    )
        .setSmallIcon(R.drawable.hard_boiled_egg)
        .setContentTitle(
            if (!applicationContext.getSharedPreferences(
                    applicationContext.packageName,
                    Context.MODE_PRIVATE
                ).getBoolean(CoreActivity.ARE_EGGS_MULTIPLE, false)
            ) {
                applicationContext.getString(R.string.egg_ready)
            } else {
                applicationContext.getString(R.string.eggs_ready)
            }
        )
        .setContentText(applicationContext.getString(R.string.bon_appetit))
        .setContentIntent(
            PendingIntent.getActivity(
                applicationContext,
                NOTIFICATION_ID,
                Intent(applicationContext, CoreActivity::class.java),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                else
                    PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        .setAutoCancel(true)
        .setStyle(NotificationCompat.BigTextStyle())
        .apply {
            priority = NotificationCompat.PRIORITY_MAX
            notify(NOTIFICATION_ID, build())
        }
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}