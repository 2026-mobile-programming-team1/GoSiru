package com.example.gosiru.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.example.gosiru.R
import com.example.gosiru.databinding.ActivityMainBinding
import com.google.firebase.messaging.FirebaseMessaging
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.gosiru.data.Notification
import com.example.gosiru.network.Supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import java.util.Locale.filter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var isProfileDone = false
    var isEditingProfile = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. 상태바/네비바 배경이 밝을 경우 글자(아이콘) 색상을 어둡게 변경
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true
        windowInsetsController.isAppearanceLightNavigationBars = true

        // 3. 내용이 시스템 바와 겹치지 않도록 안쪽 여백(Padding)을 줘서 액자처럼 밀어 넣음
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // ==========================================
        // FCM 토큰 및 권한 요청 로직 (기존 유지)
        // ==========================================
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("FCM", "토큰 가져오기 실패")
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.d("FCM_TOKEN", token)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
            }
        }

        supportFragmentManager.addOnBackStackChangedListener {
            updateAppBarByCurrentFragment()
        }

        // 첫 화면 지정
        replaceFragment(HomeFragment())
        setAppBar(R.layout.app_bar)

        // 알림 버튼 클릭
        val redDot = findViewById<View>(R.id.redDot)
        lifecycleScope.launch {
            try {
                // Supabase에서 notifications 테이블 조회
                val unreadCount = Supabase.client.from("notifications")
                    .select {
                        filter {
                            // eq("user_id", 현재_로그인한_유저_ID) // 필요시 유저 필터링 추가
                            eq("is_read", false) // 👈 핵심: 안 읽은 알림(false)만 골라내기
                        }
                    }.decodeList<Notification>().size // 개수 세기

                // 💡 2. 안 읽은 알림 개수에 따라 빨간 점 제어
                if (unreadCount > 0) {
                    redDot.visibility = View.VISIBLE  // 토스처럼 점 켜기!
                } else {
                    redDot.visibility = View.GONE     // 다 읽었으면 점 끄기!
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 에러 나면 안전하게 점을 숨김
                redDot.visibility = View.GONE
            }
        }
        // redDot.visibility = View.VISIBLE

        val btnNotification = findViewById<ImageView>(R.id.bell)
        btnNotification.setOnClickListener {

            redDot.visibility = View.GONE
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        // 하단바 클릭 시 대응 로직 수정
        binding.composeBottomNav.setContent {
            MainBottomNavBar { selectedIndex ->
                when (selectedIndex) {
                    0 -> moveFragmentWithCheck(HomeFragment()) {}
                    1 -> moveFragmentWithCheck(BenefitFragment()) {} // 👈 여기 정상 연결함
                    2 -> moveFragmentWithCheck(ProfileFragment()) {}
                }
            }
        }

        // 뒤로가기 로직 (기존 유지)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isEditingProfile) {
                    // 편집 중이면 저장 다이얼로그 띄우기
                    showSaveDialog {}
                } else {
                    // 이전 프래그먼트가 남아있으면 뒤로 가기
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        supportFragmentManager.popBackStack()
                        setAppBar(R.layout.app_bar)
                    } else {
                        // 백스택이 없으면 진짜 앱 종료 처리
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun openProfileEditFragment() {
        isEditingProfile = true
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ProfileEditFragment())
            .addToBackStack(null)
            .commit()
        setAppBar(R.layout.profile_app_bar)
    }

    private fun moveFragmentWithCheck(fragment: Fragment, onMoved: () -> Unit) {
        if (isEditingProfile) {
            showSaveDialog {
                replaceFragment(fragment)
                onMoved()
            }
        } else {
            replaceFragment(fragment)
            onMoved()
        }
    }

    private fun showSaveDialog(onMove: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("프로필 저장")
            .setMessage("변경사항을 저장하시겠습니까?")
            .setPositiveButton("저장") { _, _ ->
                isEditingProfile = false
                isProfileDone = true
                supportFragmentManager.popBackStack()
                setAppBar(R.layout.app_bar)
                onMove()
            }
            .setNegativeButton("저장 안함") { _, _ ->
                isEditingProfile = false
                supportFragmentManager.popBackStack()
                setAppBar(R.layout.app_bar)
                onMove()
            }
            .setNeutralButton("취소", null)
            .show()
    }

    fun setAppBar(layoutResId: Int) {
        binding.appBarContainer.removeAllViews()
        val appBarView = layoutInflater.inflate(layoutResId, binding.appBarContainer, true)
        val backButton = appBarView.findViewById<FrameLayout>(R.id.btnReturn)

        backButton?.setOnClickListener {
            if (isEditingProfile) {
                showSaveDialog {}
            } else {
                supportFragmentManager.popBackStack()
                setAppBar(R.layout.app_bar)
            }
        }
    }

    private fun updateAppBarByCurrentFragment() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is ProfileEditFragment) {
            setAppBar(R.layout.profile_app_bar)
        } else {
            setAppBar(R.layout.app_bar)
        }
    }
}