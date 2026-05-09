package com.example.gosiru

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.gosiru.databinding.FragmentProfileBinding

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 바인딩 연결
        _binding = FragmentProfileBinding.bind(view)

        // TODO: 프로필 화면 기능 작성
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 메모리 누수 방지
        _binding = null
    }
}