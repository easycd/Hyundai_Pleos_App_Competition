package com.easycd.nextplace.core.ui.drive

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.compositionLocalOf

/** 주행 제한 상태 (CarUxRestrictions). null = 아직 모름 → 제한으로 본다 (화면 설계서 S-12). */
val LocalDriveRestricted = compositionLocalOf<Boolean?> { null }

/** 세로 창(1칸)이면 true: 말하기 버튼 하단 가운데, 카드는 위 (화면 설계서 1장 세로 창). */
val LocalPortrait = compositionLocalOf { false }

/**
 * 가로 창인데 설계 기준(1280dp)보다 많이 좁으면 true (폴더블 에뮬레이터 · 좁은 분할 창 등).
 * 글자 크기는 그대로 두고, 칸 비율과 줄 수만 바꿔 글자가 잘리지 않게 한다.
 */
val LocalCompact = compositionLocalOf { false }

/** 어느 화면에서나 말하기 버튼 → S-02a (S-07b · S-12 · S-15). app 이 SaveRunner 로 연결한다. */
val LocalSpeak = staticCompositionLocalOf<() -> Unit> { {} }

fun Boolean?.isRestricted(): Boolean = this != false
