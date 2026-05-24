package com.example.gosiru.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gosiru.R
import com.example.gosiru.adapter.WelfareAdapter
import com.example.gosiru.databinding.FragmentBenefitBinding
import com.example.gosiru.network.Supabase
import com.example.gosiru.network.WelfareRepository
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class BenefitFragment : Fragment(R.layout.fragment_benefit) {

    private var _binding: FragmentBenefitBinding? = null
    private val binding get() = _binding!!

    private lateinit var welfareAdapter: WelfareAdapter

    private val mainActivity: MainActivity
        get() = requireActivity() as MainActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentBenefitBinding.bind(view)

        setupRecyclerView()
        updateProfileSection()

        binding.BtnOpenProfileEdit.setOnClickListener {
            mainActivity.replaceFragment(ProfileFragment())
            mainActivity.openProfileEditFragment()

            Log.d("BenefitFragment", "프로필 작성 화면으로 이동")
        }
    }

    override fun onResume() {
        super.onResume()
        updateProfileSection()
    }

    private fun setupRecyclerView() {
        welfareAdapter = WelfareAdapter(emptyList()) { welfareItem ->
            // 클릭 동작은 WelfareAdapter 내부에서 apply_link로 처리 중
        }

        binding.rvWelfareList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = welfareAdapter
        }
    }

    private fun updateProfileSection() {
        val isDone = mainActivity.isProfileDone

        binding.sectionProfileDone.visibility = if (isDone) View.VISIBLE else View.GONE
        binding.sectionProfileEmpty.visibility = if (isDone) View.GONE else View.VISIBLE

        if (isDone) {
            loadMatchedWelfare()
        }
    }

    private fun loadMatchedWelfare() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userId = Supabase.client.auth.currentUserOrNull()?.id

                if (userId != null) {
                    WelfareRepository.updateFcmToken(userId)
                }

                Log.d("BenefitFragment_Test", "1. 로그인 유저 ID: $userId")

                if (userId == null) {
                    Log.e("BenefitFragment_Test", "로그인된 유저가 없습니다.")
                    return@launch
                }

                val myProfile = WelfareRepository.getUserProfile(userId)
                Log.d("BenefitFragment_Test", "2. 내 프로필 정보: $myProfile")

                if (myProfile != null) {
                    val birthYear = myProfile.birthDate.take(4).toIntOrNull() ?: 2000
                    val currentAge = 2026 - birthYear

                    Log.d(
                        "BenefitFragment_Test",
                        "3. 계산된 나이: $currentAge, 직업: ${myProfile.jobStatus}, 소득: ${myProfile.incomeLevel}"
                    )

                    val matchedList = WelfareRepository.getMatchedWelfare(
                        age = currentAge,
                        jobStatus = myProfile.jobStatus ?: "",
                        incomeLevel = myProfile.incomeLevel,
                        isDisabled = myProfile.isDisabled,
                        isForeigner = myProfile.isForeigner,
                        gender = myProfile.gender
                    )

                    Log.d("BenefitFragment_Test", "4. 매칭된 혜택 개수: ${matchedList.size}개")

                    welfareAdapter.updateList(matchedList, binding.tvEmptyMessage)
                } else {
                    Log.e("BenefitFragment_Test", "DB에서 프로필 정보를 찾지 못했습니다.")
                    welfareAdapter.updateList(emptyList(), binding.tvEmptyMessage)
                }

            } catch (e: Exception) {
                Log.e("BenefitFragment_Test", "에러 발생: ${e.message}", e)
                welfareAdapter.updateList(emptyList(), binding.tvEmptyMessage)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}