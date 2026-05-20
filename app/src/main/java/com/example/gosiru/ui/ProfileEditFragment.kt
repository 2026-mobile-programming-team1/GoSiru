package com.example.gosiru.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.example.gosiru.R
import com.example.gosiru.databinding.FragmentProfileEditBinding

class ProfileEditFragment : Fragment(R.layout.fragment_profile_edit) {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    private val isProfileDone: Boolean
        get() = (activity as MainActivity).isProfileDone

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 바인딩 연결
        _binding = FragmentProfileEditBinding.bind(view)

        val years = (1950..2026).map { it.toString() }

        val yearAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            years
        )

        binding.dropdownBirthYear.setAdapter(yearAdapter)
        binding.dropdownBirthYear.setOnClickListener {
            binding.dropdownBirthYear.showDropDown()
        }

        //소득 구간
        val incomeRanges = listOf(
            "0 ~ 100만원",
            "100 ~ 200만원",
            "200 ~ 300만원",
            "300 ~ 400만원",
            "400 ~ 500만원",
            "500 ~ 600만원",
            "600 ~ 700만원",
            "700 ~ 800만원",
            "800 ~ 900만원",
            "900 ~ 1000만원",
            "1000만원+"
        )

        binding.seekBar.max = 10

        binding.seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {

                    binding.tvIncomeValue.text = incomeRanges[progress]
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )

        //저장버튼
        binding.BtnOpenProfileEdit.setOnClickListener {

            val activity = activity as MainActivity

            activity.isProfileDone = true
            activity.isEditingProfile = false

            parentFragmentManager.popBackStack()

            activity.setAppBar(R.layout.app_bar)
        }

    }

    override fun onResume() {
        super.onResume()

        (activity as MainActivity).isEditingProfile = true
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (activity as MainActivity).isEditingProfile = false
        // 메모리 누수 방지
        _binding = null
    }
}