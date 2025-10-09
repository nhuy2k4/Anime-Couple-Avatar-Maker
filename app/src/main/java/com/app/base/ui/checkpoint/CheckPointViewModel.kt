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
            // outfit.features is Map<String,String> flattened like "male.hair" -> "123"
            val root = JSONObject()
            outfit.features.forEach { (k, v) ->
                val parts = k.split('.')
                if (parts.size == 2) {
                    val char = parts[0]
                    val layer = parts[1]
                    val obj = root.optJSONObject(char) ?: JSONObject().also { root.put(char, it) }
                    obj.put(layer, v.toIntOrNull() ?: 0)
                }
            }
            val maleJson = root.optJSONObject("male") ?: JSONObject()
            val femaleJson = root.optJSONObject("female") ?: JSONObject()
            _outfitLiveData.postValue(Pair(maleJson, femaleJson))
        }
    }
}