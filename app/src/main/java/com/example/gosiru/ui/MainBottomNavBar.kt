package com.example.gosiru.ui
import com.example.gosiru.R
import android.annotation.SuppressLint
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MainBottomNavBar(
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        Pair("홈", R.drawable.ic_home),
        Pair("혜택", R.drawable.ic_map),
        Pair("프로필", R.drawable.ic_profile)
    )

    var selectedIndex by remember { mutableIntStateOf(0) }

    val activeColor = Color(0xFF3366FF)
    val inactiveColor = Color(0xFF8A909F) // 세련된 회색 적용

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp) // 컴팩트한 높이 유지
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp), // 위쪽만 라운드
                clip = false
            )
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(Color.White)
    ) {
        val tabWidth = maxWidth / tabs.size
        val indicatorWidth = 32.dp // 언더라인 너비

        // 언더라인 슬라이딩 애니메이션
        val indicatorOffset by animateDpAsState(
            targetValue = (tabWidth * selectedIndex) + (tabWidth / 2) - (indicatorWidth / 2),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "indicatorOffset"
        )

        // 파란색 언더라인을 더 밑으로 내림 (y축 오프셋을 -4.dp로 조정)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = indicatorOffset, y = (-4).dp)
                .width(indicatorWidth)
                .height(3.dp)
                .clip(RoundedCornerShape(50))
                .background(activeColor)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex

                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) activeColor else inactiveColor,
                    label = "color"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (selectedIndex != index) {
                                selectedIndex = index
                                onTabSelected(index)
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = tab.second),
                        contentDescription = tab.first,
                        modifier = Modifier.size(24.dp),
                        tint = contentColor
                    )
                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = tab.first,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor
                    )

                    // 밑줄 공간 확보를 위한 하단 여백
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
fun MainBottomNavBarPreview() {
    MainBottomNavBar(
        onTabSelected = {}
    )
}