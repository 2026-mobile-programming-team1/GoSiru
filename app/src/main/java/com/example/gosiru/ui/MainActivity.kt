package com.example.gosiru.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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

    private enum class BottomTab {
        HOME, BENEFIT, PROFILE
    }

    private lateinit var tabHome: LinearLayout
    private lateinit var tabBenefit: LinearLayout
    private lateinit var tabProfile: LinearLayout

    private lateinit var imgHome: ImageView
    private lateinit var imgBenefit: ImageView
    private lateinit var imgProfile: ImageView

    private lateinit var txtHome: TextView
    private lateinit var txtBenefit: TextView
    private lateinit var txtProfile: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // FCM 토큰 확인
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("FCM", "토큰 가져오기 실패")
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.d("FCM_TOKEN", token)
            }

        // Android 13 이상 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
            }
        }

        initViews()

        supportFragmentManager.addOnBackStackChangedListener {
            updateAppBarByCurrentFragment()
        }

        // 첫 화면
        replaceFragment(HomeFragment())
        setAppBar(R.layout.app_bar)
        updateBottomNavUI(BottomTab.HOME)

        // 홈 탭
        tabHome.setOnClickListener {
            moveFragmentWithCheck(HomeFragment()) {
                updateBottomNavUI(BottomTab.HOME)
            }
        }

        // 혜택 탭
        tabBenefit.setOnClickListener {
            moveFragmentWithCheck(BenefitFragment()) {
                updateBottomNavUI(BottomTab.BENEFIT)
            }
        }

        // 프로필 탭
        tabProfile.setOnClickListener {
            moveFragmentWithCheck(ProfileFragment()) {
                updateBottomNavUI(BottomTab.PROFILE)
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

    private fun initViews() {
        tabHome = findViewById(R.id.tabHome)
        tabBenefit = findViewById(R.id.tabBenefit)
        tabProfile = findViewById(R.id.tabProfile)

        imgHome = findViewById(R.id.imgHome)
        imgBenefit = findViewById(R.id.imgBenefit)
        imgProfile = findViewById(R.id.imgProfile)

        txtHome = findViewById(R.id.txtHome)
        txtBenefit = findViewById(R.id.txtBenefit)
        txtProfile = findViewById(R.id.txtProfile)
    }

    private fun updateBottomNavUI(selectedTab: BottomTab) {
        val isHome = selectedTab == BottomTab.HOME
        val isBenefit = selectedTab == BottomTab.BENEFIT
        val isProfile = selectedTab == BottomTab.PROFILE

        tabHome.isSelected = isHome
        tabBenefit.isSelected = isBenefit
        tabProfile.isSelected = isProfile

        val activeColor = Color.parseColor("#FEFCFF")
        val inactiveColor = Color.parseColor("#424754")

        imgHome.setColorFilter(if (isHome) activeColor else inactiveColor)
        txtHome.setTextColor(if (isHome) activeColor else inactiveColor)

        imgBenefit.setColorFilter(if (isBenefit) activeColor else inactiveColor)
        txtBenefit.setTextColor(if (isBenefit) activeColor else inactiveColor)

        imgProfile.setColorFilter(if (isProfile) activeColor else inactiveColor)
        txtProfile.setTextColor(if (isProfile) activeColor else inactiveColor)
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

    fun openBenefitFragment() {
        moveFragmentWithCheck(BenefitFragment()) {
            updateBottomNavUI(BottomTab.BENEFIT)
        }
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

        val appBarView = layoutInflater.inflate(
            layoutResId,
            binding.appBarContainer,
            true
        )

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