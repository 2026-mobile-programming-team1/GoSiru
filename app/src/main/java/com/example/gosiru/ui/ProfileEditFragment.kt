package com.example.gosiru.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gosiru.R
import com.example.gosiru.databinding.FragmentProfileEditBinding
import com.example.gosiru.model.UserProfile
import com.example.gosiru.network.Supabase
import com.example.gosiru.network.WelfareRepository
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class ProfileEditFragment : Fragment(R.layout.fragment_profile_edit) {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileEditBinding.bind(view)

        setupUI()
        loadMyProfileData() //  기존 데이터 불러오기 추가

        // 저장 버튼
        // 저장 버튼 클릭 시
        binding.BtnOpenProfileEdit.setOnClickListener {
            // 팝업창(다이얼로그) 띄우기
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("프로필 저장")
                .setMessage("입력하신 정보로 프로필을 저장하시겠습니까?")
                .setPositiveButton("저장") { _, _ ->
                    // 사용자가 '저장'을 누르면 그때 진짜 저장 실행!
                    saveProfile()
                }
                .setNegativeButton("취소", null) // 취소 누르면 아무 일도 안 일어남
                .show()
        }
    }

    private fun loadExistingProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            val userId = Supabase.client.auth.currentUserOrNull()?.id ?: return@launch
            val profile = WelfareRepository.getUserProfile(userId)

            profile?.let {
                // 기존 데이터가 있으면 UI에 채워넣기
                binding.dropdownBirthYear.setText(it.birthDate.take(4), false)
                binding.seekBar.progress = it.incomeLevel
                // 필요 시 성별, 직업 등 칩 선택 상태도 여기서 복구 가능
            }
        }
    }

    private fun saveProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userId = Supabase.client.auth.currentUserOrNull()?.id ?: return@launch
                val name = binding.etName.text.toString()
                // 데이터 추출
                val birthYear = binding.dropdownBirthYear.text.toString().filter { it.isDigit() }
                val birthDate = if (birthYear.isNotEmpty()) "$birthYear-01-01" else "2000-01-01"
                val incomeLevel = binding.seekBar.progress

                // 칩 그룹에서 선택된 값 가져오기
                val genderChip = binding.cgGender.findViewById<com.google.android.material.chip.Chip>(binding.cgGender.checkedChipId)
                val gender = genderChip?.text?.toString() ?: "남성"
                val jobChip = binding.cgJob.findViewById<com.google.android.material.chip.Chip>(binding.cgJob.checkedChipId)
                val jobStatus = jobChip?.text?.toString()?: "무직"
                val householdChip = binding.cgHousehold.findViewById<com.google.android.material.chip.Chip>(binding.cgHousehold.checkedChipId)
                val householdCount = when (householdChip?.text?.toString()) {
                    "1인 가구" -> 1
                    "2인 가구" -> 2
                    else -> 3
                }

                val profile = UserProfile(
                    id = userId,
                    name = name,
                    birthDate = birthDate,
                    gender = gender,
                    jobStatus = jobStatus,
                    incomeLevel = incomeLevel,
                    householdCount = householdCount, // DB에 컬럼 추가했는지 확인!
                )

                // DB 저장 요청
                val isSuccess = WelfareRepository.saveUserProfile(profile)

                if (isSuccess) {
                    Log.d("ProfileEdit", " 프로필 DB 저장 성공!")

                    val mainAct = activity as MainActivity
                    //  화면 닫기 전에 무조건 상태부터 false로 변경!
                    mainAct.isProfileDone = true
                    mainAct.isEditingProfile = false

                    // 그 다음 화면을 닫아야 경고창이 안 뜸
                    parentFragmentManager.popBackStack()
                    mainAct.setAppBar(R.layout.app_bar)
                } else {
                    Log.e("ProfileEdit", " 프로필 DB 저장 실패")
                }
            } catch (e: Exception) {
                Log.e("ProfileEdit", "저장 실패", e)
            }
        }
    }

    private fun setupUI() {
        val years = (1950..2026).map { it.toString() }
        binding.dropdownBirthYear.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, years))
        binding.dropdownBirthYear.setOnClickListener { binding.dropdownBirthYear.showDropDown() }
        binding.seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                // progress 값(0~10)에 따라 텍스트 변경. (기획에 맞게 텍스트는 수정해서 써)
                val incomeText = when (progress) {
                    0 -> "소득 없음"
                    1 -> "100만원 이하"
                    2 -> "100만원 ~ 200만원"
                    3 -> "200만원 ~ 300만원"
                    4 -> "300만원 ~ 400만원"
                    5 -> "400만원 ~ 500만원"
                    6 -> "500만원 ~ 600만원"
                    7 -> "600만원 ~ 700만원"
                    8 -> "700만원 ~ 800만원"
                    9 -> "800만원 ~ 1,000만원"
                    10 -> "1,000만원+"
                    else -> "소득 구간 선택"
                }
                binding.tvIncomeValue.text = incomeText
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun loadMyProfileData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userId = Supabase.client.auth.currentUserOrNull()?.id ?: return@launch
                val profile = WelfareRepository.getUserProfile(userId)

                // 뷰가 유효하고 프로필 데이터가 존재할 때만 실행
                if (_binding != null && profile != null) {
                    // 이름 셋팅
                    binding.etName.setText(profile.name ?: "")// 출생 연도 글자 세팅
                    binding.dropdownBirthYear.setText(profile.birthDate.take(4), false)

                    // 소득 수준 시크바 세팅
                    binding.seekBar.progress = profile.incomeLevel

                    // 성별 칩 그룹 상태 복구
                    when (profile.gender) {
                        "남성" -> binding.cgGender.check(R.id.genderMale)
                        "여성" -> binding.cgGender.check(R.id.genderFemale)
                    }

                    // 직업 칩 그룹 상태 복구
                    when (profile.jobStatus) {
                        "학생" -> binding.cgJob.check(R.id.jobStudent)
                        "직장인" -> binding.cgJob.check(R.id.jobWorker)
                        "전문직" -> binding.cgJob.check(R.id.jobPro)
                        "프리랜서" -> binding.cgJob.check(R.id.jobFree)
                        "취업준비생" -> binding.cgJob.check(R.id.jobSeeker)
                    }

                    // 장애 여부 칩 그룹 상태 복구
                    if (profile.isDisabled) {
                        binding.cgDisability.check(R.id.disabilityYes)
                    } else {
                        binding.cgDisability.check(R.id.disabilityNo)
                    }

                    // 국적 칩 그룹 상태 복구
                    if (profile.isForeigner) {
                        binding.cgNationality.check(R.id.NationalityForeign)
                    } else {
                        binding.cgNationality.check(R.id.NationalityLocal)
                    }

                // 가구형태 칩 그룹 상태 복구  ← 여기 추가
                    when (profile.householdCount) {
                        1 -> binding.cgHousehold.check(R.id.house1)
                        2 -> binding.cgHousehold.check(R.id.house2)
                        else -> binding.cgHousehold.check(R.id.house3)
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileEdit", "프로필 불러오기 실패", e)
            }
        }
    }


}