package com.language_onboard.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.braly.ads.R

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    var _binding: T? = null
    val binding: T get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = getViewBinding()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSystemBarsColor()
        initView()
        initObserver()
    }

    open fun initView() = Unit

    open fun initObserver() = Unit

    abstract fun getViewBinding(): T

    open fun getStatusBarColor() = resources.getColor(R.color.white, null)

    open fun getNavigationBarColor() = resources.getColor(R.color.white, null)

    open fun setSystemBarsColor() {
        activity?.window?.statusBarColor = getStatusBarColor()
        activity?.window?.navigationBarColor = getNavigationBarColor()
    }
}