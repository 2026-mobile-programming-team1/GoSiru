package com.example.gosiru.ui

import android.content.Intent
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

        // FCM 토큰 로그 확인
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM_TEST", "토큰 가져오기 실패", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM_TEST", "🔥 내 진짜 FCM 토큰: $token")
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 지역 화폐 결제 앱 이동
        binding.btnOpenApp.setOnClickListener {
            Log.d("HomeFragment", "지역 화폐 클릭")

            val chakPackageName = "com.komscochak.m2.client"
            val intent = requireContext().packageManager.getLaunchIntentForPackage(chakPackageName)

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                requireContext().startActivity(intent)
            } else {
                val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse("market://details?id=$chakPackageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                requireContext().startActivity(playStoreIntent)
            }
        }

        // 누적 혜택 내역 보기 버튼 예시 (필요시 구현)
        binding.btnBenefitDetail.setOnClickListener {
            Log.d("HomeFragment", "누적 혜택 내역 보기 클릭")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}