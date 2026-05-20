package com.example.gosiru

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.gosiru.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    private val mainActivity: MainActivity
        get() = activity as MainActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        updateProfileSection()

        // 테스트용: 프로필 상태 토글



        // 프로필 작성/편집 페이지로 이동
        binding.BtnOpenProfileEdit.setOnClickListener {
            // 1. 먼저 ProfileFragment로 화면을 교체합니다. (이때 백스택에 넣지 않아야 Home이 대체됨)
            mainActivity.replaceFragment(ProfileFragment())

            // 2. 그 위에 EditFragment를 엽니다. (이 함수 내부에서 addToBackStack이 실행됨)
            mainActivity.openProfileEditFragment()

            Log.d("HomeFragment", "프로필 작성 화면으로 이동 (Profile 거쳐서 Edit)")
        }

        setupClickListeners()
    }


    private fun setupClickListeners() {
        binding.btnOpenApp.setOnClickListener { Log.d("HomeFragment", "지역 화폐 클릭") }
    }

    private fun updateProfileSection() {
        val isDone = mainActivity?.isProfileDone ?: false

        binding.apply {
            sectionProfileDone.visibility = if (isDone) View.VISIBLE else View.GONE
            sectionProfileEmpty.visibility = if (isDone) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}