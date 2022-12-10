package seifemadhamdy.eggity.ui.cooking

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.color.MaterialColors
import seifemadhamdy.eggity.R
import seifemadhamdy.eggity.databinding.ActivityCookingBinding
import seifemadhamdy.eggity.ui.core.CoreActivity

class CookingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCookingBinding
    private val cookingViewModel: CookingViewModel by viewModels()

    private val cookingTime by lazy {
        intent.extras?.getInt(CoreActivity.COOKING_TIME_KEY)
    }

    private val prefs by lazy {
        getSharedPreferences(packageName, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cooking)
        binding.lifecycleOwner = this
        binding.cookingViewModel = cookingViewModel

        if (prefs.getBoolean(CoreActivity.ARE_EGGS_MULTIPLE, false)) {
            binding.boiledTipDescriptionMaterialTextView.text =
                getString(R.string.boiled_tip_description)
        } else {
            binding.boiledTipDescriptionMaterialTextView.text =
                getString(R.string.boiled_tip_description_singular)
        }

        cookingViewModel.elapsedCookingTime.observe(this) {
            if (it == 0L) {
                binding.cancelExtendedFloatingActionButton.text = getString(R.string.finish)
            }
        }

        createChannel(
            getString(R.string.egg_notification_channel_id),
            getString(R.string.egg_notification_channel_name)
        )

        cookingTime?.let {
            cookingViewModel.updateCookingTime(it)

            when (prefs.getString(
                ALARM_STATE,
                AlarmState.NOT_TRIGGERED.value
            )) {
                AlarmState.NOT_TRIGGERED.value -> {
                    prefs.edit().putString(ALARM_STATE, AlarmState.IN_PROGRESS.value).apply()
                    cookingViewModel.startTimer()
                }

                AlarmState.IN_PROGRESS.value -> cookingViewModel.createTimer()
            }
        }

        binding.cancelExtendedFloatingActionButton.setOnClickListener {
            cookingViewModel.cancelNotification()
            prefs.edit().putString(ALARM_STATE, AlarmState.NOT_TRIGGERED.value).apply()
            finish()
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    setShowBadge(false)
                    enableLights(true)

                    lightColor = MaterialColors.getColor(
                        binding.root,
                        androidx.appcompat.R.attr.colorPrimary
                    )

                    enableVibration(true)
                }
            )
        }
    }

    companion object {
        const val ALARM_STATE = "alarmState"

        enum class AlarmState(val value: String) {
            NOT_TRIGGERED("notTriggered"),
            IN_PROGRESS("inProgress"),
            TRIGGERED("triggered")
        }
    }
}