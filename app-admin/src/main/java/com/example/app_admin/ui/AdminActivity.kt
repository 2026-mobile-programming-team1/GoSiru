package com.example.app_admin.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app_admin.BuildConfig
import com.example.app_admin.R
import com.example.app_admin.data.WelfareRepository
import com.example.app_admin.databinding.ActivityAdminBinding
import com.example.app_admin.databinding.FragmentAdminRegisterBinding
import com.example.app_admin.viewmodel.AdminViewModel
import com.example.app_admin.viewmodel.AdminViewModelFactory
import com.google.android.material.chip.Chip
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: FragmentAdminRegisterBinding
    private lateinit var viewModel: AdminViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 바인딩 및 레이아웃 설정
        binding = FragmentAdminRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. 슈파베이스 초기화
        val supabase = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Postgrest.Companion)
            install(Auth)
        }

        // 3. 레포지토리 및 뷰모델 연결
        val repository = WelfareRepository(supabase.postgrest, supabase.auth)
        val factory = AdminViewModelFactory(repository)
        viewModel = viewModels<AdminViewModel> { factory }.value

        // 4. 관찰자 설정 (결과 로그 확인)
        observeViewModel()

        // 5. 버튼 클릭 리스너 설정
        binding.btnSubmit.setOnClickListener {
            registerData()
        }
    }

    private fun registerData() {
        // 1. ChipGroup에서 선택된 텍스트 가져오기
        val selectedId = binding.cgJob.checkedChipId
        val selectedJob = if (selectedId != -1) {
            findViewById<Chip>(selectedId).text.toString()
        } else {
            null
        }

        // 2. 성별 추출
        val gender = when (binding.radioGender.checkedRadioButtonId) {
            R.id.rbMale -> "남성"
            R.id.rbFemale -> "여성"
            else -> "전체"
        }

        // 3. 뷰모델에 데이터 전달
        viewModel.registerWelfare(
            title = binding.etTitle.text.toString(),
            content = binding.etContent.text.toString(),
            applyLink = binding.etLink.text.toString(),
            minAge = binding.etMinAge.text.toString().toIntOrNull(),
            maxAge = binding.etMaxAge.text.toString().toIntOrNull(),
            job = selectedJob,
            income = binding.etIncomeLimit.text.toString().toIntOrNull(),
            gender = gender,
            disabled = binding.switchDisabled.isChecked,
            foreigner = binding.switchForeigner.isChecked
        )
    }

    private fun observeViewModel() {
        // 업로드 결과 관찰
        lifecycleScope.launch {
            viewModel.uploadStatus.collect { isSuccess ->
                when (isSuccess) {
                    true -> {
                        Toast.makeText(this@AdminActivity, "등록 성공!", Toast.LENGTH_SHORT).show()
                        Log.d("ADMIN_TEST", "✅ DB 저장 성공!")
                        // 1. 성공 시 입력 폼 초기화
                        clearInputs()

                        // 2. 상태를 null로 초기화하여 다음 번에도 이벤트가 발생하게 만듦
                        viewModel.resetStatus()
                    }
                    false -> {
                        Toast.makeText(this@AdminActivity, "등록 실패..", Toast.LENGTH_SHORT).show()
                        Log.e("ADMIN_TEST", "❌ DB 저장 실패!")
                        viewModel.resetStatus()
                    }
                    null -> {}
                }
            }
        }

        // 로딩 상태 관찰
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    Log.d("ADMIN_TEST", "서버에 업로드 중...")
                }
            }
        }
    }
    private fun clearInputs() {
        // 1. 텍스트 입력칸 초기화
        binding.etTitle.text?.clear()
        binding.etContent.text?.clear()
        binding.etAmount.text?.clear()
        binding.etLink.text?.clear()
        binding.etMinAge.text?.clear()
        binding.etMaxAge.text?.clear()
        binding.etIncomeLimit.text?.clear()

        // 2. 직업 칩 그룹 초기화 (선택 해제)
        binding.cgJob.clearCheck()

        // 3. 성별 라디오 버튼 초기화 (XML 기본값인 '전체'로 되돌림)
        binding.radioGender.check(R.id.rbAll)

        // 4. 스위치 초기화 (누락되었던 switchBenefit 추가)
        binding.switchBenefit.isChecked = false
        binding.switchDisabled.isChecked = false
        binding.switchForeigner.isChecked = false
    }
}
