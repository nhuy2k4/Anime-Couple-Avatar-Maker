package com.app.base.data.repositories

import android.content.Context
import com.app.base.data.models.CosplayScene
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CosplaySceneRepository(private val context: Context) {
    fun loadScenes(): List<CosplayScene> = CosplayRepository(context).loadCosplayScenes()
}

