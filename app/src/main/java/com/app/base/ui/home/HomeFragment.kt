package com.app.base.ui.home

import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.navigate
import com.brally.mobile.base.activity.onBackPressed
import com.brally.mobile.service.event.EXIT_DIALOG_SHOW
import com.brally.mobile.service.event.HOME_CLICK_BACK
import com.brally.mobile.service.event.HOME_SHOW
import com.brally.mobile.service.event.HOME_SHOW_0
import com.brally.mobile.utils.singleClick
import com.language_onboard.utils.tracking
import com.app.base.R
import com.app.base.component.dialog.ExitsAppDialog
import com.app.base.databinding.FragmentHomeBinding
import com.app.base.session.isFirstScene
import com.app.base.session.setFirstScene
import com.app.base.ui.category.CategoryFragmentArgs

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {
    override fun initView() {
        if (isFirstScene(clazz = this)) {
            setFirstScene(clazz = this, isFirst = false)
            tracking(HOME_SHOW_0)
        }
        tracking(HOME_SHOW)
        adjustInsetsForBottomNavigation(binding.imvSetting)
    }

    override fun initListener() {
        binding.imvMenuDraw.singleClick {
            navigate(R.id.categoryFragment, CategoryFragmentArgs(categoryId = 1).toBundle())
        }

        binding.imvMenuCollection.singleClick {
//            navigate(R.id.collectionFragment)
        }

        binding.imvSetting.singleClick {
            navigate(R.id.settingFragment)
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
}
