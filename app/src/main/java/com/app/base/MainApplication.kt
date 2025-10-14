package com.app.base

import android.util.Log
import androidx.core.content.ContextCompat
import com.brally.mobile.base.application.BaseApplication
import com.brally.mobile.data.model.AppInfo
import com.brally.mobile.service.firebase.AppRemoteConfig
import com.brally.mobile.utils.Constant
import com.braly.ads.ads.BralyMobileAds
import com.braly.ads.data.Configuration
import com.language_onboard.data.local.CommonAppSharePref
import com.language_onboard.di.commonViewModelModule
import com.app.base.ui.home.HomeFragment
import com.app.base.ui.splash.SplashFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import com.app.base.database.AppDatabase
import com.app.base.database.entity.BackgroundEntity
import com.app.base.database.repository.FeatureRepository

class MainApplication : BaseApplication() {
    override val appInfo: AppInfo by lazy {
        AppInfo(
            appId = BuildConfig.APPLICATION_ID,
            icon = R.mipmap.ic_launcher,
            appName = ContextCompat.getContextForLanguage(this).getString(R.string.app_name),
            versionCode = BuildConfig.VERSION_CODE,
            versionName = BuildConfig.VERSION_NAME,
            isDebug = BuildConfig.DEBUG,
            privacy = Constant.POLICY,
            term = Constant.TERM,
            emailFeedback = Constant.EMAIL_FEEDBACK,
            appFlyer = Constant.KEY_APPSFLYER,
            rawGit = AppRemoteConfig.getDataResourceType().domainUrl,
            splashClass = SplashFragment::class.java,
            homeClass = HomeFragment::class.java,
            soundBGClass = arrayListOf(),
            appmetrica = null
        )
    }

    override fun onCreate() {
        super.onCreate()
        BralyMobileAds.apply {
            initConfig(
                this@MainApplication,
                Configuration(
                    defaultAds = Constant.DEFAULT_CONFIG_ADS,
                    isDebug = appInfo.isDebug,
                    testDeviceIds = emptyList(),
                    appsflyerKey = appInfo.appFlyer,
                    appmetricaNetwork = appInfo.appmetrica
                ).useDebugConfig(useDebug = isDebuggable())
            )
        }

        initKoin()

        // Seed data on background: features and backgrounds
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1) Ensure features are seeded (FeatureRepository will seed DB when loading from assets)
                try {
                    FeatureRepository(this@MainApplication).loadAllFeatures()
                    Log.d("MainApplication", "FeatureRepository.loadAllFeatures() invoked for seeding")
                } catch (e: Exception) {
                    Log.w("MainApplication", "Failed to seed features: ${e.message}")
                }

                // 2) Ensure backgrounds table is seeded
                try {
                    val db = AppDatabase.getInstance(this@MainApplication)
                    val dao = db.backgroundDao()
                    val existing = try { dao.getAllBackgrounds() } catch (_: Exception) { emptyList<BackgroundEntity>() }
                    if (existing.isEmpty()) {
                        val jsonStr = assets.open("data/backgrounds.json").bufferedReader().use { it.readText() }
                        val arr = org.json.JSONArray(jsonStr)
                        val seedList = mutableListOf<BackgroundEntity>()
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            val imagePath = obj.optString("image", "")
                            if (imagePath.isNotEmpty()) {
                                val id = obj.optString("id", imagePath)
                                val name = obj.optString("name", id)
                                val unlock = obj.optString("unlockCondition", "")
                                seedList.add(BackgroundEntity(id = id, name = name, image = imagePath, unlockCondition = unlock.ifEmpty { null }))
                            }
                        }
                        if (seedList.isNotEmpty()) {
                            try { dao.insertAll(seedList) } catch (e: Exception) { Log.w("MainApplication", "Failed insertAll backgrounds: ${e.message}") }
                            Log.d("MainApplication", "Seeded ${seedList.size} backgrounds into DB from assets")
                        } else {
                            Log.d("MainApplication", "No entries found in assets/data/backgrounds.json to seed backgrounds DB")
                        }
                    } else {
                        Log.d("MainApplication", "Backgrounds table already has ${existing.size} entries; skipping seeding")
                    }
                } catch (e: Exception) {
                    Log.w("MainApplication", "Failed to seed backgrounds: ${e.message}")
                }
            } catch (t: Throwable) {
                Log.e("MainApplication", "Unexpected error during seeding: ${t.message}")
            }
        }
    }

    private fun isDebuggable() = BuildConfig.DEBUG

    private fun initKoin() {
        startKoin {
            androidLogger(if (appInfo.isDebug) Level.ERROR else Level.NONE)
            androidContext(this@MainApplication)
            modules(
                commonViewModelModule,
                module {
                    single { CommonAppSharePref(get()) }
                }
            )
        }
    }
}
