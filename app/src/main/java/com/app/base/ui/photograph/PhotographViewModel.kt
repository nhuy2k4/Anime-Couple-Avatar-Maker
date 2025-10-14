package com.app.base.ui.photograph

import android.content.Context
import android.util.Log
import com.app.base.database.AppDatabase
import com.app.base.database.entity.BackgroundEntity
import com.app.base.database.entity.OutfitEntity
import com.brally.mobile.base.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class PhotographViewModel : BaseViewModel() {

    private val _backgrounds = MutableStateFlow<List<OutfitEntity>>(emptyList())
    val backgrounds = _backgrounds.asStateFlow()

    /** Load backgrounds: prefer DB; if DB empty -> seed from assets JSON then return list */
    fun loadBackgrounds(context: Context) {
        launchHandler {
            val list = withContext(Dispatchers.IO) {
                try {
                    val db = AppDatabase.getInstance(context)
                    val bgDao = db.backgroundDao()

                    // 1) Try DB first
                    val dbBackgrounds = try { bgDao.getAllBackgrounds() } catch (_: Exception) { emptyList<BackgroundEntity>() }

                    if (dbBackgrounds.isNotEmpty()) {
                        Log.d("PhotographViewModel", "Loaded ${dbBackgrounds.size} backgrounds from DB")
                    } else {
                        Log.d("PhotographViewModel", "No backgrounds found in DB; will try to seed from assets")
                    }

                    val finalBackgrounds: List<BackgroundEntity> = if (dbBackgrounds.isNotEmpty()) {
                        dbBackgrounds
                    } else {
                        // 2) DB empty -> read from assets and seed DB
                        try {
                            val jsonStr = context.assets.open("data/backgrounds.json")
                                .bufferedReader().use { it.readText() }
                            val jsonArray = org.json.JSONArray(jsonStr)
                            val seedList = mutableListOf<BackgroundEntity>()
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val imagePath = obj.optString("image", "")
                                if (imagePath.isNotEmpty()) {
                                    val id = obj.optString("id", imagePath)
                                    val name = obj.optString("name", id)
                                    val unlock = obj.optString("unlockCondition", "")
                                    seedList.add(BackgroundEntity(id = id, name = name, image = imagePath, unlockCondition = unlock.ifEmpty { null }))
                                }
                            }
                            if (seedList.isNotEmpty()) {
                                try { bgDao.insertAll(seedList) } catch (_: Exception) { /* ignore */ }
                                Log.d("PhotographViewModel", "Seeded ${seedList.size} backgrounds into DB from assets")
                            } else {
                                Log.d("PhotographViewModel", "No background definitions found in assets/data/backgrounds.json")
                            }
                            // read back from DB to have canonical list (or fallback to seedList)
                            try { bgDao.getAllBackgrounds() } catch (_: Exception) { seedList }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            emptyList<BackgroundEntity>()
                        }
                    }

                    // Map BackgroundEntity -> OutfitEntity expected by adapter (backgroundId holds asset path)
                    val checker = com.app.base.core.UnlockChecker(context)
                    val unlockedBackgrounds = finalBackgrounds.filter { checker.isUnlockedCondition(it.unlockCondition) }
                    Log.d("PhotographViewModel", "Returning ${unlockedBackgrounds.size} unlocked backgrounds to UI (total available=${finalBackgrounds.size})")
                    unlockedBackgrounds.map { bgEntity -> OutfitEntity(backgroundId = bgEntity.image) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                }
            }
            _backgrounds.value = list
        }
    }
}
