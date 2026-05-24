package com.example.gosiru.ui

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.example.gosiru.databinding.ActivityNotificationBinding
import com.example.gosiru.network.Supabase
import com.example.gosiru.viewmodel.NotificationViewModel

class NotificationActivity : ComponentActivity() {

    // 뷰바인딩 변수
    private lateinit var binding: ActivityNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 바인딩 초기화
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. 상단바 설정 (텍스트 변경은 안함!)
        binding.layoutAppBar.apply {
            // 뒤로가기 버튼만 보이게 설정
            btnReturn.visibility = View.VISIBLE
            btnReturn.setOnClickListener {
                finish() // 메인 화면으로 돌아가기
            }

            // 알림 종 아이콘은 숨김
            bell.visibility = View.GONE
        }

        // 3. 뷰모델 및 컴포즈 화면 세팅
        val viewModel = NotificationViewModel(supabase = Supabase.client)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                NotificationScreen(viewModel = viewModel)
            }
        }
    }
}