package com.language_onboard.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CommonOnboardingViewModel : ViewModel() {
    private val _swipePage = MutableLiveData<String>()
    val swipePage: LiveData<String> get() = _swipePage

    fun setSwipePage(page: String) {
        _swipePage.postValue(page)
    }
}