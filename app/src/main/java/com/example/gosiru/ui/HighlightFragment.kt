package com.example.gosiru.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gosiru.R
import com.example.gosiru.adapter.HighlightAdapter
import com.example.gosiru.databinding.FragmentHighlightBinding
import com.example.gosiru.network.WelfareRepository
import kotlinx.coroutines.launch

class HighlightFragment : Fragment(R.layout.fragment_highlight) {

    private var _binding: FragmentHighlightBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HighlightAdapter
    private var currentTab = "NEW"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHighlightBinding.bind(view)

        // HomeFragment에서 버튼 클릭 시 넘겨준 탭 타입 받기 (기본값: NEW)
        arguments?.getString("TAB_TYPE")?.let {
            currentTab = it
        }

        setupRecyclerView()
        setupClickListeners()

        // 초기 탭 세팅 및 DB 데이터 로드
        if (currentTab == "NEW") {
            selectNewTab()
        } else {
            selectIncreasedTab()
        }
    }

    private fun setupRecyclerView() {
        adapter = HighlightAdapter(emptyList(), currentTab)
        binding.rvHighlight.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHighlight.adapter = adapter
    }

    private fun setupClickListeners() {
        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 신규 정책 탭 클릭
        binding.tabNew.setOnClickListener {
            if (currentTab != "NEW") {
                currentTab = "NEW"
                selectNewTab()
            }
        }

        // 인상된 지원금 탭 클릭
        binding.tabIncreased.setOnClickListener {
            if (currentTab != "INCREASED") {
                currentTab = "INCREASED"
                selectIncreasedTab()
            }
        }
    }

    private fun selectNewTab() {
        // 탭 활성화 UI 처리 (파란색 + 굵게)
        binding.tabNew.setTextColor(Color.parseColor("#3182F6"))
        binding.tabNew.setTypeface(null, Typeface.BOLD)
        binding.tabNew.setBackgroundResource(R.drawable.bg_tab_indicator_blue)

        // 탭 비활성화 UI 처리 (회색 + 얇게)
        binding.tabIncreased.setTextColor(Color.parseColor("#8B95A1"))
        binding.tabIncreased.setTypeface(null, Typeface.NORMAL)
        binding.tabIncreased.background = null

        loadDataFromDB("NEW")
    }

    private fun selectIncreasedTab() {
        // 탭 활성화 UI 처리 (찐회색 + 굵게)
        binding.tabIncreased.setTextColor(Color.parseColor("#191F28"))
        binding.tabIncreased.setTypeface(null, Typeface.BOLD)
        binding.tabIncreased.setBackgroundResource(R.drawable.bg_tab_indicator_blue)

        // 탭 비활성화 UI 처리 (회색 + 얇게)
        binding.tabNew.setTextColor(Color.parseColor("#8B95A1"))
        binding.tabNew.setTypeface(null, Typeface.NORMAL)
        binding.tabNew.background = null

        loadDataFromDB("INCREASED")
    }

    private fun loadDataFromDB(type: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val list = WelfareRepository.getHighlightWelfare(type)
                adapter.updateData(list, type)
            } catch (e: Exception) {
                Log.e("HighlightFragment", "데이터 로드 실패", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}