package com.example.gosiru.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gosiru.R
import com.example.gosiru.databinding.ActivityMainBinding
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var isProfileDone = false
    var isEditingProfile = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // 알림 권한 요청 (Android 13 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
            }
        }
        // ==========================================

        supportFragmentManager.addOnBackStackChangedListener {
            updateAppBarByCurrentFragment()
        }

        // 첫 화면
        replaceFragment(HomeFragment())
        setAppBar(R.layout.app_bar)

        // 알림 버튼 클릭
        val btnNotification = findViewById<ImageView>(R.id.bell)
        btnNotification.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        // 💥 [여기서부터 핵심!] Compose 네비게이션 바 연결 💥
        binding.composeBottomNav.setContent {
            MainBottomNavBar { selectedIndex ->
                when (selectedIndex) {
                    0 -> moveFragmentWithCheck(HomeFragment()) {}
                    1 -> {
                        // 혜택 탭 프래그먼트 생기면 여기에 넣기 (예: moveFragmentWithCheck(BenefitFragment()) {})
                    }
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