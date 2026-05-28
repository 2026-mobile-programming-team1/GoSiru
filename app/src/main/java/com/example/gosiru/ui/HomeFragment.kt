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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.fragment.app.Fragment
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gosiru.R
import com.example.gosiru.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    private val mainActivity: MainActivity
        get() = requireActivity() as MainActivity

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
        binding.composeUrgentBanner.apply {
            // 프래그먼트 뷰가 파괴될 때 Compose 리소스를 안전하게 해제
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    UrgentBenefitBanner(
                        dDay = 3,
                        title = "코딩 지원금",
                        description = "코딩지원금 대학생에게 1000원"
                    )
                }
            }
        }
        setupClickListeners()
    }



    private fun setupClickListeners() {

//        // 1. app_bar.xml 레이아웃 바인딩 (ID가 bell, redDot 임을 확인)
//        val appBarBinding = com.example.gosiru.databinding.AppBarBinding.bind(binding.root)
//
//        // 2. 알림함 이동 로직 (Activity 이동)
//        val navigateToNotification = View.OnClickListener {
//            Log.d("HomeFragment", "알림함(NotificationActivity)으로 이동")
//            val intent = Intent(requireContext(), NotificationActivity::class.java)
//            startActivity(intent)
//        }
//
//        // 3. bellContainer에 리스너를 달면 종과 빨간 점 어디를 눌러도 잘 작동합니다.
//        appBarBinding.bellContainer.setOnClickListener(navigateToNotification)
//
//        // 혹시 모르니 개별 뷰에도 달아두고 싶다면:
//        appBarBinding.bell.setOnClickListener(navigateToNotification)
//        appBarBinding.redDot.setOnClickListener(navigateToNotification)






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

        // 🔥 여기서부터 추가됨: 2026 신규 정책 카드 클릭 이벤트
        binding.btnNewPolicy.setOnClickListener {
            val fragment = HighlightFragment().apply {
                arguments = Bundle().apply { putString("TAB_TYPE", "NEW") }
            }
            // 뒤로가기를 위해 addToBackStack(null) 필수
            mainActivity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        // 🔥 여기서부터 추가됨: 2026 인상된 지원금 카드 클릭 이벤트
        binding.btnIncreasedPolicy.setOnClickListener {
            val fragment = HighlightFragment().apply {
                arguments = Bundle().apply { putString("TAB_TYPE", "INCREASED") }
            }
            // 뒤로가기를 위해 addToBackStack(null) 필수
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
// HomeFragment.kt 파일의 가장 하단 (클래스 밖)
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
            containerColor = androidx.compose.ui.graphics.Color.White // 기존 카드처럼 흰색으로 변경
        ),
        // 기존 카드처럼 아주 미세한 그림자 효과
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = androidx.compose.ui.Modifier
                .padding(horizontal = 20.dp, vertical = 18.dp) // 기존 XML과 동일한 패딩
                .fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            // 💡 좌측 아이콘: 원형에서 사각형(Rounded)으로 변경 (기존 카드 스타일)
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier
                    .size(40.dp) // 기존 카드와 동일한 40dp
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)) // 둥근 사각형
                    .background(androidx.compose.ui.graphics.Color(0xFFFFEAEA)), // 연한 빨강
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    text = "D-$dDay",
                    color = androidx.compose.ui.graphics.Color(0xFFE53935),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 13.sp // 글자 크기 살짝 조정
                )
            }

            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(16.dp))

            // 💡 중앙 텍스트: 기존 카드와 색상 및 크기 통일
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.ui.Modifier.weight(1f)
            ) {
                androidx.compose.material3.Text(
                    text = title,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 16.sp,
                    color = androidx.compose.ui.graphics.Color(0xFF191F28) // 기존 제목 색상 (#191F28)
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(2.dp))
                androidx.compose.material3.Text(
                    text = description,
                    fontSize = 12.sp, // 기존 설명 크기 12sp
                    color = androidx.compose.ui.graphics.Color(0xFF8B95A1) // 기존 설명 색상 (#8B95A1)
                )
            }

            // 💡 우측 화살표: 기존 XML에서 쓴 아이콘(ic_media_next)과 똑같은 모양으로 변경
            androidx.compose.material3.Icon(
                painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_media_next),
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color(0xFFB0B8C1), // 기존 화살표 색상 (#B0B8C1)
                modifier = androidx.compose.ui.Modifier.size(16.dp) // 기존 화살표 크기 16dp
            )
        }
    }
}