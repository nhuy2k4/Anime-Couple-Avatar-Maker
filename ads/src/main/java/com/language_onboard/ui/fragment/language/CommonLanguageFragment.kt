package com.language_onboard.ui.fragment.language

import android.content.Context
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import com.braly.ads.R
import com.braly.ads.SmallDelayCallback
import com.braly.analytics.event.BralyTracking
import com.braly.ads.databinding.FragmentLanguageCommonBinding
import com.language_onboard.data.local.CommonAppSharePref
import com.language_onboard.data.model.Language
import com.language_onboard.data.model.LanguageSelector
import com.language_onboard.intf.OnboardingHandler
import com.language_onboard.ui.BaseLanguageOnboardFragment
import com.language_onboard.ui.adapter.CommonLanguageAdapter
import com.language_onboard.utils.FeatureConfig
import com.language_onboard.utils.Tracking
import com.language_onboard.utils.onBackPressed
import com.language_onboard.utils.setLocale
import com.language_onboard.utils.tracking

class CommonLanguageFragment : BaseLanguageOnboardFragment<FragmentLanguageCommonBinding>() {
    private val navArgs by navArgs<CommonLanguageFragmentArgs>()
    private val onboardingConfig by lazy { navArgs.onboardingConfig }
    private val languageNativeRes by lazy { onboardingConfig.languageNativeRes }
    private var navigationHandler: OnboardingHandler? = null
    private val languageAdapter by lazy {
        CommonLanguageAdapter {
            submitListLanguages(it)
        }
    }

    private var isFirstClick = true

    private val appSharePref: CommonAppSharePref by lazy {
        CommonAppSharePref(requireContext())
    }

    override fun getViewBinding(): FragmentLanguageCommonBinding =
        FragmentLanguageCommonBinding.inflate(layoutInflater)

    override fun shouldHideStatusBar(): Boolean {
        return onboardingConfig.isHideStatusBar
    }

    override fun initView() {
        super.initView()
        onBackPressed {}
        tracking(Tracking.LANGUAGE_MODULE_SHOW)
        navArgs.onboardingConfig.onboardingItems
        appSharePref.isEnableLanguage = true
        binding.rvLanguage.apply {
            adapter = languageAdapter
            itemAnimator = DefaultItemAnimator()

        }
        initLanguages()
        binding.btnCheck.setOnClickListener {
            tracking(Tracking.CLICK_SELECTED_LANGUAGE_MD)
            val language = languageAdapter.currentList.find { it.isCheck }
            val languageCode = language?.language?.countryCode ?: Language.ENGLISH.countryCode

            appSharePref.languageCode = languageCode
            appSharePref.applyLanguage(languageCode)
            requireContext().setLocale(languageCode)
            if (languageCode == Language.ENGLISH.countryCode) {
                checkToNavigate()
            } else {
                checkToNavigate()
                SmallDelayCallback { recreateApp() }
            }
        }
        binding.toolbarWrapper.tvTitle.text = getString(R.string.text_choose_language)
    }

    override fun onResume() {
        super.onResume()
        BralyTracking.logScreenView(
            requireContext(),
            this@CommonLanguageFragment::class.java.simpleName
        )
    }

    private fun recreateApp() {
        activity?.recreate()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnboardingHandler) {
            navigationHandler = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        navigationHandler = null
    }

    private fun navigateToHomeFragment() {
        navigationHandler?.onFlowFinished()
    }

    fun naviagteToOnboarding() {
        findNavController().navigate(R.id.onboardingFragment, navArgs.toBundle())
    }

    private fun submitListLanguages(languageSelector: LanguageSelector) {
        val position = languageAdapter.currentList.indexOf(languageSelector)
        val languages = onboardingConfig.languages.toTypedArray().mapIndexed { index, language ->
            LanguageSelector(language, isCheck = index == position)
        }

        languageAdapter.submitList(languages)
    }

    private fun initLanguages() {
        val languageCode = appSharePref.languageCode ?: Language.SPANISH.countryCode
        val languagePosition = Language.getLanguagePosition(languageCode)
        val languages = onboardingConfig.languages.toTypedArray().mapIndexed { index, language ->
            LanguageSelector(language, isCheck = index == languagePosition)
        }
        languageAdapter.submitList(languages)
    }

    private fun checkToNavigate() {
        val config = FeatureConfig.getConfigEnableFeatures()
        if (config.onboarding == true) {
            naviagteToOnboarding()
        } else {
            navigateToHomeFragment()
        }
    }

}