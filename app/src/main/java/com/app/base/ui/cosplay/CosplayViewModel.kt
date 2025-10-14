package com.app.base.ui.cosplay

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.base.R
import com.brally.mobile.base.viewmodel.BaseViewModel

class CosplayViewModel : BaseViewModel() {

    private val _characters = MutableLiveData<List<Int>>()
    val characters: LiveData<List<Int>> get() = _characters

    fun loadCharacters() {
        // 🔥 danh sách resource id trong drawable
        val characterImages = listOf(
            R.drawable.cos1,
            R.drawable.cos2
        )
        _characters.value = characterImages
    }

    // Return a sensible default gender for the cosplay flow.
    // Kept simple: can be extended to read user prefs or other logic later.
    fun getDefaultGender(): String = "female"
}
