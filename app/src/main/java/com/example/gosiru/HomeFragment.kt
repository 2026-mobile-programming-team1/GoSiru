package com.example.gosiru

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.gosiru.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isProfileDone = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 바인딩 연결
        _binding = FragmentHomeBinding.bind(view)

        updateProfileSection()

        // 테스트 버튼 클릭
        binding.BtnProfile.setOnClickListener {

            isProfileDone = !isProfileDone
            updateProfileSection()
        }

        binding.btnOpenApp.setOnClickListener {
            Log.d("HomeFragment", "지역 화폐 결제 앱 이동 버튼 클릭")
        }

        binding.btnBenefitDetail.setOnClickListener {
            Log.d("HomeFragment", "내역 보기 버튼 클릭")
        }

        binding.BtnViewAll.setOnClickListener {
            Log.d("HomeFragment", "전체 보기 버튼 클릭")
        }

        binding.btnApplication.setOnClickListener {
            Log.d("HomeFragment", "신청 하기 버튼 클릭")
        }

        binding.BtnOpenProfileEdit.setOnClickListener {
            Log.d("HomeFragment", "프로필 작성하기 버튼 클릭")
        }


        // TODO: 홈 화면 기능 작성
    }

    private fun updateProfileSection() {

        if (isProfileDone) {
            //프로필 작성시
            binding.sectionProfileDone.visibility = View.VISIBLE
            binding.sectionProfileEmpty.visibility = View.GONE

        } else {
            //프로필 미 작성시
            binding.sectionProfileEmpty.visibility = View.VISIBLE
            binding.sectionProfileDone.visibility = View.GONE
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        // 메모리 누수 방지
        _binding = null
    }
}