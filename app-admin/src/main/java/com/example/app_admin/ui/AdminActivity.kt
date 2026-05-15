package com.example.app_admin.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app_admin.BuildConfig
import com.example.app_admin.data.WelfareRepository
import com.example.app_admin.viewmodel.AdminViewModel
import com.example.app_admin.viewmodel.AdminViewModelFactory
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {

    // 뷰모델 생성 (Factory 사용)
    private lateinit var viewModel: AdminViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 아직 디자인이 없으므로 setContentView는 생략하거나 기본값 유지

        // 1. 슈파베이스 초기화
        val supabase = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Postgrest.Companion)
            install(Auth)
        }

        // 2. 레포지토리 및 뷰모델 연결
        val repository = WelfareRepository(supabase.postgrest, supabase.auth)
        val factory = AdminViewModelFactory(repository)
        viewModel = viewModels<AdminViewModel> { factory }.value

        // 3. 결과 관찰 (성공/실패 여부 로그 찍기)
        observeViewModel()

        // 4. 즉시 테스트 데이터 전송 (프론트 버튼 누른 셈 치고)
        Log.d("ADMIN_TEST", "테스트 데이터 전송 시작...")
        viewModel.registerWelfare(
            title = "시흥시 대학생 알바 모집",
            content = "방학 기간 동안 시청에서 근무할 대학생들을 모집합니다.",
            minAge = 20,
            maxAge = 25,
            job = "대학생",
            income = null,
            gender = "전체",
            disabled = false,
            foreigner = false
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // 로딩 상태 관찰
            viewModel.isLoading.collect { isLoading ->
                if (isLoading) Log.d("ADMIN_TEST", "서버에 올리는 중...")
            }
        }

        lifecycleScope.launch {
            // 업로드 결과 관찰
            viewModel.uploadStatus.collect { isSuccess ->
                when (isSuccess) {
                    true -> Log.d("ADMIN_TEST", "✅ DB 저장 성공! 슈파베이스 확인해봐.")
                    false -> Log.e("ADMIN_TEST", "❌ DB 저장 실패... RLS나 테이블 설정 확인!")
                    null -> {} // 초기 상태
                }
            }
        }
    }
}