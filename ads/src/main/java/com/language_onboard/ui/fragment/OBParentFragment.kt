package com.language_onboard.ui.fragment

import android.content.Context
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.braly.ads.R
import com.braly.ads.databinding.FragmentObParentBinding
import com.language_onboard.intf.OnboardingHandler
import com.language_onboard.ui.BaseLanguageOnboardFragment
import com.language_onboard.utils.FeatureConfig

enum class Screen {
    ONBOARDING,
    LANGUAGE,
    NO_CONFIG
}

class OBParentFragment : BaseLanguageOnboardFragment<FragmentObParentBinding>() {
    private val navArgs by navArgs<OBParentFragmentArgs>()
    private var navigationHandler: OnboardingHandler? = null

    override fun getViewBinding(): FragmentObParentBinding =
        FragmentObParentBinding.inflate(layoutInflater)

    override fun shouldHideStatusBar(): Boolean {
        return navArgs.onboardingConfig.isHideStatusBar
    }

    override fun initView() {
        super.initView()
        if (startDestination() == Screen.LANGUAGE) {
            findNavController().navigate(R.id.languageFragment, navArgs.toBundle())
        } else if (startDestination() == Screen.ONBOARDING) {
            findNavController().navigate(R.id.onboardingFragment, navArgs.toBundle())
        } else {
            navigationHandler?.onFlowFinished()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnboardingHandler) {
            navigationHandler = context
        }
    }

    private fun startDestination(): Screen {
        val config = FeatureConfig.getConfigEnableFeatures()
        return if (config.language == true) Screen.LANGUAGE
        else if (config.onboarding == true) Screen.ONBOARDING
        else Screen.NO_CONFIG
    }

}