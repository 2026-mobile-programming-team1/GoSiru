package com.example.app_admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_admin.data.WelfareRepository
import com.example.app_admin.data.WelfareRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel(private val repository: WelfareRepository) : ViewModel() {

    // 1. UI 상태 관리 (성공/실패 여부를 관찰 가능하게 만듦)
    private val _uploadStatus = MutableStateFlow<Boolean?>(null)
    val uploadStatus: StateFlow<Boolean?> = _uploadStatus

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 2. 복지 등록 함수
    fun registerWelfare(
        title: String,
        content: String,
        minAge: Int?,
        maxAge: Int?,
        job: String?,
        income: Int?,
        gender: String?,
        disabled: Boolean,
        foreigner: Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true // 로딩 시작

            val request = WelfareRequest(
                title = title,
                content = content,
                min_age = minAge,
                max_age = maxAge,
                target_job = job,
                income_limit = income,
                gender = gender,
                is_disabled_only = disabled,
                is_foreigner_only = foreigner
            )

            // 레포지토리에 데이터 전송 요청
            val result = repository.uploadWelfare(request)
            _uploadStatus.value = result // 성공/실패 결과 저장
            _isLoading.value = false // 로딩 종료
        }
    }

    // 상태 초기화 (성공 알림 후 다시 입력 폼으로 돌아올 때 등)
    fun resetStatus() {
        _uploadStatus.value = null
    }
}