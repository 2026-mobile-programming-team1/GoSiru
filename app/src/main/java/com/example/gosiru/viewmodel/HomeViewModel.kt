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
    // ⭐ 마감일이 없는(null) 상시 혜택도 에러 없이 받기 위해 `String? = null` 처리
    @SerialName("end_date") val endDate: String? = null,
    // ⭐ 바텀시트 UI에 넘겨줄 추가 정보들
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("apply_link") val applyLink: String? = null
)

// 💡 3. UI(Compose)에서 사용할 가공된 상태 클래스
data class UrgentBannerUiState(
    val title: String,
    val subtitle: String, // 기존 description에서 디자인 시안에 맞춰 이름 변경
    val dDay: Int,
    val period: String,   // 예: "2026.03.01 ~ 03.31"
    val applyLink: String // 이동할 웹사이트 링크
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
                            // ⭐ gte("end_date", ...) 로직 덕분에 DB에서 1차적으로 null인 애들은 탈락합니다.
                            // 마감일이 '오늘 이후'로 존재하는 것만 필터링!
                            gte("end_date", LocalDate.now().toString())
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

                    // ⭐ 만약 DB에서 꼬여서 null이 들어오더라도 앱이 터지지 않도록 방어 코드 추가
                    if (welfare.endDate != null) {
                        val dDay = calculateDDay(welfare.endDate)

                        // 💡 날짜 가공: 시작일이 없으면 "상시", 있으면 "2026.03.01" 형태로 변경
                        val startStr = welfare.startDate?.replace("-", ".") ?: "상시"
                        val endStr = welfare.endDate.replace("-", ".")
                        val displayPeriod = "$startStr ~ $endStr"

                        // 4. [핵심 조건] 딱 마감 D-5 이내일 때만 배너 데이터 생성
                        if (dDay in 0..5) {
                            urgentBannerState.value = UrgentBannerUiState(
                                title = welfare.title,
                                subtitle = welfare.content ?: "지원 내용을 확인해보세요.",
                                dDay = dDay,
                                period = displayPeriod,
                                applyLink = welfare.applyLink ?: ""
                            )
                        } else {
                            urgentBannerState.value = null
                        }
                    } else {
                        // endDate가 null이면 마감 임박이 아니므로 배너 숨김
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