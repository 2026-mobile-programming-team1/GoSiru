package com.example.gosiru.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.example.gosiru.R
import com.example.gosiru.databinding.FragmentHomeBinding
import com.example.gosiru.viewmodel.HomeViewModel

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val mainActivity: MainActivity
        get() = requireActivity() as MainActivity

    private val viewModel: HomeViewModel by lazy {
        HomeViewModel(com.example.gosiru.network.Supabase.client)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // FCM 토큰 로그 확인
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM_TEST", "토큰 가져오기 실패", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM_TEST", "🔥 내 진짜 FCM 토큰: $token")
        }

        viewModel.fetchUrgentWelfare()

        binding.composeUrgentBanner.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    val bannerState = viewModel.urgentBannerState.value
                    // 💡 바텀시트 노출 상태 관리
                    var showSheet by remember { mutableStateOf(false) }

                    if (bannerState != null) {
                        binding.tvUrgentTitle.visibility = View.VISIBLE
                        binding.composeUrgentBanner.visibility = View.VISIBLE

                        // 💡 배너 클릭 시 바텀시트 띄우기
                        Box(modifier = Modifier.clickable { showSheet = true }) {
                            UrgentBenefitBanner(
                                dDay = bannerState.dDay,
                                title = bannerState.title,
                                description = bannerState.subtitle // ViewModel의 subtitle을 배너 설명으로 사용
                            )
                        }

                        // 💡 바텀시트 구현부 (최신 ViewModel 데이터 바인딩)
                        if (showSheet) {
                            WelfareDetailBottomSheet(
                                dDay = bannerState.dDay,
                                title = bannerState.title,
                                subtitle = bannerState.subtitle,
                                period = bannerState.period,
                                applyLink = bannerState.applyLink,
                                onDismiss = { showSheet = false }
                            )
                        }
                    } else {
                        binding.tvUrgentTitle.visibility = View.GONE
                        binding.composeUrgentBanner.visibility = View.GONE
                    }
                }
            }
        }
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 지역 화폐 결제 앱 이동
        binding.btnOpenApp.setOnClickListener {
            val chakPackageName = "com.komscochak.m2.client"
            val intent = requireContext().packageManager.getLaunchIntentForPackage(chakPackageName)

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                requireContext().startActivity(intent)
            } else {
                val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse("market://details?id=$chakPackageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                requireContext().startActivity(playStoreIntent)
            }
        }

        // 누적 혜택 내역 보기 버튼
        binding.btnBenefitDetail.setOnClickListener {
            Log.d("HomeFragment", "누적 혜택 내역 보기 클릭")
        }

        // 2026 신규 정책 카드 클릭 이벤트
        binding.btnNewPolicy.setOnClickListener {
            val highlightFragment = HighlightFragment().apply {
                arguments = Bundle().apply {
                    putString("TAB_TYPE", "NEW")
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, highlightFragment)
                .addToBackStack(null) // 뒤로 가기를 위해 백스택에 추가
                .commit()
        }

// 2026 인상된 지원금 카드 클릭 이벤트
        binding.btnIncreasedPolicy.setOnClickListener {
            val highlightFragment = HighlightFragment().apply {
                arguments = Bundle().apply {
                    putString("TAB_TYPE", "INCREASED")
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, highlightFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// --------------------------------------------------------------------------------
// 💡 아래는 바텀시트 및 체크리스트 UI 컴포저블입니다.
// --------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelfareDetailBottomSheet(
    dDay: Int,
    title: String,
    subtitle: String,
    period: String,
    applyLink: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    // 휘발성 체크리스트 텍스트 하드코딩
    val checklistItems = listOf("신분증 (본인 명의)", "입학 증명서 (학교 발행)", "신청서 (시청 양식)")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // 1. 마감 임박 배지
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFF4E6))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("마감 임박", color = Color(0xFFFF8A00), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))

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
                    // 파란 원형 D-Day
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3182F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("D-$dDay", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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

            // 4. 체크리스트 영역 (디자인 시안대로 카운트 제거)
            Text(text = "신청 전 챙기세요!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191F28))
            Spacer(modifier = Modifier.height(12.dp))
            checklistItems.forEach { item ->
                AppleStyleChecklistItem(text = item)
            }
            Spacer(modifier = Modifier.height(32.dp))

            // 5. 하단 버튼 2개
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 상세정보보기 버튼 (DB 링크로 이동)
                OutlinedButton(
                    onClick = {
                        if (applyLink.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(applyLink))
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E8EB)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4E5968))
                ) {
                    Text("상세정보보기", fontWeight = FontWeight.Bold)
                }

                // 근처 복지 센터 (구글 지도 행정복지센터 검색)
                Button(
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=행정복지센터")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")

                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=행정복지센터"))
                            context.startActivity(webIntent)
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
}

@Composable
fun AppleStyleChecklistItem(text: String) {
    // 💡 UI 상태만 관리 (끄면 날아감)
    var isChecked by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isChecked = !isChecked }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 체크박스 아이콘
        Icon(
            painter = painterResource(
                id = if (isChecked) android.R.drawable.checkbox_on_background
                else android.R.drawable.checkbox_off_background
            ),
            contentDescription = null,
            tint = if (isChecked) Color(0xFF3182F6) else Color(0xFFD1D6DB),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        // 💡 체크 시 취소선(LineThrough) 효과
        Text(
            text = text,
            fontSize = 16.sp,
            color = if (isChecked) Color(0xFFB0B8C1) else Color(0xFF333D4B),
            textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
        )
    }
}

@Composable
fun UrgentBenefitBanner(
    dDay: Int,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 좌측 배지 영역 (D-Day 표기)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFEAEA)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "D-$dDay",
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 중앙 텍스트 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF191F28)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color(0xFF8B95A1)
                )
            }

            // 우측 화살표 아이콘
            Icon(
                painter = painterResource(id = android.R.drawable.ic_media_next),
                contentDescription = null,
                tint = Color(0xFFB0B8C1),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}