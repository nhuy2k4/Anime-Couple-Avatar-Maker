package com.app.base.ui.dialog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.base.database.repository.ClaimResult
import com.app.base.database.repository.MissionProgress
import com.app.base.database.repository.RewardRepository
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RewardRepository(application)

    private val _missionProgress = MutableLiveData<MissionProgress>()
    val missionProgress: LiveData<MissionProgress> = _missionProgress

    // Change to MutableLiveData<ClaimResult?> to allow null values
    private val _claimResult = MutableLiveData<ClaimResult?>()
    val claimResult: LiveData<ClaimResult?> = _claimResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadMissions(playerId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val progress = repository.getMissionProgress(playerId)
                _missionProgress.value = progress
            } catch (e: Exception) {
                _claimResult.value = ClaimResult.Error(e.message ?: "Error loading missions")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun claimMissionReward(playerId: Int, missionId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.claimMissionReward(playerId, missionId)
                _claimResult.value = result

                // Reload missions after claiming
                if (result is ClaimResult.Success) {
                    loadMissions(playerId)
                }
            } catch (e: Exception) {
                _claimResult.value = ClaimResult.Error(e.message ?: "Error claiming reward")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun claimAllMissionRewards(playerId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.claimAllMissionRewards(playerId)
                _claimResult.value = result

                // Reload missions after claiming all
                if (result is ClaimResult.Success) {
                    loadMissions(playerId)
                }
            } catch (e: Exception) {
                _claimResult.value = ClaimResult.Error(e.message ?: "Error claiming all rewards")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateMissionProgress(playerId: Int, missionType: String, amount: Int = 1) {
        viewModelScope.launch {
            try {
                val success = repository.updateMissionProgress(playerId, missionType, amount)
                if (success) {
                    loadMissions(playerId) // Reload to show updated progress
                }
            } catch (e: Exception) {
                _claimResult.value = ClaimResult.Error(e.message ?: "Error updating progress")
            }
        }
    }

    fun clearClaimResult() {
        _claimResult.value = null
    }
}
