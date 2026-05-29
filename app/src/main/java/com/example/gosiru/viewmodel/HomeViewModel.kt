package com.example.gosiru.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar

// 💡 1. profiles 테이블과 연동할 유저 프로필 데이터 클래스
@Serializable
data class UserProfile(
    @SerialName("birth_date") val birthDate: String,
    @SerialName("job_status") val jobStatus: String
)

// 💡 2. welfare_list 테이블에서 가져올 복지 데이터 클래스
@Serializable
data class UrgentWelfareResponse(
    val id: Long,
    val title: String,
    val content: String?,
    @SerialName("end_date") val endDate: String
)

// 💡 3. UI(Compose)에서 사용할 가공된 상태 클래스
data class UrgentBannerUiState(
    val title: String,
    val description: String,
    val dDay: Int
)

class HomeViewModel(private val supabase: SupabaseClient) : ViewModel() {

    // 배너 상태 관리 (기본값 null -> 데이터 조건 맞으면 활성화)
    var urgentBannerState = mutableStateOf<UrgentBannerUiState?>(null)

    fun fetchUrgentWelfare() {
        viewModelScope.launch {
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch

                // 1. 유저 프로필 가져오기
                val profile = supabase.from("profiles")
                    .select { filter { eq("id", userId) } }
                    .decodeSingle<UserProfile>()

                // 2. 만 나이 계산 (현재 연도 - 출생 연도)
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val birthYear = profile.birthDate.substring(0, 4).toInt()
                val userAge = currentYear - birthYear

                // 3. 조건에 맞는 가장 급한 복지 딱 1개만 가져오기
                val response = supabase.from("welfare_list")
                    .select {
                        filter {
//                            neq("end_date", null) // 기한이 없는 것은 패스
                            gte("end_date", LocalDate.now().toString()) // 마감일이 오늘 이후인 것
                            lte("min_age", userAge)
                            gte("max_age", userAge)
                            or {
                                eq("target_job", "전체")
                                eq("target_job", profile.jobStatus)
                            }
                        }
                        order("end_date", order = Order.ASCENDING) // 마감 임박순 정렬
                        limit(1) // 탑 1개만 선정
                    }
                    .decodeList<UrgentWelfareResponse>()

                if (response.isNotEmpty()) {
                    val welfare = response[0]
                    val dDay = calculateDDay(welfare.endDate)

                    // 4. [핵심 조건] 딱 마감 D-5 이내일 때만 배너 데이터 생성
                    if (dDay in 0..5) {
                        urgentBannerState.value = UrgentBannerUiState(
                            title = welfare.title,
                            description = welfare.content ?: "",
                            dDay = dDay
                        )
                    } else {
                        urgentBannerState.value = null
                    }
                } else {
                    urgentBannerState.value = null
                }
            } catch (e: Exception) {
                Log.e("HomeVM", "🚨 배너 데이터 로드 실패: ${e.message}")
                e.printStackTrace()
                urgentBannerState.value = null
            }
        }
    }

    private fun calculateDDay(endDate: String): Int {
        val targetDate = LocalDate.parse(endDate)
        val today = LocalDate.now()
        return ChronoUnit.DAYS.between(today, targetDate).toInt()
    }
}