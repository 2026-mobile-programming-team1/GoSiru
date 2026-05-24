package com.example.app_admin.data

import android.util.Log
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [데이터 모델 구역]
 * 모든 모델은 파일 최상단에 모아두는 것이 유지보수에 좋습니다.
 */

@Serializable
data class ProfileResponse(
    val id: String,
    val is_admin: Boolean
)

@Serializable
data class WelfareResponse(
    val id: Int
)

@Serializable
data class NotificationInsert(
    val title: String,
    val body: String,
    // 코틀린 변수명은 welfareId로 쓰되, DB 컬럼명인 welfare_id와 자동으로 매칭해줍니다.
    @SerialName("welfare_id") val welfareId: Int
)

/**
 * [레포지토리 구역]
 */
class WelfareRepository(
    private val postgrest: Postgrest,
    private val auth: Auth
) {

    // 1. 로그인 + 관리자 체크 함수
    suspend fun loginAndCheckAdmin(email: String, pass: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            auth.signInWith(Email) {
                this.email = email
                password = pass
            }

            val userUid = auth.currentUserOrNull()?.id ?: return@withContext Result.failure(Exception("유저 정보를 찾을 수 없음"))

            val profile = postgrest.from("profiles")
                .select {
                    filter { eq("id", userUid) }
                }.decodeSingle<ProfileResponse>()

            if (profile.is_admin) {
                Result.success(true)
            } else {
                auth.signOut()
                Result.failure(Exception("관리자 권한이 없습니다."))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // 2. 복지 및 알림 순차 등록 함수
    suspend fun uploadWelfare(item: WelfareRequest): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("ADMIN_TEST", "🚀 [1단계] welfare_list 저장 시작...")

            // [STEP 1] welfare_list 저장 및 생성된 ID 확보
            val savedWelfare = postgrest.from("welfare_list")
                .insert(item) {
                    select()
                }.decodeSingle<WelfareResponse>()

            Log.d("ADMIN_TEST", "✅ [2단계] welfare_list 저장 성공! ID: ${savedWelfare.id}")

            // [STEP 2] 알림 데이터 생성 (apply_link는 이제 여기서 빠집니다!)
            val notificationData = NotificationInsert(
                title = "[신규 혜택] ${item.title}",
                body = item.content,
                welfareId = savedWelfare.id
            )

            // [STEP 3] notifications 테이블에 알림 저장
            Log.d("ADMIN_TEST", "🚀 [3단계] notifications 알림 저장 시작...")
            postgrest.from("notifications").insert(notificationData)

            Log.d("ADMIN_TEST", "🎉 [4단계] 모든 과정 성공!")
            true

        } catch (e: Exception) {
            Log.e("ADMIN_TEST", "❌❌❌ 진짜 에러 발생! ❌❌❌")
            Log.e("ADMIN_TEST", "에러 메시지: ${e.message}")
            Log.e("ADMIN_TEST", "상세 내용: ${e.toString()}")
            e.printStackTrace()
            false
        }
    }
}