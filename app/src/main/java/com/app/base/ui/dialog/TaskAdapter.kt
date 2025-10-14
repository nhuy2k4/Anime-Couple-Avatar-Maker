package com.app.base.ui.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.base.R
import com.app.base.database.entity.MissionEntity

class TaskAdapter(
    private val onGoNowClick: (MissionEntity) -> Unit,
    private val onClaimClick: (MissionEntity) -> Unit
) : ListAdapter<MissionEntity, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgReward: ImageView = itemView.findViewById(R.id.imgReward)
        private val tvRewardAmount: TextView = itemView.findViewById(R.id.tvRewardAmount)
        private val tvTaskDesc: TextView = itemView.findViewById(R.id.tvTaskDesc)
        private val btnGoNow: Button = itemView.findViewById(R.id.btnGoNow)
        private val progressTask: ProgressBar = itemView.findViewById(R.id.progressTask)
        private val tvTaskProgress: TextView = itemView.findViewById(R.id.tvTaskProgress)

        fun bind(mission: MissionEntity) {
            // Set reward icon based on type
            imgReward.setImageResource(
                when (mission.rewardType) {
                    "DIAMOND" -> R.drawable.ic_diamond
                    "FEATURE" -> R.drawable.ic_gift
                    else -> R.drawable.ic_gift
                }
            )

            // Set reward amount
            tvRewardAmount.text = "x${mission.rewardAmount}"

            // Set task description
            tvTaskDesc.text = mission.title

            // Set progress
            progressTask.max = mission.target
            progressTask.progress = mission.progress
            tvTaskProgress.text = "${mission.progress}/${mission.target}"

            // Set button state and text based on mission status
            when (mission.status) {
                "IN_PROGRESS" -> {
                    btnGoNow.text = "Go Now"
                    btnGoNow.isEnabled = true
                    btnGoNow.setBackgroundResource(R.drawable.bg_btn_go_now)
                    btnGoNow.setOnClickListener { onGoNowClick(mission) }
                }
                "COMPLETED" -> {
                    btnGoNow.text = "Claim"
                    btnGoNow.isEnabled = true
                    btnGoNow.setBackgroundResource(R.drawable.bg_claim_button)
                    btnGoNow.setOnClickListener { onClaimClick(mission) }
                }
                "CLAIMED" -> {
                    btnGoNow.text = "Claimed"
                    btnGoNow.isEnabled = false
                    btnGoNow.setBackgroundResource(R.drawable.bg_btn_claimed)
                    btnGoNow.setOnClickListener(null)
                }
                else -> {
                    btnGoNow.text = "Locked"
                    btnGoNow.isEnabled = false
                    btnGoNow.setBackgroundResource(R.drawable.bg_btn_locked)
                    btnGoNow.setOnClickListener(null)
                }
            }

            // Update item background based on completion status
            itemView.setBackgroundResource(
                when (mission.status) {
                    "COMPLETED" -> R.drawable.bg_task_item_completed
                    "CLAIMED" -> R.drawable.bg_task_item_claimed
                    else -> R.drawable.bg_task_item
                }
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // Method for TaskDialog compatibility
    fun updateMissions(newMissions: List<MissionEntity>) {
        submitList(newMissions)
    }

    private class TaskDiffCallback : DiffUtil.ItemCallback<MissionEntity>() {
        override fun areItemsTheSame(oldItem: MissionEntity, newItem: MissionEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MissionEntity, newItem: MissionEntity): Boolean {
            return oldItem == newItem
        }
    }
}
