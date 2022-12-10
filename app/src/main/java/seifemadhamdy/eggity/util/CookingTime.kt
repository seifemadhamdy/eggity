package seifemadhamdy.eggity.util

import android.util.Log

class CookingTime(
    private val eggBoiledType: BoiledType = BoiledType.HARD,
    eggQuantity: Int = 1,
    private val eggSize: EggSize = EggSize.LARGE,
    isEggFrosted: Boolean = true
) {
    private fun boilingTime(): Int {
        when (eggBoiledType) {
            BoiledType.SOFT -> {
                return when (eggSize) {
                    EggSize.SMALL -> 190
                    EggSize.MEDIUM -> 220
                    EggSize.LARGE -> 250
                    EggSize.EXTRA_LARGE -> 270
                }
            }

            BoiledType.MEDIUM -> {
                return when (eggSize) {
                    EggSize.SMALL -> 250
                    EggSize.MEDIUM -> 280
                    EggSize.LARGE -> 310
                    EggSize.EXTRA_LARGE -> 340
                }
            }

            BoiledType.HARD -> {
                return when (eggSize) {
                    EggSize.SMALL -> 480
                    EggSize.MEDIUM -> 520
                    EggSize.LARGE -> 570
                    EggSize.EXTRA_LARGE -> 620
                }
            }
        }
    }

    private val extraQuantityTime = if (eggQuantity > 1) {
        if (isEggFrosted) {
            (eggQuantity - 1) * 20
        } else {
            (eggQuantity - 1) * 15
        }
    } else {
        0
    }

    private val defrostTime = if (isEggFrosted) {
        when (eggSize) {
            EggSize.SMALL -> 50
            EggSize.MEDIUM -> 60
            else -> 70
        }
    } else {
        0
    }

    fun totalTime() = boilingTime() + extraQuantityTime + defrostTime.apply {
        Log.d(
            "TAG",
            "totalTime: $this"
        )
    }

    companion object {
        enum class BoiledType {
            SOFT,
            MEDIUM,
            HARD
        }

        enum class EggSize {
            SMALL,
            MEDIUM,
            LARGE,
            EXTRA_LARGE
        }
    }
}