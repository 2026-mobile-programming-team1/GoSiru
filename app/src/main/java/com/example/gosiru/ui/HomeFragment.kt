package com.example.gosiru.ui // 패키지명 확인!

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager // 리사이클러뷰 매니저 임포트
import com.example.gosiru.R
import com.example.gosiru.adapter.WelfareAdapter // 어댑터 임포트
import com.example.gosiru.databinding.FragmentHomeBinding
import com.example.gosiru.network.Supabase
import com.example.gosiru.network.WelfareRepository
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 👉 추가된 부분: 어댑터 변수 선언
    private lateinit var welfareAdapter: WelfareAdapter

    private val mainActivity: MainActivity
        get() = requireActivity() as MainActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // 👉 추가된 부분: 리사이클러뷰 초기 세팅 실행
        setupRecyclerView()

        updateProfileSection()

        // 프로필 작성/편집 페이지로 이동
        binding.BtnOpenProfileEdit.setOnClickListener {
            // 1. 먼저 ProfileFragment로 화면을 교체
            mainActivity.replaceFragment(ProfileFragment())
            // 2. 그 위에 EditFragment 열기
            mainActivity.openProfileEditFragment()

            Log.d("HomeFragment", "프로필 작성 화면으로 이동 (Profile 거쳐서 Edit)")
        }
        // FCM 진짜 토큰 로그로 뽑아보기
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                android.util.Log.w("FCM_TEST", "토큰 가져오기 실패", task.exception)
                return@addOnCompleteListener
            }
            // 성공하면 토큰 가져오기
            val token = task.result
            android.util.Log.d("FCM_TEST", "🔥 내 진짜 FCM 토큰: $token")
        }
        setupClickListeners()
    }
    override fun onResume() {
        super.onResume()
        updateProfileSection()
    }
    private fun setupClickListeners() {
        binding.btnOpenApp.setOnClickListener { Log.d("HomeFragment", "지역 화폐 클릭") }
    }

    // 👉 추가된 부분: 리사이클러뷰랑 어댑터 연결하는 함수
    private fun setupRecyclerView() {
        welfareAdapter = WelfareAdapter(emptyList()) { welfareItem ->
            // 클릭 동작은 이미 WelfareAdapter 내부에 구현되어 있음
        }

        // xml에 추가한 rvWelfareList와 연결
        binding.rvWelfareList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = welfareAdapter
        }
    }

    private fun updateProfileSection() {
        val isDone = mainActivity.isProfileDone
        binding.apply {
            sectionProfileDone.visibility = if (isDone) View.VISIBLE else View.GONE
            sectionProfileEmpty.visibility = if (isDone) View.GONE else View.VISIBLE
        }

        // 👉 추가된 부분: 프로필이 작성되어 있으면 맞춤 복지 데이터를 불러옴!
        if (isDone) {
            loadMatchedWelfare()
        }
    }

    private fun loadMatchedWelfare() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1번 CCTV: 유저 아이디 확인
                val userId = Supabase.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    WelfareRepository.updateFcmToken(userId)
                }
                Log.d("HomeFragment_Test", "1. 로그인 유저 ID: $userId")

                if (userId == null) {
                    Log.e("HomeFragment_Test", "🚨 로그인된 유저가 없습니다! 데이터를 못 가져와요.")
                    return@launch
                }

                // 2번 CCTV: 프로필 정보 확인
                val myProfile = WelfareRepository.getUserProfile(userId)
                Log.d("HomeFragment_Test", "2. 내 프로필 정보: $myProfile")

                if (myProfile != null) {
                    // 나이 계산 (에러 방지용 안전 처리 추가)
                    val birthYear = myProfile.birthDate.take(4).toIntOrNull() ?: 2000
                    val currentAge = 2026 - birthYear
                    Log.d("HomeFragment_Test", "3. 계산된 나이: $currentAge, 직업: ${myProfile.jobStatus}, 소득: ${myProfile.incomeLevel}")

                    // 4번 CCTV: 매칭된 리스트 가져오기
                    val matchedList = WelfareRepository.getMatchedWelfare(
                        age = currentAge,
                        jobStatus = myProfile.jobStatus?: "",
                        incomeLevel = myProfile.incomeLevel,
                        isDisabled = myProfile.isDisabled,
                        isForeigner = myProfile.isForeigner,
                        gender = myProfile.gender
                    )
                    Log.d("HomeFragment_Test", "4. 매칭된 혜택 개수: ${matchedList.size}개")

                    welfareAdapter.updateList(matchedList, binding.tvEmptyMessage)
                } else {
                    Log.e("HomeFragment_Test", "🚨 DB에서 프로필 정보를 못 찾았습니다!")
                }

            } catch (e: Exception) {
                Log.e("HomeFragment_Test", "🚨 에러 발생: ${e.message}", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}