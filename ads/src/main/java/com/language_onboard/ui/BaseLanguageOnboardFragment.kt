package com.language_onboard.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.braly.ads.R
import com.language_onboard.utils.hideNavBar

abstract class BaseLanguageOnboardFragment<T : ViewBinding> : BaseFragment<T>() {
    abstract fun shouldHideStatusBar(): Boolean

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (shouldHideStatusBar()) {
            hideNavBar()
            _binding?.root?.findViewById<View>(R.id.main)?.let {
                adjustInsetsForBottomNavigation(it)
            }
        } else {
            setSystemBarsColor()
        }
        initView()
        initObserver()
    }


    fun adjustInsetsForBottomNavigation(viewBottom: View) {
        ViewCompat.setOnApplyWindowInsetsListener(viewBottom) { view, insets ->
            try {
                val params = view.layoutParams as ViewGroup.MarginLayoutParams
                val displayCutout =
                    insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars())
                params.topMargin = (displayCutout.top).toInt()
                view.layoutParams = params
            } catch (e: Exception) {
                e.printStackTrace()
            }
            insets
        }
    }
}