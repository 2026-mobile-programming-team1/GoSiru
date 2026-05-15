package com.example.app_admin.data

import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

// Profiles 테이블용 응답 모델
@Serializable
data class ProfileResponse(
    val id: String,
    val is_admin: Boolean
)

class WelfareRepository(
    private val postgrest: Postgrest,
    private val auth: Auth // Auth 추가
) {

    // 1. 로그인 + 관리자 체크 함수
    suspend fun loginAndCheckAdmin(email: String, pass: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // 이메일 로그인 시도
            auth.signInWith(Email) {
                this.email = email
                password = pass
            }

            // 로그인 성공 시 현재 유저의 UID 가져오기
            val userUid = auth.currentUserOrNull()?.id ?: return@withContext Result.failure(Exception("유저 정보를 찾을 수 없음"))

            // Profiles 테이블에서 is_admin 값 확인
            val profile = postgrest.from("profiles")
                .select {
                    filter { eq("id", userUid) }
                }.decodeSingle<ProfileResponse>()

            if (profile.is_admin) {
                Result.success(true)
            } else {
                // 관리자가 아니면 로그아웃 시키고 실패 반환
                auth.signOut()
                Result.failure(Exception("관리자 권한이 없습니다."))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // 기존 업로드 함수 (그대로 유지)
    suspend fun uploadWelfare(item: WelfareRequest): Boolean = withContext(Dispatchers.IO) {
        try {
            postgrest.from("welfare_list").insert(item)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}