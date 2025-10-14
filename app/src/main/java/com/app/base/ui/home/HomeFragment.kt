package com.app.base.ui.home

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.app.base.R
import com.app.base.component.dialog.ExitsAppDialog
import com.app.base.databinding.FragmentHomeBinding
import com.app.base.database.repository.RewardRepository
import com.app.base.ui.dialog.DailyRewardDialog
import com.app.base.ui.dialog.SettingDialogFragment
import com.app.base.ui.dialog.TaskDialog
import com.app.base.ui.dialog.DialogRankingFragment
import com.app.base.ui.dialog.DialogProfileFragment
import com.app.base.ui.main.MainViewModel
import com.app.base.utils.PlayerManager
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.navigate
import com.brally.mobile.base.activity.onBackPressed
import com.brally.mobile.service.event.EXIT_DIALOG_SHOW
import com.brally.mobile.service.event.HOME_CLICK_BACK
import com.brally.mobile.utils.singleClick
import com.braly.analytics.notification.UpdateAppUtils
import com.language_onboard.utils.tracking
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    private lateinit var rewardRepository: RewardRepository
    private lateinit var playerManager: PlayerManager

    override fun initView() {
        adjustInsetsForBottomNavigation(binding.btnSetting)
        checkUpdate()
        initRewardSystem()
//        showNative(AdManager.NATIVE_HOME, binding.nativeAdsView)
    }

    private fun initRewardSystem() {
        rewardRepository = RewardRepository(requireContext())
        playerManager = PlayerManager.getInstance(requireContext())
        initializePlayerAndCheckRewards()
    }

    private fun initializePlayerAndCheckRewards() {
        lifecycleScope.launch {
            try {
                // Initialize player system first
                val playerId = playerManager.initializePlayerSystem()

                // Now check reward status
                checkRewardStatus(playerId)

            } catch (e: Exception) {
                // Handle error silently or log
                e.printStackTrace()
            }
        }
    }

    private fun checkRewardStatus(playerId: Int) {
        lifecycleScope.launch {
            try {
                // Check daily reward status
                val dailyStatus = rewardRepository.getDailyCheckInStatus(playerId)
                updateDailyRewardButton(dailyStatus.canClaim)

                // Initialize and check mission progress
                val missionProgress = rewardRepository.getMissionProgress(playerId)
                updateTaskButton(missionProgress.missions.any { it.status == "COMPLETED" })

                // Auto-complete login mission when opening home
                rewardRepository.updateMissionProgress(playerId, "LOGIN")

                // Update diamond display
                val diamonds = playerManager.getPlayerDiamonds()
                binding.tvDiamond.text = diamonds.toString()

            } catch (e: Exception) {
                // Handle error silently or log
                e.printStackTrace()
            }
        }
    }

    private fun updateDailyRewardButton(canClaim: Boolean) {
        if (canClaim) {
            // Thêm hiệu ứng hoặc badge để báo có thể claim
            binding.btnDaily.alpha = 1.0f
            // Có thể thêm animation hoặc glow effect
        } else {
            binding.btnDaily.alpha = 0.8f
        }
    }

    private fun updateTaskButton(hasCompletedMissions: Boolean) {
        if (hasCompletedMissions) {
            // Thêm hiệu ứng hoặc badge để báo có mission hoàn thành
            binding.btnTask.alpha = 1.0f
            // Có thể thêm animation hoặc glow effect
        } else {
            binding.btnTask.alpha = 0.8f
        }
    }

    private suspend fun getCurrentPlayerId(): Int {
        return playerManager.getCurrentPlayerId()
    }

    private val mainViewModel by activityViewModel<MainViewModel>()
    override fun initListener() {

        binding.btnSetting.singleClick {
            SettingDialogFragment.newInstance()
                .show(parentFragmentManager, "SettingDialog")
        }
        binding.btnAvatar.singleClick {
            mainViewModel.markResetOnNextEnter()
            val action = HomeFragmentDirections.actionHomeFragmentToCategoryFragment(
                fromHome = true
            )
            findNavController().navigate(action)
        }
        // Dialog bindings
        binding.btnProfile.singleClick {
            DialogProfileFragment().show(parentFragmentManager, "DialogProfile")
        }
        binding.btnTask.singleClick {
            showTaskDialog()
        }
        binding.btnDaily.singleClick {
            showDailyRewardDialog()
        }
        binding.btnRank.singleClick {
            DialogRankingFragment().show(parentFragmentManager, "DialogRanking")
        }
        binding.btnCosplay.singleClick {
            navigate(R.id.cosplayFragment)
        }
        binding.btnGallery.singleClick {
            navigate(R.id.galleryFragment)
        }
        binding.btnPkBattle.singleClick {
            navigate(R.id.arenaSelectFragment)
        }
        onBackPressed {
            tracking(HOME_CLICK_BACK)
            tracking(EXIT_DIALOG_SHOW)
            ExitsAppDialog(requireActivity()).also { dialog ->
                dialog.show(this) {
                    requireActivity().finish()
                }
            }
        }
    }

    private fun showDailyRewardDialog() {
        lifecycleScope.launch {
            try {
                val playerId = getCurrentPlayerId()
                val dailyStatus = rewardRepository.getDailyCheckInStatus(playerId)

                val dialog = DailyRewardDialog(
                    context = requireContext(),
                    todayIndex = dailyStatus.currentDay - 1, // Convert to 0-based index
                    claimedDays = dailyStatus.claimedDays,
                    onClaim = { day ->
                        lifecycleScope.launch {
                            rewardRepository.claimDailyReward(playerId)
                            checkRewardStatus(playerId) // Refresh UI
                        }
                    }
                )

                dialog.show()

                // Refresh status after dialog is dismissed
                dialog.setOnDismissListener {
                    lifecycleScope.launch {
                        checkRewardStatus(playerId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showTaskDialog() {
        lifecycleScope.launch {
            try {
                val playerId = getCurrentPlayerId()

                val dialog = TaskDialog(
                    context = requireContext(),
                    playerId = playerId,
                    lifecycleOwner = viewLifecycleOwner,
                    viewModelStoreOwner = this@HomeFragment,
                    onGoToAction = { actionType ->
                        handleTaskAction(actionType)
                    }
                )

                dialog.show()

                // Refresh status after dialog is dismissed
                dialog.setOnDismissListener {
                    lifecycleScope.launch {
                        checkRewardStatus(playerId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleTaskAction(actionType: String) {
        when (actionType) {
            "EDITOR" -> {
                // Chuyển đến avatar editor
                binding.btnAvatar.performClick()
            }
            "PK_MODE" -> {
                // Chuyển đến PK Battle
                binding.btnPkBattle.performClick()
            }
            "WATCH_AD" -> {
                // Hiển thị quảng cáo reward
                showRewardedAd()
            }
        }
    }

    private fun showRewardedAd() {
        // Logic hiển thị quảng cáo
        // TODO: Thay bằng logic quảng cáo thực tế của bạn

        // Giả lập xem ad thành công - thay bằng callback thực của ad
        // Sau khi xem xong ad, update mission progress:
        lifecycleScope.launch {
            try {
                val playerId = getCurrentPlayerId()
                rewardRepository.updateMissionProgress(playerId, "WATCH_AD")
                checkRewardStatus(playerId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Public methods để các fragment khác có thể gọi để update mission progress
    fun onPhotoSaved() {
        lifecycleScope.launch {
            try {
                val playerId = getCurrentPlayerId()
                rewardRepository.updateMissionProgress(playerId, "SAVE_PHOTO")
                checkRewardStatus(playerId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onMatchPlayed() {
        lifecycleScope.launch {
            try {
                val playerId = getCurrentPlayerId()
                rewardRepository.updateMissionProgress(playerId, "PLAY_MATCH")
                checkRewardStatus(playerId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onMatchWon() {
        lifecycleScope.launch {
            try {
                val playerId = getCurrentPlayerId()
                rewardRepository.updateMissionProgress(playerId, "WIN_MATCH")
                checkRewardStatus(playerId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun initData() {

    }

    private fun checkUpdate() {
        activity?.let {
            UpdateAppUtils.checkUpdateApp(it)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh diamond count and reward status when returning to home
        if (::playerManager.isInitialized) {
            lifecycleScope.launch {
                try {
                    val playerId = getCurrentPlayerId()
                    checkRewardStatus(playerId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
