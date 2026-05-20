package com.example.gosiru.network

import android.util.Log
import com.example.gosiru.model.UserProfile
import com.example.gosiru.model.WelfareItem
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import com.google.firebase.messaging.FirebaseMessaging

// 🔥 SQL 함수에 던져줄 파라미터 전용 클래스 새로 생성!
@Serializable
data class MatchParams(
    val u_age: Int,
    val u_job: String,
    val u_income: Int,
    val u_disabled: Boolean,
    val u_foreigner: Boolean,
    val u_gender: String
)

object WelfareRepository {

    suspend fun saveUserProfile(profile: UserProfile): Boolean {
        return try {
            Supabase.client.postgrest.from("profiles").upsert(profile)
            true
        } catch (e: Exception) {
            Log.e("Repository", "프로필 저장 실패", e)
            false
        }
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            Supabase.client.postgrest.from("profiles")
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<UserProfile>()
        } catch (e: Exception) {
            Log.e("Repository", "프로필 가져오기 실패", e)
            null
        }
    }

    // 🔥 여기 완벽하게 수정됨!
    suspend fun getMatchedWelfare(
        age: Int,
        jobStatus: String,
        incomeLevel: Int,
        isDisabled: Boolean,
        isForeigner: Boolean,
        gender: String
    ): List<WelfareItem> {
        return try {
            // 파라미터 클래스에 데이터 꽉꽉 채워서
            val params = MatchParams(
                u_age = age,
                u_job = jobStatus,
                u_income = incomeLevel,
                u_disabled = isDisabled,
                u_foreigner = isForeigner,
                u_gender = gender
            )

            // parameters 속성으로 명확하게 전달!
            Supabase.client.postgrest.rpc(
                function = "get_matched_welfare",
                parameters = params
            ).decodeList<WelfareItem>()
        } catch (e: Exception) {
            Log.e("Repository", "복지 매칭 실패", e)
            emptyList()
        }
    }

    fun saveFcmToken(userUid: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                MainScope().launch {
                    try {
                        Supabase.client.postgrest.from("profiles").update(
                            mapOf("fcm_token" to token)
                        ) { filter { eq("id", userUid) } }
                    } catch (e: Exception) {
                        Log.e("Repository", "FCM 토큰 저장 실패", e)
                    }
                }
            }
        }
    }
}