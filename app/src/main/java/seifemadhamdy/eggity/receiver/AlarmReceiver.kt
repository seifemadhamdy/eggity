package seifemadhamdy.eggity.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import seifemadhamdy.eggity.ui.cooking.CookingActivity
import seifemadhamdy.eggity.util.sendNotification

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        (ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager).sendNotification(context)

        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE).edit()
            .putString(
                CookingActivity.ALARM_STATE,
                CookingActivity.Companion.AlarmState.TRIGGERED.value
            )
            .apply()
    }
}