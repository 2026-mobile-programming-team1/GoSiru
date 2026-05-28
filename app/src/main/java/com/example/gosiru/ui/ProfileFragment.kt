package com.example.gosiru.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gosiru.R
import com.example.gosiru.databinding.FragmentProfileBinding
import com.example.gosiru.network.Supabase
import com.example.gosiru.network.WelfareRepository
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val isProfileDone: Boolean
        get() = (activity as MainActivity).isProfileDone

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 바인딩 연결
        _binding = FragmentProfileBinding.bind(view)

        updateProfileSection()

        // TODO: 프로필 화면 기능 작성
        binding.BtnOpenProfileEdit.setOnClickListener {

            (activity as MainActivity).openProfileEditFragment()
            Log.d("Profile", "수정 버튼 탭")
        }

        binding.BtnProfileEdit.setOnClickListener {

            (activity as MainActivity).openProfileEditFragment()
        }

    }

    private fun updateProfileSection() {

        if (isProfileDone) {

            binding.profileStatus.visibility = View.VISIBLE
            binding.NoProfileSection.visibility = View.GONE

        } else {

            binding.NoProfileSection.visibility = View.VISIBLE
            binding.profileStatus.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        val mainAct = activity as MainActivity

        // 현재 프로필 편집 모드(isEditingProfile == true)가 아닐 때만 서버에서 데이터를 조회하도록 방어
        if (!mainAct.isEditingProfile) {
            refreshProfileStatus()
        }
    }

    private fun refreshProfileStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            val userId = Supabase.client.auth.currentUserOrNull()?.id ?: return@launch
            val profile = WelfareRepository.getUserProfile(userId)

            if (profile != null) {
                (activity as MainActivity).isProfileDone = true
                binding.profileStatus.visibility = View.VISIBLE
                binding.NoProfileSection.visibility = View.GONE
                //
                binding.tvUserName.text = profile.name ?: "이름 없음"
                binding.tvJob.text = profile.jobStatus ?: "-"
                binding.tvHouseholdType.text = when (profile.householdCount) {
                    1 -> "1인 가구"
                    2 -> "2인 가구"
                    else -> "3인 이상 가구"
                }
                binding.tvIncomeLevel.text = "중위소득 ${profile.incomeLevel * 10 + 100}% 이하"
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 메모리 누수 방지
        _binding = null
    }
}