package com.app.base.ui.battle

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.app.base.R
import com.app.base.databinding.FragmentWaitingBinding
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.navigate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WaitingFragment : BaseFragment<FragmentWaitingBinding, WaitingViewModel>() {

    override fun initView() {
        startCountdown()
    }

    override fun initListener() {

    }

    override fun initData() {

    }

    private fun startCountdown() {
        lifecycleScope.launch {
            var count = 3
            binding.countdownText.text = count.toString()

            while (count > 0) {
                animateCountBounce()
                delay(1000)
                count--
                binding.countdownText.text = if (count > 0) count.toString() else "GO!"
            }

            delay(500)
            binding.startup.visibility = View.GONE

            // Hiển thị bot + player (animation khởi đầu)
            showBotPlayerWithPropertyAnimation()
            delay(1000)

            // ✅ Lấy dữ liệu từ arguments (được truyền từ CategoryFragment)
            val outfitJson = arguments?.getString("outfitJson", "{}") ?: "{}"
            val botId = arguments?.getInt("botId", -1) ?: -1  // Thay đổi từ "selectedBotId" thành "botId"
            val backgroundId = arguments?.getString("backgroundId", "") ?: ""
            val mode = arguments?.getString("mode", "B") ?: "B"

            // ✅ Chuẩn bị bundle gửi sang BattleFragment
            val battleBundle = Bundle().apply {
                putString("outfitJson", outfitJson)   // player JSON
                putInt("botId", botId)        // bot ID (để load từ DB) - Thay đổi từ "selectedBotId" thành "botId"
                putString("backgroundId", backgroundId)
                putString("mode", mode)
            }

            // ✅ Chuyển sang BattleFragment
            navigate(R.id.battleFragment, battleBundle)
        }
    }

    private fun animateCountBounce() {
        binding.countdownText.scaleX = 1f
        binding.countdownText.scaleY = 1f
        binding.countdownText.animate()
            .scaleX(1.3f).scaleY(1.3f)
            .setDuration(120)
            .withEndAction {
                binding.countdownText.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }.start()
    }

    private fun showBotPlayerWithPropertyAnimation() {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        binding.botPlayer.translationX = screenWidth
        binding.botPlayer.visibility = View.VISIBLE
        binding.botPlayer.animate().translationX(0f).setDuration(450).withStartAction {
            binding.botPlayer.alpha = 0f
            binding.botPlayer.animate().alpha(1f).setDuration(300).start()
        }.start()
    }
}
