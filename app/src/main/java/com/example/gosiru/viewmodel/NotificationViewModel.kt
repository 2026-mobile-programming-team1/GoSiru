package com.example.gosiru.viewmodel // 패키지명 확인!

import android.service.autofill.Validators.or
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.gosiru.data.Notification
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.util.Objects.isNull

class NotificationViewModel(private val supabase: SupabaseClient) : ViewModel() {

    var notificationList = mutableStateOf<List<Notification>>(emptyList())

    suspend fun fetchNotifications() {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        Log.d("NotificationVM", "현재 요청하는 유저 ID: $userId")
        try {
            val response = supabase.from("notifications")
                .select {

                    filter {
                        or {
                            eq("user_id", userId)
                            isNull("user_id")
                        }
                    }
                    order("created_at", order = Order.DESCENDING)
                }
                .decodeList<Notification>()
            //Log.d("NotificationVM", "🔥 [테스트] 서버에서 전체 가져온 알림 개수: ${response.size}")

            notificationList.value = response
            Log.d("NotificationVM", "불러온 알림 개수: ${response.size}")

        } catch (e: Exception) {
            Log.e("NotificationVM", "🚨 Supabase 에러: ${e.message}")
            e.printStackTrace()
        }
    }
}