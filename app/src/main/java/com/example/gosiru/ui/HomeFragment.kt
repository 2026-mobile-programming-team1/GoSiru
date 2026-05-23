package com.example.gosiru.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
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

        setupHomeOnlyView()
        setupClickListeners()
        logFcmToken()
    }

    private fun setupHomeOnlyView() {
        // 기존 홈 XML 안에 있던 복지 리스트 영역을 숨김 처리
        binding.CustomBenefitHeader.visibility = View.GONE
        binding.sectionProfileEmpty.visibility = View.GONE
        binding.sectionProfileDone.visibility = View.GONE
    }

    private fun setupClickListeners() {
        binding.btnOpenApp.setOnClickListener {
            Log.d("HomeFragment", "지역 화폐 클릭")
        }
    }

    private fun logFcmToken() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM_TEST", "토큰 가져오기 실패", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.d("FCM_TEST", "내 진짜 FCM 토큰: $token")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}