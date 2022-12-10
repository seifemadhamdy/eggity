package seifemadhamdy.eggity.util

import android.text.format.DateUtils
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter

object BindingAdapters {
    @BindingAdapter("android:isSelected")
    @JvmStatic
    fun setSelected(view: View, isSelected: Boolean) {
        view.isSelected = isSelected
    }

    @BindingAdapter("elapsedTime")
    @JvmStatic
    fun TextView.setElapsedTime(value: Long) {
        (value / 1000).apply {
            text = if (this < 60) {
                toString()
            } else {
                DateUtils.formatElapsedTime(this)
            }
        }
    }
}