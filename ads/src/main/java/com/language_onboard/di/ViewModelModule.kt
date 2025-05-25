package com.language_onboard.di

import com.language_onboard.ui.viewmodel.CommonOnboardingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val commonViewModelModule = module {
    viewModel { CommonOnboardingViewModel() }
}