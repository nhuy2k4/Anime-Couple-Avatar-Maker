package com.app.base.ui.dialog

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.app.base.R

class DailyRewardDialog(
    context: Context,
    private val todayIndex: Int,
    private val claimedDays: Set<Int>,
    private val onClaim: ((Int) -> Unit)? = null
) : Dialog(context) {

    private var _grid: GridLayout? = null
    private var isClaimed = false

    private val rewards = listOf(
        DailyReward(
            day = 1,
            icon = R.drawable.reward,
            label = "Day 1",
            type = "hair",
            value = 1
        ),
        DailyReward(
            day = 2,
            icon = R.drawable.reward,
            label = "Day 2",
            type = "hair",
            value = 2
        ),
        DailyReward(
            day = 3,
            icon = R.drawable.reward,
            label = "Day 3",
            type = "hair",
            value = 3
        ),
        DailyReward(
            day = 4,
            icon = R.drawable.reward,
            label = "Day 4",
            type = "hair",
            value = 4
        ),
        DailyReward(
            day = 5,
            icon = R.drawable.reward,
            label = "Day 5",
            type = "eye",
            value = 1
        ),
        DailyReward(
            day = 6,
            icon = R.drawable.reward,
            label = "Day 6",
            type = "eye",
            value = 3
        ),
        DailyReward(
            day = 7,
            icon = R.drawable.reward,
            label = "Day 7",
            type = "eye",
            value = 2
        )
    )

    init {
        try {
            setupDialog()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing dialog", e)
            dismiss()
        }
    }

    private fun setupDialog() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_daily_reward, null)
        setContentView(view)
        setCancelable(true)
        setCanceledOnTouchOutside(false)

        setupGrid(view)
        setupButtons(view)
    }

    private fun setupGrid(view: View) {
        try {
            _grid = view.findViewById<GridLayout>(R.id.gridRewards).apply {
                removeAllViews()
                rewards.forEachIndexed { index, reward ->
                    addRewardItem(this, index, reward)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up grid", e)
        }
    }

    private fun addRewardItem(grid: GridLayout, index: Int, reward: DailyReward) {
        try {
            val itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_daily_reward, grid, false)

            val icon = itemView.findViewById<ImageView>(R.id.imgReward)
            val label = itemView.findViewById<TextView>(R.id.tvDay)
            val check = itemView.findViewById<ImageView>(R.id.imgCheck)

            icon.setImageResource(reward.icon)
            label.text = if (index == todayIndex) "Today" else reward.label

            check.visibility = if (claimedDays.contains(index)) {
                View.VISIBLE
            } else {
                View.GONE
            }

            if (index == todayIndex) {
                itemView.setBackgroundResource(R.drawable.bg_gradient)
            }

            grid.addView(itemView)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding reward item", e)
        }
    }

    private fun setupButtons(view: View) {
        try {
            view.findViewById<Button>(R.id.btnClaim).apply {
                isEnabled = !isClaimed && !claimedDays.contains(todayIndex)
                setOnClickListener {
                    handleClaim()
                }
            }

            view.findViewById<ImageButton>(R.id.btnClose).apply {
                setOnClickListener {
                    dismiss()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up buttons", e)
        }
    }

    private fun handleClaim() {
        if (!isClaimed && !claimedDays.contains(todayIndex)) {
            try {
                isClaimed = true
                onClaim?.invoke(todayIndex)
                updateRewardVisibility()
                dismiss()
            } catch (e: Exception) {
                Log.e(TAG, "Error handling claim", e)
            }
        }
    }

    private fun updateRewardVisibility() {
        try {
            _grid?.getChildAt(todayIndex)?.findViewById<ImageView>(R.id.imgCheck)?.apply {
                visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating reward visibility", e)
        }
    }

    override fun dismiss() {
        super.dismiss()
        cleanup()
    }

    private fun cleanup() {
        _grid = null
    }

    companion object {
        private const val TAG = "DailyRewardDialog"
    }
}

data class DailyReward(
    val day: Int,
    val icon: Int,
    val label: String,
    val type: String,
    val value: Int
)
