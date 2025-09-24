package com.app.base.ui.photograph

import android.content.Context
import com.app.base.database.AppDatabase
import com.app.base.database.OutfitEntity
import com.brally.mobile.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class PhotographViewModel : BaseViewModel() {

    private val _photos = MutableStateFlow<List<OutfitEntity>>(emptyList())
    val photos = _photos.asStateFlow()

    fun loadInitialData(context: Context) {
        launchHandler {
            val list = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(context).outfitDao().getAllOutfits()
            }
            _photos.value = list
        }
    }

    fun insertOutfit(context: Context, outfit: OutfitEntity, onComplete: () -> Unit) {
        launchHandler {
            withContext(Dispatchers.IO) {
                AppDatabase.getInstance(context).outfitDao().insert(outfit)
            }
            loadInitialData(context)
            onComplete()
        }
    }

}
