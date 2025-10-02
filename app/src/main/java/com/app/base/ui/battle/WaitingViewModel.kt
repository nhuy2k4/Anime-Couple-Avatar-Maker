package com.app.base.ui.battle


import androidx.lifecycle.viewModelScope
import com.brally.mobile.base.viewmodel.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WaitingViewModel : BaseViewModel() {

    // Trạng thái chờ (có thể hiển thị ra UI)
    private val _status = MutableStateFlow("Đang chờ đối thủ...")
    val status: StateFlow<String> get() = _status

    // Ví dụ: đếm ngược 10 giây
    private val _countdown = MutableStateFlow(3)
    val countdown: StateFlow<Int> get() = _countdown

    init {
        startCountdown()
    }

    private fun startCountdown() {
        viewModelScope.launch {
            for (i in 10 downTo 0) {
                _countdown.value = i
                delay(1000)
            }
            _status.value = "Đã tìm thấy đối thủ!"
        }
    }
}
