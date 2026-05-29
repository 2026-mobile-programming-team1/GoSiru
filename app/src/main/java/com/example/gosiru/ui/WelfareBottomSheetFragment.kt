package com.example.gosiru.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// 💡 XML 기반의 Fragment에서 Compose 바텀시트를 띄우기 위한 래퍼 클래스
class WelfareBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        // 프래그먼트를 생성할 때 데이터를 넘겨받는 함수
        fun newInstance(
            title: String,
            subtitle: String,
            endDate: String,
            startDate: String,
            applyLink: String
        ): WelfareBottomSheetFragment {
            return WelfareBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString("TITLE", title)
                    putString("SUBTITLE", subtitle)
                    putString("END_DATE", endDate)
                    putString("START_DATE", startDate)
                    putString("APPLY_LINK", applyLink)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // 전달받은 데이터 꺼내기
        val title = arguments?.getString("TITLE") ?: ""
        val subtitle = arguments?.getString("SUBTITLE") ?: "지원 내용을 확인해보세요."
        val endDate = arguments?.getString("END_DATE") ?: ""
        val startDate = arguments?.getString("START_DATE") ?: ""
        val applyLink = arguments?.getString("APPLY_LINK") ?: ""

        // D-Day 및 기간 계산
        val dDay = calculateDDay(endDate)
        val startStr = if (startDate.isNotEmpty()) startDate.replace("-", ".") else "상시"
        val endStr = if (endDate.isNotEmpty()) endDate.replace("-", ".") else "상시"
        val period = if (startStr == "상시" && endStr == "상시") "상시 지원" else "$startStr ~ $endStr"

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    // 💡 바텀시트 안쪽 내용물
                    BottomSheetContent(title, subtitle, dDay, period, applyLink)
                }
            }
        }
    }

    private fun calculateDDay(endDate: String): Int {
        if (endDate.isEmpty() || endDate == "상시") return 0
        return try {
            val targetDate = LocalDate.parse(endDate)
            ChronoUnit.DAYS.between(LocalDate.now(), targetDate).toInt()
        } catch (e: Exception) { 0 }
    }
}

// ---------------------------------------------------------
// 💡 아래는 아까 만든 시안 100% 동일한 Compose UI 입니다.
// 💡 에러 방지를 위해 모두 'private'을 붙였습니다!
// ---------------------------------------------------------

// ---------------------------------------------------------
// 💡 교체할 UI 컴포저블 코드 (파일 맨 아래에 덮어씌우세요)
// ---------------------------------------------------------

@Composable
private fun BottomSheetContent(
    title: String, subtitle: String, dDay: Int, period: String, applyLink: String
) {
    val context = LocalContext.current
    val checklistItems = listOf("신분증 (본인 명의)", "입학 증명서 (학교 발행)", "신청서 (시청 양식)")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 40.dp)
    ) {
        // 드래그 핸들
        Box(
            modifier = Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFE5E8EB)).align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // 1. 마감 임박 배지
        if (dDay > 0) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFF4E6))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("마감 임박", color = Color(0xFFFF8A00), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. 메인 타이틀 & 서브타이틀
        Text(text = title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191F28))
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = subtitle, fontSize = 15.sp, color = Color(0xFF8B95A1))
        Spacer(modifier = Modifier.height(24.dp))

        // 3. 신청 일정 확인 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFF3182F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (dDay > 0) "D-$dDay" else "상시", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("신청 일정 확인", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191F28))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("신청 기간: $period", fontSize = 13.sp, color = Color(0xFF8B95A1))
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // 4. 체크리스트 영역 (💡 호출하는 함수 이름도 ChecklistItem으로 변경!)
        Text(text = "신청 전 챙기세요!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191F28))
        Spacer(modifier = Modifier.height(12.dp))
        checklistItems.forEach { item ->
            ChecklistItem(text = item)
        }
        Spacer(modifier = Modifier.height(32.dp))

        // 5. 하단 버튼 2개
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (applyLink.isNotEmpty()) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(applyLink)))
                    }
                },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E8EB))
            ) {
                Text("상세정보보기", color = Color(0xFF4E5968), fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val gmmIntentUri = Uri.parse("geo:0,0?q=행정복지센터")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply { setPackage("com.google.android.apps.maps") }
                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                    } else {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://googleusercontent.com/maps.google.com/5")))
                    }
                },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3182F6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("근처 복지 센터", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 💡 함수명 변경 완료! import 에러가 안 나도록 by 대신 = 를 사용했습니다.
@Composable
private fun ChecklistItem(text: String) {
    val isChecked = remember { mutableStateOf(false) } // by -> = 로 변경
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isChecked.value = !isChecked.value } // .value 로 접근
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = if (isChecked.value) android.R.drawable.checkbox_on_background else android.R.drawable.checkbox_off_background),
            contentDescription = null,
            tint = if (isChecked.value) Color(0xFF3182F6) else Color(0xFFD1D6DB),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = if (isChecked.value) Color(0xFFB0B8C1) else Color(0xFF333D4B),
            textDecoration = if (isChecked.value) TextDecoration.LineThrough else TextDecoration.None
        )
    }
}
