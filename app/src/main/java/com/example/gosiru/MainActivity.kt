package com.example.gosiru

import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gosiru.databinding.ActivityMainBinding
import androidx.activity.OnBackPressedCallback // 상단에 import 추가
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var isProfileDone = false
    var isEditingProfile = false

    private lateinit var tabHome: LinearLayout
    private lateinit var tabProfile: LinearLayout
    private lateinit var imgHome: ImageView
    private lateinit var imgProfile: ImageView
    private lateinit var txtHome: TextView
    private lateinit var txtProfile: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

        supportFragmentManager.addOnBackStackChangedListener {
            updateAppBarByCurrentFragment()
        }

        // 첫 화면
        replaceFragment(HomeFragment())
        setAppBar(R.layout.app_bar)
        updateBottomNavUI(isHome = true)

        // 홈 탭
        tabHome.setOnClickListener {
            moveFragmentWithCheck(HomeFragment()) { updateBottomNavUI(isHome = true) }
        }

        // 프로필 탭
        tabProfile.setOnClickListener {
            moveFragmentWithCheck(ProfileFragment()) { updateBottomNavUI(isHome = false) }
        }
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

    private fun initViews() {
        tabHome = findViewById(R.id.tabHome)
        tabProfile = findViewById(R.id.tabProfile)
        imgHome = findViewById(R.id.imgHome)
        imgProfile = findViewById(R.id.imgProfile)
        txtHome = findViewById(R.id.txtHome)
        txtProfile = findViewById(R.id.txtProfile)
    }

    // 탭 색상 변경 로직을 하나로 통합
    private fun updateBottomNavUI(isHome: Boolean) {
        tabHome.isSelected = isHome
        tabProfile.isSelected = !isHome

        val activeColor = Color.parseColor("#FEFCFF")
        val inactiveColor = Color.parseColor("#424754")

        imgHome.setColorFilter(if (isHome) activeColor else inactiveColor)
        txtHome.setTextColor(if (isHome) activeColor else inactiveColor)

        imgProfile.setColorFilter(if (!isHome) activeColor else inactiveColor)
        txtProfile.setTextColor(if (!isHome) activeColor else inactiveColor)
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

    // 탭 이동 시 콜백을 받아서 UI 업데이트까지 한 번에 처리
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