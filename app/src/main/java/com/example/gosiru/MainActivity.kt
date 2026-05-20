package com.example.gosiru

import android.content.pm.PackageManager
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
import com.kakao.vectormap.KakaoMapSdk
import io.ktor.websocket.Frame

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // 프로필 작성 여부
    var isProfileDone = false

    // 프로필 수정 중 여부
    var isEditingProfile = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 카카오맵 SDK 초기화
        val appInfo = packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA
        )

        val appKey = appInfo.metaData.getString("com.kakao.sdk.AppKey") ?: ""
        KakaoMapSdk.init(this, appKey)


        // 하단 탭
        val tabHome = findViewById<LinearLayout>(R.id.tabHome)
        val tabProfile = findViewById<LinearLayout>(R.id.tabProfile)

        // 하단 이미지
        val imgHome = findViewById<ImageView>(R.id.imgHome)
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)

        // 하단 텍스트
        val txtHome = findViewById<TextView>(R.id.txtHome)
        val txtProfile = findViewById<TextView>(R.id.txtProfile)


        supportFragmentManager.addOnBackStackChangedListener {
            updateAppBarByCurrentFragment()
        }

        // 첫 화면
        replaceFragment(HomeFragment())
        setAppBar(R.layout.app_bar)
        tabHome.isSelected = true
        imgHome.setColorFilter(Color.parseColor("#FEFCFF"))
        txtHome.setTextColor(Color.parseColor("#FEFCFF"))

        // 홈 탭
        tabHome.setOnClickListener {
            moveFragmentWithCheck(HomeFragment())

            tabHome.isSelected = true
            tabProfile.isSelected = false

            //img 와 txt변경
            imgHome.setColorFilter(Color.parseColor("#FEFCFF"))
            imgProfile.setColorFilter(Color.parseColor("#424754"))
            txtHome.setTextColor(Color.parseColor("#FEFCFF"))
            txtProfile.setTextColor(Color.parseColor("#424754"))

        }

        // 프로필 탭
        tabProfile.setOnClickListener {
            moveFragmentWithCheck(ProfileFragment())

            tabHome.isSelected = false
            tabProfile.isSelected = true

            //img 와 txt변경
            imgHome.setColorFilter(Color.parseColor("#424754"))
            imgProfile.setColorFilter(Color.parseColor("#FEFCFF"))
            txtHome.setTextColor(Color.parseColor("#424754"))
            txtProfile.setTextColor(Color.parseColor("#FEFCFF"))

        }

    }

    // 일반 화면 전환
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // 프로필 수정 화면 진입
    fun openProfileEditFragment() {

        isEditingProfile = true

        // 그 다음 edit 화면 올리기
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ProfileEditFragment())
            .addToBackStack(null)
            .commit()

        setAppBar(R.layout.profile_app_bar)
    }

    // 편집 상태 확인 후 화면 이동
    private fun moveFragmentWithCheck(fragment: Fragment) {

        if (isEditingProfile) {

            showSaveDialog {
                replaceFragment(fragment)
            }

        } else {

            replaceFragment(fragment)
        }
    }

    // 저장 여부 다이얼로그
    private fun showSaveDialog(onMove: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("프로필 저장")
            .setMessage("변경사항을 저장하시겠습니까?")
            .setPositiveButton("저장") { _, _ ->
                // 저장 로직 (DB저장 등)
                isEditingProfile = false
                isProfileDone = true

                supportFragmentManager.popBackStack()

                setAppBar(R.layout.app_bar)

                onMove() // 예약된 이동이 있다면 수행
            }
            .setNegativeButton("저장 안함") { _, _ ->
                isEditingProfile = false
                supportFragmentManager.popBackStack()
                setAppBar(R.layout.app_bar)
                setAppBar(R.layout.app_bar)
                onMove()
            }
            .setNeutralButton("취소", null)
            .show()
    }

    // 상단 AppBar 변경
    fun setAppBar(layoutResId: Int) {

        binding.appBarContainer.removeAllViews()

        val appBarView = layoutInflater.inflate(
            layoutResId,
            binding.appBarContainer,
            true
        )

        // 뒤로가기 버튼
        val backButton = appBarView.findViewById<FrameLayout>(R.id.btnReturn)

        backButton?.setOnClickListener {

            // 수정 중이면 저장 여부 확인
            if (isEditingProfile) {

                showSaveDialog {
                    // 뒤로가기 처리
                }

            } else {

                supportFragmentManager.popBackStack()

                setAppBar(R.layout.app_bar)
            }
        }
    }
    //앱바 자동 변경
    private fun updateAppBarByCurrentFragment() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        when (currentFragment) {
            is ProfileEditFragment -> {
                setAppBar(R.layout.profile_app_bar) // 수정 화면용 바
            }
            else -> {
                setAppBar(R.layout.app_bar) // 기본 바 (Home, Map, Profile 공용)
            }
        }
    }

    // 휴대폰 뒤로가기 버튼 처리
    override fun onBackPressed() {

        if (isEditingProfile) {

            showSaveDialog {
                // 뒤로가기 처리
            }

        } else {

            super.onBackPressed()

            setAppBar(R.layout.app_bar)
        }
    }

}