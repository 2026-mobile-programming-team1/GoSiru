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
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.example.gosiru.R
import com.example.gosiru.databinding.ActivityMainBinding
import com.google.firebase.messaging.FirebaseMessaging
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var isProfileDone = false
    var isEditingProfile = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true
        windowInsetsController.isAppearanceLightNavigationBars = true

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

        val btnNotification = findViewById<ImageView>(R.id.bell)
        btnNotification.setOnClickListener {
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

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isEditingProfile) {
                    showSaveDialog {}
                } else {
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        supportFragmentManager.popBackStack()
                        setAppBar(R.layout.app_bar)
                    } else {
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