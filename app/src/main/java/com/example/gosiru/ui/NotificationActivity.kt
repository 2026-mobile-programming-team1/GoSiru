package com.example.gosiru.ui

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.example.gosiru.databinding.ActivityNotificationBinding
import com.example.gosiru.databinding.AppBarBinding // 💡 반드시 임포트 하세요!
import com.example.gosiru.network.Supabase
import com.example.gosiru.viewmodel.NotificationViewModel

class NotificationActivity : ComponentActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var appBarBinding: AppBarBinding // 💡 app_bar 전용 바인딩 변수 추가

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 전체 화면 바인딩 초기화
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 💡 2. 상단 바(app_bar) 전용 바인딩 초기화
        // binding.layoutAppBar.root 라고 하면 에러가 날 수 있으니, 아래처럼 바인딩합니다.
        appBarBinding = AppBarBinding.bind(binding.layoutAppBar.root)

        // 3. 상단바 커스텀 설정
        appBarBinding.apply {
            // "시루떡" 글씨를 "알림"으로 변경 (기존 코드에 없었지만 추가하면 좋습니다!)
            textView.text = "알림 목록"

            // 뒤로가기 버튼 보이게 하고 기능 연결
            btnReturn.visibility = View.VISIBLE
            btnReturn.setOnClickListener {
                finish()
            }

            // 종 모양 영역 숨기기
            bellContainer.visibility = View.GONE
            // 💡 bell 하나만 숨기면 영역이 남을 수 있으므로 bellContainer 전체를 숨기는 것이 깔끔합니다.
        }

        // 4. 뷰모델 및 컴포즈 화면 세팅
        val viewModel = NotificationViewModel(supabase = Supabase.client)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                NotificationScreen(viewModel = viewModel)
            }
        }
    }
}