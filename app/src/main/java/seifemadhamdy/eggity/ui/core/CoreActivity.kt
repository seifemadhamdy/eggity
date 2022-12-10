package seifemadhamdy.eggity.ui.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import seifemadhamdy.eggity.R
import seifemadhamdy.eggity.databinding.ActivityCoreBinding
import seifemadhamdy.eggity.ui.cooking.CookingActivity
import seifemadhamdy.eggity.util.CookingTime


class CoreActivity : AppCompatActivity() {
    private val coreViewModel: CoreViewModel by viewModels()

    private val prefs by lazy {
        getSharedPreferences(packageName, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (prefs.getString(
            CookingActivity.ALARM_STATE,
            CookingActivity.Companion.AlarmState.NOT_TRIGGERED.value
        )) {
            CookingActivity.Companion.AlarmState.IN_PROGRESS.value -> {
                navigateToCookingActivity()
            }

            CookingActivity.Companion.AlarmState.TRIGGERED.value -> prefs.edit().putString(
                CookingActivity.ALARM_STATE,
                CookingActivity.Companion.AlarmState.NOT_TRIGGERED.value
            ).apply()
        }

        binding = ActivityCoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        coreViewModel.selectedMaterialCardView.observe(this) {
            checkMaterialCardView(it[0], it[1])
            updateCookingTime()
        }

        coreViewModel.eggQuantity.observe(this) {
            if (it > 1) {
                binding.eggsBoiledTypeMaterialTextView.text = getString(R.string.eggs_boiled_type)
                binding.quantityMaterialTextView.text = getString(R.string.x_eggs, it)
                binding.sizeOfEggsMaterialTextView.text = getString(R.string.size_of_eggs)
                binding.temperatureOfEggsMaterialTextView.text =
                    getString(R.string.temperature_of_eggs)

                if (binding.temperatureMaterialSwitch.isChecked) {
                    binding.temperatureMaterialSwitch.text =
                        getString(R.string.tip_refrigeration_temperatures)
                } else {
                    binding.temperatureMaterialSwitch.text =
                        getString(R.string.tip_room_temperature)
                }

                prefs.edit().putBoolean(ARE_EGGS_MULTIPLE, true).apply()
            } else {
                binding.eggsBoiledTypeMaterialTextView.text = getString(R.string.egg_boiled_type)
                binding.quantityMaterialTextView.text = getString(R.string.one_egg)
                binding.sizeOfEggsMaterialTextView.text = getString(R.string.size_of_egg)
                binding.temperatureOfEggsMaterialTextView.text =
                    getString(R.string.temperature_of_egg)

                if (binding.temperatureMaterialSwitch.isChecked) {
                    binding.temperatureMaterialSwitch.text =
                        getString(R.string.tip_refrigeration_temperatures_singular)
                } else {
                    binding.temperatureMaterialSwitch.text =
                        getString(R.string.tip_room_temperature_singular)
                }

                prefs.edit().putBoolean(ARE_EGGS_MULTIPLE, false).apply()
            }

            binding.removeMaterialButton.isEnabled =
                !(it <= 1 && binding.removeMaterialButton.isEnabled)
        }

        coreViewModel.cookingTime.observe(this) {
            binding.startExtendedFloatingActionButton.text = getString(
                R.string.cook_for_x_minutes,
                DateUtils.formatElapsedTime(it.toLong())
            )
        }

        setSingleSelection(MaterialCardViewType.BOILED_TYPE, MaterialCardViewType.SIZE)

        binding.removeMaterialButton.setOnClickListener {
            coreViewModel.decrementEggQuantity()
            updateCookingTime()
        }

        binding.addMaterialButton.setOnClickListener {
            coreViewModel.incrementEggQuantity()
            updateCookingTime()
        }

        binding.temperatureMaterialSwitch.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked) {
                coreViewModel.eggQuantity.value?.let {
                    if (it > 1) {
                        view.text = getString(R.string.tip_refrigeration_temperatures)
                    } else {
                        view.text = getString(R.string.tip_refrigeration_temperatures_singular)
                    }
                }
            } else {
                coreViewModel.eggQuantity.value?.let {
                    if (it > 1) {
                        view.text = getString(R.string.tip_room_temperature)
                    } else {
                        view.text = getString(R.string.tip_room_temperature_singular)
                    }
                }
            }

            updateCookingTime()
        }

        binding.startExtendedFloatingActionButton.setOnClickListener {
            navigateToCookingActivity()
        }
    }

    private fun navigateToCookingActivity() {
        Intent(this, CookingActivity::class.java).apply {
            putExtra(COOKING_TIME_KEY, coreViewModel.cookingTime.value)
            startActivity(this)
        }

        finish()
    }

    private fun checkMaterialCardView(vararg materialCardViewsResIds: Int) {
        for (materialCardViewsResId in materialCardViewsResIds) {
            findViewById<MaterialCardView>(materialCardViewsResId).isChecked = true
        }
    }

    private fun setSingleSelection(vararg materialCardViewTypes: MaterialCardViewType) {
        for (materialCardViewType in materialCardViewTypes)
            for (materialCardView in materialCardViewType.materialCardViews) {
                findViewById<MaterialCardView>(materialCardView).apply {
                    setOnClickListener {
                        if (!isChecked) {
                            @Suppress("NAME_SHADOWING")
                            for (materialCardView in arrayListOf<Int>().run {
                                addAll(materialCardViewType.materialCardViews)
                                remove(id)
                                toTypedArray()
                            })
                                binding.root.findViewById<MaterialCardView>(materialCardView).isChecked =
                                    false

                            isChecked = true

                            coreViewModel.apply {
                                if (materialCardViewType == MaterialCardViewType.BOILED_TYPE) {
                                    updateSelectedBoiledTypeMaterialCardView(id)
                                } else {
                                    updateSelectedSizeMaterialCardView(id)
                                }
                            }

                            updateCookingTime()
                        }
                    }
                }
            }
    }

    private fun getTotalCookingTime(): Int {
        if (coreViewModel.selectedMaterialCardView.value != null)
            return CookingTime(
                when (coreViewModel.selectedMaterialCardView.value!![0]) {
                    MaterialCardViewType.BOILED_TYPE.materialCardViews[0] -> CookingTime.Companion.BoiledType.SOFT
                    MaterialCardViewType.BOILED_TYPE.materialCardViews[1] -> CookingTime.Companion.BoiledType.MEDIUM
                    else -> CookingTime.Companion.BoiledType.HARD
                },
                coreViewModel.eggQuantity.value ?: 1,
                when (coreViewModel.selectedMaterialCardView.value!![1]) {
                    MaterialCardViewType.SIZE.materialCardViews[0] -> CookingTime.Companion.EggSize.SMALL
                    MaterialCardViewType.SIZE.materialCardViews[1] -> CookingTime.Companion.EggSize.MEDIUM
                    MaterialCardViewType.SIZE.materialCardViews[3] -> CookingTime.Companion.EggSize.EXTRA_LARGE
                    else -> CookingTime.Companion.EggSize.LARGE
                },
                binding.temperatureMaterialSwitch.isChecked
            ).totalTime()

        return 0
    }

    private fun updateCookingTime() {
        coreViewModel.updateCookingTime(getTotalCookingTime())
    }

    companion object {
        private lateinit var binding: ActivityCoreBinding

        const val COOKING_TIME_KEY = "cookingTime"
        const val ARE_EGGS_MULTIPLE = "areEggsMultiple"

        private enum class MaterialCardViewType(val materialCardViews: Array<Int>) {
            BOILED_TYPE(
                arrayOf(
                    binding.softBoiledMaterialCardView.id,
                    binding.mediumBoiledMaterialCardView.id,
                    binding.hardBoiledMaterialCardView.id
                )
            ),

            SIZE(
                arrayOf(
                    binding.sMaterialCardView.id,
                    binding.mMaterialCardView.id,
                    binding.lMaterialCardView.id,
                    binding.xMaterialCardView.id
                )
            )
        }
    }
}