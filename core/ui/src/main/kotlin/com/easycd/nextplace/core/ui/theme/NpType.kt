package com.easycd.nextplace.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.sp

/** 글자 크기 (화면 설계서 1장): 본문 28, 버튼 32, 카드 제목 40, 질문 48, 가게 이름 64, 최소 24. */
object NpType {
    /** 한국어도 띄어쓰기 단위로 줄을 바꾸고(“커/피” 방지), 마지막 줄에 한 글자만 남지 않게 줄 길이를 고르게 한다. */
    private val wrap = LineBreak(LineBreak.Strategy.Balanced, LineBreak.Strictness.Normal, LineBreak.WordBreak.Phrase)

    /**
     * 띄어쓰기 단위 줄바꿈(Phrase)은 글자의 언어가 한국어일 때만 동작한다. 기기 언어가 영어여도(에뮬레이터 등)
     * 한국어 규칙으로 줄을 바꾸도록 모든 글자에 한국어를 지정한다.
     */
    private val korean = LocaleList("ko-KR")

    val small = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, lineBreak = wrap, localeList = korean)
    val body = TextStyle(fontSize = 28.sp, lineHeight = 38.sp, lineBreak = wrap, localeList = korean)
    val button = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, lineBreak = wrap, localeList = korean, fontWeight = FontWeight.Bold)
    val title = TextStyle(fontSize = 36.sp, lineHeight = 44.sp, lineBreak = wrap, localeList = korean, fontWeight = FontWeight.Bold)
    val cardTitle = TextStyle(fontSize = 40.sp, lineHeight = 50.sp, lineBreak = wrap, localeList = korean, fontWeight = FontWeight.Bold)
    val question = TextStyle(fontSize = 48.sp, lineHeight = 58.sp, lineBreak = wrap, localeList = korean, fontWeight = FontWeight.Bold)
    val placeName = TextStyle(fontSize = 64.sp, lineHeight = 74.sp, lineBreak = wrap, localeList = korean, fontWeight = FontWeight.ExtraBold)
    val live = TextStyle(fontSize = 40.sp, lineHeight = 52.sp, lineBreak = wrap, localeList = korean, fontWeight = FontWeight.Medium)

    val material = Typography(bodyLarge = body, bodyMedium = body, titleLarge = title, labelLarge = button)
}
