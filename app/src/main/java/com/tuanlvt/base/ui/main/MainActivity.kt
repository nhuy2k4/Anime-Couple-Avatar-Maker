package com.tuanlvt.base.ui.main

import android.content.Context
import android.content.ContextWrapper
import androidx.core.view.isVisible
import com.brally.mobile.base.activity.navigate
import com.brally.mobile.base.databinding.ActivityMainBinding
import com.brally.mobile.service.event.NO_INTERNET_DIALOG_SHOW
import com.brally.mobile.service.event.subscribeEventNetwork
import com.brally.mobile.service.sound.AppMusicPlayer
import com.brally.mobile.ui.features.main.BaseMainActivity
import com.brally.mobile.utils.openSettingNetWork
import com.brally.mobile.utils.singleClick
import com.braly.ads.analytics.event.BralyTracking
import com.language_onboard.data.local.CommonAppSharePref
import com.language_onboard.data.model.Language
import com.tuanlvt.base.R
import com.tuanlvt.base.utils.ContextUtils
import org.koin.android.ext.android.inject
import java.util.Locale

class MainActivity : BaseMainActivity<ActivityMainBinding, MainViewModel>() {
    private val commonSharePref by inject<CommonAppSharePref>()
    override val graphResId: Int
        get() = R.navigation.app_nav

    private val listDestinationNoMusic = listOf(
        R.id.splashFragment,
        com.braly.ads.R.id.obParentFragment,
        com.braly.ads.R.id.languageFragment,
        com.braly.ads.R.id.onboardingFragment
    )

    override fun initView() {
        super.initView()
        navController?.addOnDestinationChangedListener { _, destination, _ ->
            if (listDestinationNoMusic.contains(destination.id).not()) {
                AppMusicPlayer.checkAndPlay()
            }
        }
    }

    override fun initData() {
        super.initData()
        subscribeEventNetwork { online ->
            runOnUiThread {
                binding.layoutNoInternet.root.isVisible = online.not()
            }
            if (online.not()) {
                BralyTracking.logEvent(this, NO_INTERNET_DIALOG_SHOW)
            }
        }
        binding.layoutNoInternet.buttonSetting.singleClick { openSettingNetWork() }

        viewModel.isLoading.observe {
            binding.loading.loadingView.isVisible = it
        }
    }

    override fun onFlowFinished() {
        navigate(R.id.homeFragment, isPopAll = true)
    }

    override fun onResume() {
        super.onResume()
        try {
            if (navController?.currentDestination == null) return
            if (listDestinationNoMusic.contains(navController?.currentDestination?.id).not()) {
                AppMusicPlayer.checkAndPlay()
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        AppMusicPlayer.stop()
        AppMusicPlayer.stopFxMusicPlayer()
    }

    override fun onDestroy() {
        AppMusicPlayer.releaseBackgroundMusic()
        AppMusicPlayer.releaseFxMusic()
        super.onDestroy()
    }

    override fun attachBaseContext(context: Context) {
        val locale = commonSharePref.languageCode ?: Language.ENGLISH.countryCode
        val localeUpdatedContext: ContextWrapper =
            ContextUtils.updateLocale(context, Locale(locale))
        super.attachBaseContext(localeUpdatedContext)
    }
}
