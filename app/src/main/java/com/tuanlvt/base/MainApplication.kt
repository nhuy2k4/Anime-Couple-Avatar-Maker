package com.tuanlvt.base

import androidx.core.content.ContextCompat
import com.brally.mobile.base.application.BaseApplication
import com.brally.mobile.data.model.AppInfo
import com.brally.mobile.service.firebase.AppRemoteConfig
import com.brally.mobile.utils.Constant
import com.braly.ads.ads.BralyMobileAds
import com.braly.ads.data.Configuration
import com.language_onboard.data.local.CommonAppSharePref
import com.language_onboard.di.commonViewModelModule
import com.tuanlvt.base.ui.home.HomeFragment
import com.tuanlvt.base.ui.splash.SplashFragment
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

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
