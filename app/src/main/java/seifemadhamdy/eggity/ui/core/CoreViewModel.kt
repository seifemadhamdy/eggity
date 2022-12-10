package seifemadhamdy.eggity.ui.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import seifemadhamdy.eggity.R

class CoreViewModel : ViewModel() {
    private var _selectedMaterialCardView: MutableLiveData<ArrayList<Int>> =
        MutableLiveData(
            arrayListOf(
                R.id.hard_boiled_material_card_view,
                R.id.l_material_card_view
            )
        )

    val selectedMaterialCardView: LiveData<ArrayList<Int>> get() = _selectedMaterialCardView

    private var _eggQuantity: MutableLiveData<Int> = MutableLiveData(1)
    val eggQuantity: LiveData<Int> get() = _eggQuantity

    private var _cookingTime: MutableLiveData<Int> = MutableLiveData()
    val cookingTime: LiveData<Int> get() = _cookingTime


    fun updateSelectedBoiledTypeMaterialCardView(boiledTypeMaterialCardViewResId: Int) {
        _selectedMaterialCardView.value?.set(0, boiledTypeMaterialCardViewResId)
    }

    fun updateSelectedSizeMaterialCardView(sizeMaterialCardViewResId: Int) {
        _selectedMaterialCardView.value?.set(1, sizeMaterialCardViewResId)
    }

    fun incrementEggQuantity() {
        _eggQuantity.value = _eggQuantity.value?.inc()
    }

    fun decrementEggQuantity() {
        if (eggQuantity.value!! > 1)
            _eggQuantity.value = _eggQuantity.value?.dec()
    }

    fun updateCookingTime(cookingTime: Int) {
        _cookingTime.value = cookingTime
    }
}