//package com.app.base.ui.reward
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.Observer
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.app.base.databinding.FragmentRewardBinding
//import com.app.base.ui.reward.adapter.MissionAdapter
//import com.app.base.database.model.RewardData
//import dagger.hilt.android.AndroidEntryPoint
//
//@AndroidEntryPoint
//class RewardFragment : Fragment() {
//
//    private var _binding: FragmentRewardBinding? = null
//    private val binding get() = _binding!!
//
//    private val viewModel: RewardViewModel by viewModels()
//    private lateinit var missionAdapter: MissionAdapter
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        _binding = FragmentRewardBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        setupRecyclerView()
//        setupClickListeners()
//        observeViewModel()
//
//        // Load data for default player (you should set actual player ID)
//        viewModel.setPlayerId(1)
//    }
//
//    private fun setupRecyclerView() {
//        missionAdapter = MissionAdapter { rewardId ->
//            viewModel.claimMissionReward(rewardId)
//        }
//
//        binding.rvMissions.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = missionAdapter
//        }
//    }
//
//    private fun setupClickListeners() {
//        binding.apply {
//            btnDailyCheckIn.setOnClickListener {
//                viewModel.claimDailyCheckIn()
//            }
//
//            btnWatchAd.setOnClickListener {
//                // In real app, show ad first, then claim reward
//                showAd {
//                    viewModel.claimAdReward()
//                }
//            }
//
//            btnRefresh.setOnClickListener {
//                viewModel.refreshData()
//            }
//        }
//    }
//
//    private fun observeViewModel() {
//        viewModel.playerDiamonds.observe(viewLifecycleOwner) { diamonds ->
//            binding.tvDiamondCount.text = diamonds.toString()
//        }
//
//        viewModel.dailyCheckInAvailable.observe(viewLifecycleOwner) { available ->
//            binding.btnDailyCheckIn.isEnabled = available
//            binding.btnDailyCheckIn.text = if (available) "Claim Daily Reward" else "Already Claimed Today"
//        }
//
//        viewModel.activeMissions.observe(viewLifecycleOwner) { missions ->
//            missionAdapter.submitList(missions)
//            binding.tvMissionCount.text = "Active Missions: ${missions.size}"
//        }
//
//        viewModel.completedMissions.observe(viewLifecycleOwner) { missions ->
//            val completedCount = missions.filter { it.status == "COMPLETED" }.size
//            binding.tvCompletedCount.text = "Ready to Claim: $completedCount"
//        }
//
//        viewModel.rewardClaimResult.observe(viewLifecycleOwner) { result ->
//            result.fold(
//                onSuccess = { reward ->
//                    showRewardClaimedDialog(reward)
//                },
//                onFailure = { error ->
//                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
//                }
//            )
//        }
//
//        viewModel.loading.observe(viewLifecycleOwner) { loading ->
//            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
//        }
//
//        viewModel.playerItems.observe(viewLifecycleOwner) { items ->
//            val featureCount = items.filter { it.itemType == "FEATURE" }.size
//            val backgroundCount = items.filter { it.itemType == "BACKGROUND" }.size
//            val avatarCount = items.filter { it.itemType == "AVATAR" }.size
//
//            binding.tvItemStats.text = "Features: $featureCount | Backgrounds: $backgroundCount | Avatars: $avatarCount"
//        }
//    }
//
//    private fun showRewardClaimedDialog(reward: RewardData) {
//        val message = when (reward.type) {
//            "DIAMOND" -> "You received ${reward.amount} diamonds!"
//            "FEATURE" -> "You received a new feature!"
//            "BACKGROUND" -> "You received a new background!"
//            "AVATAR" -> "You received a new avatar!"
//            else -> "You received a reward!"
//        }
//
//        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
//
//        // You can create a custom dialog here for better UX
//    }
//
//    private fun showAd(onAdCompleted: () -> Unit) {
//        // Simulate ad watching - replace with actual ad implementation
//        Toast.makeText(requireContext(), "Showing Ad...", Toast.LENGTH_SHORT).show()
//
//        // Simulate ad completion after 2 seconds
//        binding.root.postDelayed({
//            onAdCompleted()
//        }, 2000)
//    }
//
//    // Call this method when player completes a PK match
//    fun onPkMatchCompleted(isWin: Boolean) {
//        viewModel.claimPkBotReward(isWin)
//    }
//
//    // Call this method when player logs in
//    fun onPlayerLogin() {
//        viewModel.onPlayerLogin()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}
