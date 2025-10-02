package com.app.base.core.helper

import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.app.base.R
import android.util.Log
class ScoreEffectHelper(
    private val container: ViewGroup,
    private val topBonusContainer: View
) {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var loopRunnable: Runnable

    /**
     * Loop hiển thị điểm + tim, cập nhật topBonusScore và tvTop
     * tvTopScore: TextView để hiển thị +N
     * topBonusScore: TextView hiển thị điểm bonus (chỉ số hiện thị)
     * tvTop: TextView hiển thị label (Bonus, Pretty, Gorgeous, Perfect)
     */
    fun startLoop(tvTopScore: TextView, topBonusScore: TextView, tvTop: TextView, interval: Long = 2500L) {
        loopRunnable = object : Runnable {
            override fun run() {
                val points = generateRandomBonus(tvTop) // random + set label
                topBonusScore.text = "+$points"        // cập nhật topBonusScore
                showTopBonusOnce(tvTopScore)
                showScoreBonus(tvTopScore, points)
                handler.postDelayed(this, interval)
            }
        }
        handler.postDelayed(loopRunnable, interval)
    }

    fun stopLoop() {
        if (::loopRunnable.isInitialized) handler.removeCallbacks(loopRunnable)
    }

    /** +N bay lên trên tvTopScore */
    fun showScoreBonus(tvTopScore: TextView, bonusValue: Int) {
        Handler(Looper.getMainLooper()).postDelayed({
            val plusOne = TextView(container.context).apply {
                text = "+$bonusValue"
                textSize = 20f
                setTextColor(tvTopScore.currentTextColor)
                x = tvTopScore.x + tvTopScore.width / 2f - 20f
                y = tvTopScore.y - 50f
            }
            container.addView(plusOne)

            plusOne.animate()
                .translationYBy(-100f)
                .alpha(0f)
                .setDuration(800)
                .withEndAction { container.removeView(plusOne) }
                .start()

            val current = tvTopScore.text.toString().replace("+", "").toIntOrNull() ?: 0
            tvTopScore.text = "${current + bonusValue}"
        }, 200) // delay 200ms
    }

    /** +1 khi click heartPoint */
    fun showClickBonus(tvTopScore: TextView, heartPoint: View, bonusValue: Int) {
        // Lấy vị trí start từ heartPoint (so với container)
        val startLoc = IntArray(2)
        heartPoint.getLocationOnScreen(startLoc)
        val containerLoc = IntArray(2)
        container.getLocationOnScreen(containerLoc)

        val startX = startLoc[0] - containerLoc[0] + heartPoint.width / 2f - 30f
        val startY = startLoc[1] - containerLoc[1] + heartPoint.height / 2f - 30f

        // +N bay lên
        val plusOne = TextView(container.context).apply {
            text = "+$bonusValue"
            textSize = 20f
            setTextColor(tvTopScore.currentTextColor)
            x = startX
            y = startY - 100f
        }
        container.addView(plusOne)

        plusOne.animate()
            .translationYBy(-150f)
            .alpha(0f)
            .setDuration(800)
            .withEndAction { container.removeView(plusOne) }
            .start()

        // Vị trí kết thúc (tvTopScore)
        val endLoc = IntArray(2)
        tvTopScore.getLocationOnScreen(endLoc)
        val endX = endLoc[0] - containerLoc[0] + tvTopScore.width / 2f
        val endY = endLoc[1] - containerLoc[1] + tvTopScore.height / 2f

        // Tim bay
        val heart = ImageView(container.context).apply {
            setImageResource(R.drawable.ic_heart)
            layoutParams = ConstraintLayout.LayoutParams(60, 60)
            x = startX
            y = startY
        }
        container.addView(heart)

        animateHeart(heart, startX, startY, endX, endY) {
            // Cập nhật score khi tim bay xong
            val current = tvTopScore.text.toString().replace("+", "").toIntOrNull() ?: 0
            tvTopScore.text = "${current + bonusValue}"
        }
        // Inside showClickBonus
        Log.d("ScoreEffectHelper", "Heart startX: $startX, startY: $startY")
        Log.d("ScoreEffectHelper", "Heart endX: $endX, endY: $endY")
    }



    /** Tim bay từ topBonusContainer đến tvTopScore */
    fun showTopBonusOnce(tvTopScore: TextView) {
        topBonusContainer.visibility = View.VISIBLE

        val loc = IntArray(2)
        topBonusContainer.getLocationOnScreen(loc)
        val containerLoc = IntArray(2)
        container.getLocationOnScreen(containerLoc)

        val startX = loc[0] - containerLoc[0] + topBonusContainer.width / 2f - 40f
        val startY = loc[1] - containerLoc[1] + topBonusContainer.height / 2f - 40f

        val endX = tvTopScore.x + tvTopScore.width / 2f - 40f
        val endY = tvTopScore.y + tvTopScore.height / 2f - 40f

        val heart = ImageView(container.context).apply {
            setImageResource(R.drawable.ic_heart)
            layoutParams = ConstraintLayout.LayoutParams(80, 80)
            x = startX
            y = startY
        }
        container.addView(heart)

        animateHeart(heart, startX, startY, endX, endY) {
            topBonusContainer.visibility = View.INVISIBLE
        }
        Log.d("ScoreEffectHelper", "Heart startX: $startX, startY: $startY")
        Log.d("ScoreEffectHelper", "Heart endX: $endX, endY: $endY")
    }

    /** Animate tim bay theo đường cong */
    private fun animateHeart(
        heart: ImageView,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        onEnd: () -> Unit
    ) {
        val path = android.graphics.Path().apply {
            moveTo(startX, startY)
            quadTo((startX + endX) / 2f, startY - 250f, endX, endY)
        }
        val pathMeasure = android.graphics.PathMeasure(path, false)
        val pos = FloatArray(2)

        ValueAnimator.ofFloat(0f, pathMeasure.length).apply {
            duration = 1300
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { anim ->
                val distance = anim.animatedValue as Float
                pathMeasure.getPosTan(distance, pos, null)
                heart.x = pos[0] - heart.width / 2f
                heart.y = pos[1] - heart.height / 2f
                heart.alpha = 1f - anim.animatedFraction
                heart.scaleX = 0.5f + 0.5f * (1 - anim.animatedFraction)
                heart.scaleY = 0.5f + 0.5f * (1 - anim.animatedFraction)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    (heart.parent as? ViewGroup)?.removeView(heart)
                    onEnd()
                }
            })
            start()
        }
    }

    /** Random điểm từ 40-100 và set label */
    fun generateRandomBonus(tvTop: TextView): Int {
        val points = (40..100).random()
        val label = when (points) {
            in 40..54 -> "Bonus"
            in 55..69 -> "Pretty"
            in 70..85 -> "Gorgeous"
            else -> "Perfect"
        }
        tvTop.text = label
        return points
    }
}
