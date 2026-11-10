package com.easycd.nextplace.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** 글자 크기 (화면 설계서 1장): 본문 28, 버튼 32, 카드 제목 40, 질문 48, 가게 이름 64, 최소 24. */
object NpType {
    val small = TextStyle(fontSize = 24.sp, lineHeight = 32.sp)
    val body = TextStyle(fontSize = 28.sp, lineHeight = 38.sp)
    val button = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold)
    val title = TextStyle(fontSize = 36.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold)
    val cardTitle = TextStyle(fontSize = 40.sp, lineHeight = 50.sp, fontWeight = FontWeight.Bold)
    val question = TextStyle(fontSize = 48.sp, lineHeight = 58.sp, fontWeight = FontWeight.Bold)
    val placeName = TextStyle(fontSize = 64.sp, lineHeight = 74.sp, fontWeight = FontWeight.ExtraBold)
    val live = TextStyle(fontSize = 40.sp, lineHeight = 52.sp, fontWeight = FontWeight.Medium)

    val material = Typography(bodyLarge = body, bodyMedium = body, titleLarge = title, labelLarge = button)
}
