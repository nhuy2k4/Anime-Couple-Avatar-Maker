package com.app.base.ui.home

import androidx.navigation.fragment.findNavController
import com.app.base.R
import com.app.base.component.dialog.ExitsAppDialog
import com.app.base.databinding.FragmentHomeBinding
import com.app.base.ui.category.CategoryFragmentArgs
import com.app.base.ui.main.MainViewModel
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.navigate
import com.brally.mobile.base.activity.onBackPressed
import com.brally.mobile.service.ads.AdManager
import com.brally.mobile.service.ads.showFull
import com.brally.mobile.service.ads.showNative
import com.brally.mobile.service.event.EXIT_DIALOG_SHOW
import com.brally.mobile.service.event.HOME_CLICK_BACK
import com.brally.mobile.utils.singleClick
import com.braly.analytics.notification.UpdateAppUtils
import com.language_onboard.utils.tracking
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.getValue

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {
    override fun initView() {
        adjustInsetsForBottomNavigation(binding.btnSetting)
        checkUpdate()
//        showNative(AdManager.NATIVE_HOME, binding.nativeAdsView)
    }
    private val mainViewModel by activityViewModel<MainViewModel>()
    override fun initListener() {

        binding.btnSetting.singleClick {
            navigate(R.id.settingFragment)
        }
        binding.btnAvatar.singleClick {
            mainViewModel.markResetOnNextEnter()
            val action = HomeFragmentDirections.actionHomeFragmentToCategoryFragment(
                categoryId = -1,
                fromHome = true
            )
            findNavController().navigate(action)
        }
        binding.btnGallery.singleClick {
            navigate(R.id.galleryFragment)
        }

        onBackPressed {
            tracking(HOME_CLICK_BACK)
            tracking(EXIT_DIALOG_SHOW)
            ExitsAppDialog(requireActivity()).also { dialog ->
                dialog.show(this) {
                    requireActivity().finish()
                }
            }
        }
    }

    override fun initData() {

    }

    private fun checkUpdate() {
        activity?.let {
            UpdateAppUtils.checkUpdateApp(it)
        }
    }
}
