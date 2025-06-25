package com.language_onboard.ui.fragment.onboarding

import android.content.Context
import android.os.Handler
import androidx.navigation.fragment.navArgs
import com.braly.ads.R
import com.braly.ads.databinding.FragmentOnboardingModuleCommonBinding
import com.language_onboard.data.model.OnboardingItem
import com.language_onboard.data.model.OnboardingType
import com.language_onboard.intf.OnboardingHandler
import com.language_onboard.ui.BaseLanguageOnboardFragment
import com.language_onboard.ui.adapter.OnboardingPagerAdapter
import com.language_onboard.ui.viewmodel.CommonOnboardingViewModel
import com.language_onboard.utils.AdManager
import com.language_onboard.utils.Tracking
import com.language_onboard.utils.gone
import com.language_onboard.utils.nextPage
import com.language_onboard.utils.onBackPressed
import com.language_onboard.utils.setPageChangeListener
import com.language_onboard.utils.tracking
import com.language_onboard.utils.visible
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class CommonOnBoardingModuleFragment : BaseLanguageOnboardFragment<FragmentOnboardingModuleCommonBinding>() {
    private var navigationHandler: OnboardingHandler? = null
    private lateinit var onboardingPagerAdapter: OnboardingPagerAdapter
    private val navArgs by navArgs<CommonOnBoardingModuleFragmentArgs>()
    private val onboardingConfig by lazy { navArgs.onboardingConfig }
    private val viewModel: CommonOnboardingViewModel by activityViewModel()
    private val appOnboardingItems by lazy {
        activity?.let {
            val isOnboardingFsnEnable = AdManager.shouldShowOnboardingNativeFull(it)
            if (isOnboardingFsnEnable) {
                val items = mutableListOf<OnboardingItem>()
                val size = navArgs.onboardingConfig.onboardingItems.size
                items.addAll(navArgs.onboardingConfig.onboardingItems)
                var insertPos = size - 1
                if (insertPos <= 0) insertPos = 1
                items.add(
                    insertPos, OnboardingItem(
                        OnboardingType.ADVERTISEMENT.type,
                        nativeAdsLayoutRes = onboardingConfig.nativeFullRes
                    )
                )
                items
            } else {
                navArgs.onboardingConfig.onboardingItems
            }
        } ?: emptyList()
    }

    private val reloadNativeSmallHandler = Handler()

    override fun shouldHideStatusBar(): Boolean {
        return onboardingConfig.isHideStatusBar
    }


    override fun onDestroy() {
        super.onDestroy()
        reloadNativeSmallHandler.removeCallbacksAndMessages(null)
    }

    override fun initView() {
        setupViewPager(appOnboardingItems)
        val layoutRes = if (appOnboardingItems.isNotEmpty()) {
            appOnboardingItems.first().nativeAdsLayoutRes
        } else {
            null
        }

        binding.viewPager.setPageChangeListener { position ->
            val currentItem = appOnboardingItems[position]
            if (currentItem.type == OnboardingType.ADVERTISEMENT.type) {
                reloadNativeSmallHandler.removeCallbacksAndMessages(null)
                binding.btnNextModule.gone()
                binding.dotIndicator.gone()
                setupCtaButton(false)
                binding.commonNextButton.apply {
                    visible()
                    setOnClickListener {
                        binding.viewPager.nextPage()
                        tracking("${Tracking.ONBOARD_CLICK_NEXT_MD}_${binding.viewPager.currentItem}")
                    }
                }
                viewModel.setSwipePage(currentItem.type)
                tracking(Tracking.SHOW_FULL_NATIVE_ONBOARDING_MD)
            } else if (position == appOnboardingItems.size - 1) {
                binding.dotIndicator.visible()
                binding.btnNextModule.visible()
                binding.commonNextButton.gone()
                setupCtaButton(true)
                reloadNativeSmallHandler.removeCallbacksAndMessages(null)
//                AdManager.showNativeWithHandler(
//                    requireActivity(),
//                    this@CommonOnBoardingModuleFragment,
//                    reloadNativeSmallHandler,
//                    binding.nativeAd,
//                    AdManager.NATIVE_OB3,
//                    currentItem.nativeAdsLayoutRes
//                )
                viewModel.setSwipePage(currentItem.type)
                tracking(Tracking.SHOW_ONBOARDING_3_MD)
            } else {
                when (position) {
                    0 -> {
                        reloadNativeSmallHandler.removeCallbacksAndMessages(null)
//                        AdManager.showNativeWithHandler(
//                            requireActivity(),
//                            this@CommonOnBoardingModuleFragment,
//                            reloadNativeSmallHandler,
//                            binding.nativeAd,
//                            AdManager.NATIVE_OB1,
//                            layoutRes
//                        )
                        tracking(Tracking.SHOW_ONBOARDING_1_MD)
                    }

                    1 -> {
                        reloadNativeSmallHandler.removeCallbacksAndMessages(null)
//                        AdManager.showNativeWithHandler(
//                            requireActivity(),
//                            this@CommonOnBoardingModuleFragment,
//                            reloadNativeSmallHandler,
//                            binding.nativeAd,
//                            AdManager.NATIVE_OB2,
//                            currentItem.nativeAdsLayoutRes
//                        )
                        tracking(Tracking.SHOW_ONBOARDING_2_MD)
                    }
                }
                binding.dotIndicator.visible()
                binding.btnNextModule.visible()
                binding.commonNextButton.gone()
                setupCtaButton(false)

                viewModel.setSwipePage(currentItem.type)
            }

            setupGuidingButton(position)
        }

        binding.btnNextModule.setOnClickListener {
            if (binding.viewPager.currentItem == appOnboardingItems.size - 1) {
                navigateToHome()
                tracking(Tracking.ONBOARD_CLICK_GET_STARTED_MD)
            } else {
                binding.viewPager.nextPage()
                tracking("${Tracking.ONBOARD_CLICK_NEXT_MD}_${binding.viewPager.currentItem}")
            }
        }

        onBackPressed {}
    }

    private fun setupGuidingButton(currentPos: Int) {
        val nextPos = currentPos + 1
        if (nextPos < appOnboardingItems.size) {
            val nextObItem = appOnboardingItems.getOrElse(nextPos) { null }
            if (nextObItem != null && nextObItem.type == OnboardingType.ADVERTISEMENT.type) {
                binding.lottieAnim.visible()
            } else {
                binding.lottieAnim.gone()
            }
        } else {
            binding.lottieAnim.gone()
        }
    }

    private fun setupCtaButton(isShowStartButton: Boolean) {
        if (!isShowStartButton) {
            binding.btnNextModule.text = getString(R.string.title_next)
        } else {
            binding.btnNextModule.text = getString(R.string.title_started)
        }
    }

    override fun getViewBinding(): FragmentOnboardingModuleCommonBinding =
        FragmentOnboardingModuleCommonBinding.inflate(layoutInflater)

    override fun onResume() {
        super.onResume()
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

    private fun setupViewPager(onboardingItems: List<OnboardingItem>) {
        onboardingPagerAdapter =
            OnboardingPagerAdapter(this@CommonOnBoardingModuleFragment, onboardingItems)
        binding.viewPager.adapter = onboardingPagerAdapter
        binding.dotIndicator.attachTo(binding.viewPager)
    }

    private fun navigateToHome() {
        navigationHandler?.onFlowFinished()
    }
}