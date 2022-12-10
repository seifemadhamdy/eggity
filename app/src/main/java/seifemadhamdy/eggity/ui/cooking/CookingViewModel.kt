package seifemadhamdy.eggity.ui.cooking

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.SystemClock
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import seifemadhamdy.eggity.receiver.AlarmReceiver
import seifemadhamdy.eggity.util.cancelNotifications

class CookingViewModel(private val app: Application) : AndroidViewModel(app) {
    private val notifyPendingIntent by lazy {
        PendingIntent.getBroadcast(
            getApplication(),
            REQUEST_CODE,
            notifyIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private val alarmManager by lazy {
        app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private val prefs by lazy {
        app.getSharedPreferences(app.packageName, Context.MODE_PRIVATE)
    }

    private val notifyIntent by lazy {
        Intent(app, AlarmReceiver::class.java)
    }

    private val _elapsedCookingTime = MutableLiveData<Long>()

    val elapsedCookingTime: LiveData<Long>
        get() = _elapsedCookingTime

    private var _isAlarmOn = MutableLiveData<Boolean>()

    private lateinit var timer: CountDownTimer

    private val _cookingTime = MutableLiveData<Int>()

    private val cookingTime: LiveData<Int>
        get() = _cookingTime

    init {
        _isAlarmOn.value = PendingIntent.getBroadcast(
            getApplication(),
            REQUEST_CODE,
            notifyIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_MUTABLE
            else
                PendingIntent.FLAG_NO_CREATE
        ) != null

        if (_isAlarmOn.value == true) {
            createTimer()
        }
    }

    fun startTimer() {
        _isAlarmOn.value?.let {
            if (!it) {
                _isAlarmOn.value = true

                val triggerTime =
                    SystemClock.elapsedRealtime() + (cookingTime.value?.times(1000) ?: 0)

                (ContextCompat.getSystemService(
                    app,
                    NotificationManager::class.java
                ) as NotificationManager).cancelNotifications()

                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    notifyPendingIntent
                )

                viewModelScope.launch {
                    saveTime(triggerTime)
                }
            }
        }

        createTimer()
    }

    fun createTimer() {
        viewModelScope.launch {
            val triggerTime = loadTime()

            timer = object : CountDownTimer(triggerTime, SECOND) {
                override fun onTick(millisUntilFinished: Long) {
                    _elapsedCookingTime.value = triggerTime - SystemClock.elapsedRealtime()

                    if (_elapsedCookingTime.value!! <= 0) {
                        resetTimer()
                    }
                }

                override fun onFinish() {
                    resetTimer()
                }
            }

            timer.start()
        }
    }

    fun cancelNotification() {
        resetTimer()
        alarmManager.cancel(notifyPendingIntent)
    }

    private fun resetTimer() {
        timer.cancel()
        _elapsedCookingTime.value = 0
        _isAlarmOn.value = false
    }

    private suspend fun saveTime(triggerTime: Long) =
        withContext(Dispatchers.IO) {
            prefs.edit().putLong(TRIGGER_TIME, triggerTime).apply()
        }

    private suspend fun loadTime(): Long =
        withContext(Dispatchers.IO) {
            prefs.getLong(TRIGGER_TIME, 0)
        }

    fun updateCookingTime(cookingTime: Int) {
        _cookingTime.value = cookingTime
    }

    companion object {
        const val REQUEST_CODE = 0
        private const val TRIGGER_TIME = "TRIGGER_AT"
        private const val SECOND: Long = 1000L
    }
}