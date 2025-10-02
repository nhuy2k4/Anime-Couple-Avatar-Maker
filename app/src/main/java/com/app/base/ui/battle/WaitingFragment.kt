package com.app.base.ui.battle

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.app.base.R
import com.app.base.databinding.FragmentWaitingBinding
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.navigate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WaitingFragment : BaseFragment<FragmentWaitingBinding, WaitingViewModel>() {

    override fun initView() {
        // Khởi chạy countdown khi view đã ready
        startCountdown()
    }

    override fun initData() {}
    override fun initListener() {}

    private fun startCountdown() {
        lifecycleScope.launch {
            var count = 3
            binding.countdownText.text = count.toString()

            while (count > 0) {
                // hiệu ứng scale/ngắn khi đổi số (tuỳ chọn, cho đẹp)
                animateCountBounce()
                delay(1000)
                count--
                binding.countdownText.text = count.toString()
            }

            // đợi 1 frame để số 0 hiển thị, sau đó ẩn startup và hiển thị bot với animation
            delay(300)
            binding.startup.visibility = View.GONE

            // Chọn 1 trong 2 cách bên dưới (uncomment 1 cách bạn muốn)
            showBotPlayerWithPropertyAnimation()
            // showBotPlayerWithSlideTransition()
            delay(1000)
            navigate(R.id.battleFragment)
        }
    }

    private fun animateCountBounce() {
        binding.countdownText.scaleX = 1f
        binding.countdownText.scaleY = 1f
        binding.countdownText.animate()
            .scaleX(1.3f).scaleY(1.3f)
            .setDuration(120)
            .withEndAction {
                binding.countdownText.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(120)
                    .start()
            }.start()
    }

    private fun showBotPlayerWithPropertyAnimation() {
        // đặt translationX sang phải ngoài màn hình
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        binding.botPlayer.translationX = screenWidth
        binding.botPlayer.visibility = View.VISIBLE

        // animate về vị trí
        binding.botPlayer.animate()
            .translationX(0f)
            .setDuration(450)
            .withStartAction {
                // tuỳ chọn: fade in
                binding.botPlayer.alpha = 0f
                binding.botPlayer.animate().alpha(1f).setDuration(300).start()
            }
            .start()
    }

}
