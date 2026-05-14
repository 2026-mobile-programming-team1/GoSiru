package com.example.gosiru

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.gosiru.databinding.FragmentProfileBinding

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

        (activity as MainActivity).setAppBar(R.layout.app_bar)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 메모리 누수 방지
        _binding = null
    }
}