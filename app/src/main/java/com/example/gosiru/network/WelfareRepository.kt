package com.example.gosiru.network

import com.example.gosiru.data.WelfareItem // 1번에서 만든 모델 import
import com.example.gosiru.network.Supabase // SupabaseClient가 있는 경로로 import 맞춰줘
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import com.google.firebase.messaging.FirebaseMessaging

object WelfareRepository {

    // Supabase에서 매칭된 복지 리스트 가져오기
    suspend fun getMatchedWelfare(
        age: Int,
        jobStatus: String,
        incomeLevel: Int
    ): List<WelfareItem> {
        return Supabase.client.postgrest.rpc("get_matched_welfare") {
            mapOf(
                "u_age" to age,
                "u_job" to jobStatus,
                "u_income" to incomeLevel
            )
        }.decodeList<WelfareItem>()
    }

    // FCM 토큰을 Supabase profiles 테이블에 저장
    fun saveFcmToken(userUid: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val token = task.result
            MainScope().launch {
                Supabase.client.postgrest.from("profiles").update(
                    mapOf("fcm_token" to token)
                ) { filter { eq("id", userUid) } }
            }
        }
    }
}