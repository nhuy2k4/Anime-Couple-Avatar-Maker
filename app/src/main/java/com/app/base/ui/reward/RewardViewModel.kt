//package com.app.base.ui.reward
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.app.base.database.entity.PlayerRewardEntity
//import com.app.base.database.entity.PlayerItemEntity
//import com.app.base.database.model.RewardData
//import com.app.base.database.repository.RewardRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class RewardViewModel @Inject constructor(
//    private val rewardRepository: RewardRepository
//) : ViewModel() {
//
//    private val _playerDiamonds = MutableLiveData<Int>()
//    val playerDiamonds: LiveData<Int> = _playerDiamonds
//
//    private val _dailyCheckInAvailable = MutableLiveData<Boolean>()
//    val dailyCheckInAvailable: LiveData<Boolean> = _dailyCheckInAvailable
//
//    private val _activeMissions = MutableLiveData<List<PlayerRewardEntity>>()
//    val activeMissions: LiveData<List<PlayerRewardEntity>> = _activeMissions
//
//    private val _completedMissions = MutableLiveData<List<PlayerRewardEntity>>()
//    val completedMissions: LiveData<List<PlayerRewardEntity>> = _completedMissions
//
//    private val _playerItems = MutableLiveData<List<PlayerItemEntity>>()
//    val playerItems: LiveData<List<PlayerItemEntity>> = _playerItems
//
//    private val _rewardClaimResult = MutableLiveData<Result<RewardData>>()
//    val rewardClaimResult: LiveData<Result<RewardData>> = _rewardClaimResult
//
//    private val _loading = MutableLiveData<Boolean>()
//    val loading: LiveData<Boolean> = _loading
//
//    private var currentPlayerId: Int = 1 // Default player ID, should be set from session
//
//    fun setPlayerId(playerId: Int) {
//        currentPlayerId = playerId
//        loadPlayerData()
//    }
//
//    fun loadPlayerData() {
//        viewModelScope.launch {
//            _loading.value = true
//            try {
//                // Initialize missions for new players
//                rewardRepository.initializeMissions(currentPlayerId)
//
//                // Load all player data
//                loadPlayerDiamonds()
//                loadDailyCheckInStatus()
//                loadMissions()
//                loadPlayerItems()
//            } finally {
//                _loading.value = false
//            }
//        }
//    }
//
//    private suspend fun loadPlayerDiamonds() {
//        _playerDiamonds.value = rewardRepository.getPlayerDiamonds(currentPlayerId)
//    }
//
//    private suspend fun loadDailyCheckInStatus() {
//        _dailyCheckInAvailable.value = rewardRepository.canClaimDailyCheckIn(currentPlayerId)
//    }
//
//    private suspend fun loadMissions() {
//        _activeMissions.value = rewardRepository.getActiveMissions(currentPlayerId)
//        _completedMissions.value = rewardRepository.getCompletedMissions(currentPlayerId)
//    }
//
//    private suspend fun loadPlayerItems() {
//        _playerItems.value = rewardRepository.getPlayerItems(currentPlayerId)
//    }
//
//    // === DAILY CHECK-IN ===
//    fun claimDailyCheckIn() {
//        viewModelScope.launch {
//            _loading.value = true
//            val result = rewardRepository.claimDailyCheckIn(currentPlayerId)
//            _rewardClaimResult.value = result
//
//            if (result.isSuccess) {
//                loadPlayerDiamonds()
//                loadDailyCheckInStatus()
//                loadPlayerItems()
//            }
//            _loading.value = false
//        }
//    }
//
//    // === MISSION SYSTEM ===
//    fun claimMissionReward(rewardId: Long) {
//        viewModelScope.launch {
//            _loading.value = true
//            val result = rewardRepository.claimReward(currentPlayerId, rewardId)
//            _rewardClaimResult.value = result
//
//            if (result.isSuccess) {
//                loadPlayerDiamonds()
//                loadMissions()
//                loadPlayerItems()
//            }
//            _loading.value = false
//        }
//    }
//
//    // === AD REWARD ===
//    fun claimAdReward() {
//        viewModelScope.launch {
//            _loading.value = true
//            val result = rewardRepository.claimAdReward(currentPlayerId)
//            _rewardClaimResult.value = result
//
//            if (result.isSuccess) {
//                loadPlayerDiamonds()
//                // Update watch ad mission progress
//                rewardRepository.increaseMissionProgress(currentPlayerId, com.app.base.database.model.MissionType.WATCH_ADS)
//                loadMissions()
//            }
//            _loading.value = false
//        }
//    }
//
//    // === PK BOT REWARD ===
//    fun claimPkBotReward(isWin: Boolean) {
//        viewModelScope.launch {
//            _loading.value = true
//            val result = rewardRepository.claimPkBotReward(currentPlayerId, isWin)
//            _rewardClaimResult.value = result
//
//            if (result.isSuccess) {
//                loadPlayerDiamonds()
//                loadMissions() // Refresh missions to show updated progress
//            }
//            _loading.value = false
//        }
//    }
//
//    // === UTILITY METHODS ===
//    fun hasPlayerItem(itemId: String, itemType: String): Boolean {
//        return _playerItems.value?.any { it.itemId == itemId && it.itemType == itemType } ?: false
//    }
//
//    fun getPlayerItemsByType(itemType: String): List<PlayerItemEntity> {
//        return _playerItems.value?.filter { it.itemType == itemType } ?: emptyList()
//    }
//
//    fun refreshData() {
//        loadPlayerData()
//    }
//
//    // Called when player logs in
//    fun onPlayerLogin() {
//        viewModelScope.launch {
//            rewardRepository.increaseMissionProgress(currentPlayerId, com.app.base.database.model.MissionType.LOGIN_DAYS)
//            loadMissions()
//        }
//    }
//
//    // Called when player wins/plays a match
//    fun onMatchCompleted(isWin: Boolean) {
//        // PK bot reward is claimed separately through claimPkBotReward
//        // This is for tracking other types of matches
//        viewModelScope.launch {
//            if (isWin) {
//                rewardRepository.increaseMissionProgress(currentPlayerId, com.app.base.database.model.MissionType.WIN_MATCHES)
//            }
//            rewardRepository.increaseMissionProgress(currentPlayerId, com.app.base.database.model.MissionType.PLAY_MATCHES)
//            loadMissions()
//        }
//    }
//
//    // Called when player spends diamonds
//    fun onDiamondsSpent(amount: Int) {
//        viewModelScope.launch {
//            rewardRepository.increaseMissionProgress(currentPlayerId, com.app.base.database.model.MissionType.SPEND_DIAMONDS, amount)
//            loadPlayerDiamonds()
//            loadMissions()
//        }
//    }
//}
