package com.app.base.ui.checkpoint

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.base.database.AppDatabase
import com.brally.mobile.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class CheckPointViewModel : BaseViewModel() {
    private val _outfitLiveData = MutableLiveData<Pair<JSONObject, JSONObject>>()
    val outfitLiveData: LiveData<Pair<JSONObject, JSONObject>> = _outfitLiveData

    fun loadLatestOutfit(db: AppDatabase) {
        viewModelScope.launch(Dispatchers.IO) {
            val outfit = db.outfitDao().getLatestOutfit() ?: return@launch
            val json = JSONObject(outfit.outfitJson)
            val maleJson = json.optJSONObject("male") ?: JSONObject()
            val femaleJson = json.optJSONObject("female") ?: JSONObject()
            _outfitLiveData.postValue(Pair(maleJson, femaleJson))
        }
    }
}