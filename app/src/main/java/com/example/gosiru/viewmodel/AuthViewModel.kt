package com.example.gosiru.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gosiru.network.Supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val uid: String? = null,
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    // 회원가입
    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "이메일과 비밀번호를 입력해주세요.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                Supabase.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                val uid = Supabase.client.auth.currentUserOrNull()?.id
                _uiState.value = AuthUiState(isSuccess = true, uid = uid)
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = "회원가입 실패: ${e.message}")
            }
        }
    }

    // 로그인 (새로 추가)
    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "이메일과 비밀번호를 입력해주세요.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                Supabase.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                val uid = Supabase.client.auth.currentUserOrNull()?.id
                _uiState.value = AuthUiState(isSuccess = true, uid = uid)
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = "로그인 실패: 정보를 확인해주세요.")
            }
        }
    }
}