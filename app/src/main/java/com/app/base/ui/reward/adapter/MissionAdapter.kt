//package com.app.base.ui.reward.adapter
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.app.base.databinding.ItemMissionBinding
//import com.app.base.database.entity.PlayerRewardEntity
//import com.app.base.database.model.RewardData
//import com.app.base.database.model.RewardStatus
//import com.google.gson.Gson
//
//class MissionAdapter(
//    private val onClaimClick: (Long) -> Unit
//) : ListAdapter<PlayerRewardEntity, MissionAdapter.MissionViewHolder>(MissionDiffCallback()) {
//
//    private val gson = Gson()
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
//        val binding = ItemMissionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return MissionViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
//        holder.bind(getItem(position))
//    }
//
//    inner class MissionViewHolder(
//        private val binding: ItemMissionBinding
//    ) : RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(mission: PlayerRewardEntity) {
//            binding.apply {
//                tvMissionTitle.text = mission.title
//                tvMissionDescription.text = mission.description
//
//                // Parse reward data
//                val rewardData = gson.fromJson(mission.rewardData, RewardData::class.java)
//                tvRewardText.text = when (rewardData.type) {
//                    "DIAMOND" -> "${rewardData.amount} Diamonds"
//                    "FEATURE" -> "New Feature"
//                    "BACKGROUND" -> "New Background"
//                    "AVATAR" -> "New Avatar"
//                    else -> "Reward"
//                }
//
//                // Progress bar
//                progressBar.max = mission.target
//                progressBar.progress = mission.progress
//                tvProgress.text = "${mission.progress}/${mission.target}"
//
//                // Claim button state
//                when (mission.status) {
//                    RewardStatus.LOCKED.name -> {
//                        btnClaim.isEnabled = false
//                        btnClaim.text = "Locked"
//                    }
//                    RewardStatus.AVAILABLE.name, RewardStatus.IN_PROGRESS.name -> {
//                        btnClaim.isEnabled = false
//                        btnClaim.text = "In Progress"
//                    }
//                    RewardStatus.COMPLETED.name -> {
//                        btnClaim.isEnabled = true
//                        btnClaim.text = "Claim"
//                        btnClaim.setOnClickListener { onClaimClick(mission.id) }
//                    }
//                    RewardStatus.CLAIMED.name -> {
//                        btnClaim.isEnabled = false
//                        btnClaim.text = "Claimed"
//                    }
//                }
//
//                // Progress percentage
//                val percentage = if (mission.target > 0) {
//                    (mission.progress * 100) / mission.target
//                } else 0
//                progressBar.progress = percentage
//            }
//        }
//    }
//
//    class MissionDiffCallback : DiffUtil.ItemCallback<PlayerRewardEntity>() {
//        override fun areItemsTheSame(oldItem: PlayerRewardEntity, newItem: PlayerRewardEntity): Boolean {
//            return oldItem.id == newItem.id
//        }
//
//        override fun areContentsTheSame(oldItem: PlayerRewardEntity, newItem: PlayerRewardEntity): Boolean {
//            return oldItem == newItem
//        }
//    }
//}
