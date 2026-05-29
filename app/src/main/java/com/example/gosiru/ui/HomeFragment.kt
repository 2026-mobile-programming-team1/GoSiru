package com.example.gosiru.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.fragment.app.Fragment
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gosiru.R
import com.example.gosiru.databinding.FragmentHomeBinding
import com.example.gosiru.viewmodel.HomeViewModel // 💡 새로 만든 HomeViewModel 임포트

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val mainActivity: MainActivity
        get() = requireActivity() as MainActivity

    // 💡 HomeViewModel 초기화 (MainActivity에 선언된 supabase 객체를 주입받는 구조)
    // 프로젝트의 의존성 주입 방식(Hilt 등)이 따로 있다면 그에 맞춰 변경하셔도 됩니다.
    private val viewModel: HomeViewModel by lazy {
        HomeViewModel(com.example.gosiru.network.Supabase.client)    }

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

        // 💡 1. 홈 화면이 생성될 때 DB에서 유저 맞춤형 기한 임박 복지 데이터를 요청
        viewModel.fetchUrgentWelfare()

        // 💡 2. Compose 배너 영역에 실시간 DB 데이터 바인딩
        binding.composeUrgentBanner.apply {
            // 프래그먼트 뷰가 파괴될 때 Compose 리소스를 안전하게 해제
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    // ViewModel의 상태를 관찰합니다 (데이터 변경 시 자동 UI 업데이트)
                    val bannerState = viewModel.urgentBannerState.value

                    if (bannerState != null) {
                        // 조건에 맞는 데이터가 존재하고 D-5 이내일 때 -> 레이아웃 노출 및 데이터 매핑
                        binding.tvUrgentTitle.visibility = View.VISIBLE
                        binding.composeUrgentBanner.visibility = View.VISIBLE

                        UrgentBenefitBanner(
                            dDay = bannerState.dDay,
                            title = bannerState.title,
                            description = bannerState.description
                        )
                    } else {
                        // 노출 대상 복지가 없을 때 -> 제목과 배너 공간을 아예 숨김 처리
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
            Log.d("HomeFragment", "지역 화폐 클릭")

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
            val fragment = HighlightFragment().apply {
                arguments = Bundle().apply { putString("TAB_TYPE", "NEW") }
            }
            mainActivity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        // 2026 인상된 지원금 카드 클릭 이벤트
        binding.btnIncreasedPolicy.setOnClickListener {
            val fragment = HighlightFragment().apply {
                arguments = Bundle().apply { putString("TAB_TYPE", "INCREASED") }
            }
            mainActivity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

@Composable
fun UrgentBenefitBanner(
    dDay: Int,
    title: String,
    description: String
) {
    androidx.compose.material3.Card(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp), // 아래 카드와의 간격
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp), // 카드 모서리
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White // 흰색 배경
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = androidx.compose.ui.Modifier
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            // 좌측 배지 영역 (D-Day 표기)
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .background(androidx.compose.ui.graphics.Color(0xFFFFEAEA)), // 연한 빨강
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    text = "D-$dDay",
                    color = androidx.compose.ui.graphics.Color(0xFFE53935),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(16.dp))

            // 중앙 텍스트 정보 (DB 연동 완료)
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.ui.Modifier.weight(1f)
            ) {
                androidx.compose.material3.Text(
                    text = title,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 16.sp,
                    color = androidx.compose.ui.graphics.Color(0xFF191F28)
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(2.dp))
                androidx.compose.material3.Text(
                    text = description,
                    fontSize = 12.sp,
                    color = androidx.compose.ui.graphics.Color(0xFF8B95A1)
                )
            }

            // 우측 화살표 아이콘
            androidx.compose.material3.Icon(
                painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_media_next),
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color(0xFFB0B8C1),
                modifier = androidx.compose.ui.Modifier.size(16.dp)
            )
        }
    }
}