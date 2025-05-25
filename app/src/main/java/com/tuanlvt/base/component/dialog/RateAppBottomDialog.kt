package com.tuanlvt.base.component.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.brally.mobile.base.activity.BaseBottomSheetFragment
import com.brally.mobile.utils.Common
import com.brally.mobile.utils.singleClick
import com.language_onboard.utils.gone
import com.tuanlvt.base.R
import com.tuanlvt.base.databinding.DialogRateAppBinding

class RateAppBottomDialog : BaseBottomSheetFragment<DialogRateAppBinding>() {
    override fun getVB(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogRateAppBinding {
        return DialogRateAppBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        binding.apply {
            ratingBar.onRateListener = { star ->
                btnSubmit.apply {
                    text = if (star > 4) {
                        etdFeedbackLayout.gone()
                        tvFeedbackSize.gone()
                        getString(R.string.rate_us)
                    } else {
                        etdFeedbackLayout.isVisible = true
                        tvFeedbackSize.apply {
                            isVisible = true
                            text = String.format(getString(R.string.max_size_feedback_text), 0)
                        }
                        getString(R.string.feed_back)
                    }
                    btnSubmit.isVisible = true
                    btnSubmit.isEnabled = true
                }
            }

            btnSubmit.singleClick {
                rateApp()
            }

            etdFeedback.doAfterTextChanged {
                tvFeedbackSize.apply {
                    isVisible = true
                    text =
                        String.format(
                            getString(R.string.max_size_feedback_text),
                            it.toString().length
                        )
                }
            }
        }
    }

    private fun rateApp() {
        val star = binding.ratingBar.rating.toInt()
        if (star > 3) {
//            activity?.let { Common.openAppInStore(it, it.packageName) }
        } else {
//            FeedbackSdk.sendFeedback(binding.etdFeedback.text.toString())
        }
        dialog?.dismiss()
    }

    companion object {
        const val TAG = "RateAppBottomSheet"
        fun getInstance(): RateAppBottomDialog {
            val bottomSheet = RateAppBottomDialog()
            return bottomSheet
        }
    }
}
