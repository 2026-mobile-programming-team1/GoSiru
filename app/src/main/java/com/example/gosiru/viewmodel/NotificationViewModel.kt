package com.example.gosiru.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gosiru.data.Notification
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 💡 1. 복지 리스트에서 링크만 쏙 빼오기 위한 전용 데이터 클래스
@Serializable
data class WelfareLinkResponse(
    @SerialName("apply_link") val applyLink: String? = null
)

class NotificationViewModel(private val supabase: SupabaseClient) : ViewModel() {

    var notificationList = mutableStateOf<List<Notification>>(emptyList())

    // 💡 2. 알림 목록 가져오기 (오류 나던 필터링 구문 수정)
    suspend fun fetchNotifications() {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        Log.d("NotificationVM", "현재 요청하는 유저 ID: $userId")

        try {
            val response = supabase.from("notifications")
                .select {
                    filter {
                        // 엉뚱한 import 에러를 방지하기 위해 Supabase 공식 문자열 쿼리 사용
                        // (내 아이디이거나, 아이디가 지정되지 않은 전체 알림이거나)
                        or{
                            eq("user_id", userId)
                            exact("user_id", null)
                        }
                    }
                    order("created_at", order = Order.DESCENDING)
                }
                .decodeList<Notification>()

            notificationList.value = response
            Log.d("NotificationVM", "불러온 알림 개수: ${response.size}")

        } catch (e: Exception) {
            Log.e("NotificationVM", "🚨 Supabase 알림 로드 에러: ${e.message}")
            e.printStackTrace()
        }
    }

    // 💡 3. 새로 추가! 알림 클릭 시 DB에서 진짜 링크 가져오는 함수
    fun getWelfareLink(welfareId: Int, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                // welfare_list 테이블에 가서 파라미터로 받은 id와 똑같은 글을 찾음
                val welfare = supabase.from("welfare_list")
                    .select {
                        filter { eq("id", welfareId) }
                    }
                    .decodeSingle<WelfareLinkResponse>()

                // 찾은 링크를 화면(NotificationScreen)으로 돌려줌
                onResult(welfare.applyLink)

            } catch (e: Exception) {
                Log.e("NotificationVM", "🚨 링크 불러오기 실패: ${e.message}")
                // 에러가 나거나 링크가 없으면 null 반환
                onResult(null)
            }
        }
    }
}