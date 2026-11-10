package com.easycd.nextplace.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.Density
import com.android.resources.ScreenOrientation
import com.easycd.nextplace.core.domain.model.AppSettings
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.domain.model.PoiCandidate
import com.easycd.nextplace.core.domain.model.SharedPlace
import com.easycd.nextplace.core.domain.model.Side
import com.easycd.nextplace.core.domain.model.UserMessage
import com.easycd.nextplace.core.domain.voice.VoicePrompt
import com.easycd.nextplace.core.ui.component.MapPin
import com.easycd.nextplace.core.ui.component.PinTone
import com.easycd.nextplace.core.ui.component.ToastHost
import com.easycd.nextplace.core.ui.drive.DriveLockScreen
import com.easycd.nextplace.core.ui.drive.LocalDriveRestricted
import com.easycd.nextplace.core.ui.drive.LocalPortrait
import com.easycd.nextplace.core.ui.theme.LocalReduceMotion
import com.easycd.nextplace.core.ui.theme.NextPlaceTheme
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.feature.myplaces.ui.common.PlaceRowModel
import com.easycd.nextplace.feature.myplaces.ui.detail.PlaceDetailScreen
import com.easycd.nextplace.feature.myplaces.ui.detail.PlaceDetailUiState
import com.easycd.nextplace.feature.myplaces.ui.edit.PlaceEditScreen
import com.easycd.nextplace.feature.myplaces.ui.edit.PlaceEditUiState
import com.easycd.nextplace.feature.myplaces.ui.list.MyPlacesScreen
import com.easycd.nextplace.feature.myplaces.ui.list.MyPlacesUiState
import com.easycd.nextplace.feature.myplaces.ui.resolve.ResolveScreen
import com.easycd.nextplace.feature.myplaces.ui.resolve.ResolveUiState
import com.easycd.nextplace.feature.myplaces.ui.search.DirectSearchScreen
import com.easycd.nextplace.feature.myplaces.ui.search.DirectSearchUiState
import com.easycd.nextplace.feature.recommend.session.RecCard
import com.easycd.nextplace.feature.recommend.session.RecommendState
import com.easycd.nextplace.feature.recommend.ui.RecommendScreen
import com.easycd.nextplace.feature.recommend.ui.RecommendUiState
import com.easycd.nextplace.feature.remind.pipeline.RemindState
import com.easycd.nextplace.feature.remind.ui.RemindCard
import com.easycd.nextplace.feature.remind.ui.RemindUiState
import com.easycd.nextplace.feature.settings.ui.SettingsScreen
import com.easycd.nextplace.feature.settings.ui.SettingsUiState
import com.easycd.nextplace.feature.share.ui.receive.ImportRow
import com.easycd.nextplace.feature.share.ui.receive.ImportScreen
import com.easycd.nextplace.feature.share.ui.receive.ImportUiState
import com.easycd.nextplace.feature.share.ui.send.ShareRowModel
import com.easycd.nextplace.feature.share.ui.send.ShareScreen
import com.easycd.nextplace.feature.share.ui.send.ShareUiState
import com.easycd.nextplace.feature.visit.note.VisitNoteState
import com.easycd.nextplace.feature.visit.ui.VisitNoteCard
import com.easycd.nextplace.feature.visit.ui.VisitNoteUiState
import com.easycd.nextplace.feature.voicesave.ui.confirm.ConfirmCard
import com.easycd.nextplace.feature.voicesave.ui.confirm.ConfirmUiState
import com.easycd.nextplace.feature.voicesave.ui.home.HomeScreen
import com.easycd.nextplace.feature.voicesave.ui.home.HomeUiState
import com.easycd.nextplace.feature.voicesave.ui.home.LatestCard
import com.easycd.nextplace.feature.voicesave.ui.home.LatestChip
import com.easycd.nextplace.feature.voicesave.ui.home.LocationLabel
import com.easycd.nextplace.feature.voicesave.ui.listen.ListenOverlay
import com.easycd.nextplace.feature.voicesave.ui.listen.ListenPhase
import com.easycd.nextplace.feature.voicesave.ui.listen.ListenUiState
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

/**
 * 화면 설계서 6장 그림(docs/screens/S-xx.png)과 나란히 놓고 보는 렌더링 (1280×1080dp 가로 창, 다크).
 * 기록: ./gradlew -Pnextplace.screens=true :tools:screenshots:recordPaparazziDebug
 */
class ScreensTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig(
            screenWidth = 1280, screenHeight = 1080, xdpi = 160, ydpi = 160,
            density = Density.MEDIUM, orientation = ScreenOrientation.LANDSCAPE,
        ),
        maxPercentDifference = 0.1,
    )

    private val now = System.currentTimeMillis()
    private val day = 86_400_000L
    private val here = GeoPoint(37.5000, 127.0300)

    private fun shot(dark: Boolean = true, restricted: Boolean = false, content: @Composable () -> Unit) {
        paparazzi.snapshot {
            CompositionLocalProvider(LocalDriveRestricted provides restricted, LocalPortrait provides false, LocalReduceMotion provides true) {
                NextPlaceTheme(dark = dark) {
                    Box(Modifier.fillMaxSize().background(Np.colors.background)) { content() }
                }
            }
        }
    }

    private val home = HomeUiState(
        locationLabel = LocationLabel("테헤란로", "강남역 방향"),
        total = 12, unconfirmed = 3, nearbyCount = 2,
        latest = LatestCard("p1", "서울갈비", "고깃집 · 120m", false, now - 60_000, LatestChip.SAVED, 0, false),
    )

    private fun cand(n: String, c: String, d: Double, side: Side = Side.RIGHT) =
        PoiCandidate("kakao", n, n, "음식점 > $c", "서울 강남구 테헤란로 1", 37.5005, 127.0305, d, side)

    @Test fun s01_home() = shot { HomeScreen(home) {} }
    @Test fun s01_home_light() = shot(dark = false) { HomeScreen(home) {} }
    @Test fun s01b_empty() = shot { HomeScreen(HomeUiState(locationLabel = LocationLabel("테헤란로", "강남역 방향"))) {} }

    @Test fun s02a_listening() = shot { HomeScreen(home) {}; ListenOverlay(ListenUiState(ListenPhase.LISTENING, "방금 지난 빨간 간판 고깃집", 0.6f)) {} }
    @Test fun s02b_understanding() = shot { HomeScreen(home) {}; ListenOverlay(ListenUiState(ListenPhase.UNDERSTANDING, "방금 지난 빨간 간판 고깃집 저장해줘")) {} }
    @Test fun s02c_retry() = shot { HomeScreen(home) {}; ListenOverlay(ListenUiState(ListenPhase.RETRY)) {} }
    @Test fun s14_searching() = shot { HomeScreen(home) {}; ListenOverlay(ListenUiState(ListenPhase.SEARCHING, "방금 지난 빨간 간판 고깃집 저장해줘", target = GeoPoint(37.4990, 127.0290), current = here)) {} }

    @Test fun s03_confirm() = shot { HomeScreen(home) {}; ConfirmCard(ConfirmUiState(true, "k", false, "서울갈비", "고깃집", 120, false, Side.RIGHT, "테헤란로 123", now)) {} }
    @Test fun s03b_second() = shot { HomeScreen(home) {}; ConfirmCard(ConfirmUiState(true, "k2", true, "한우마을", "고깃집", 160, false, Side.LEFT, null, now)) {} }

    @Test fun s04a_saved() = shot { HomeScreen(home) {}; ToastHost(flowOf(UserMessage.Saved("서울갈비")), 60.dp, 140.dp) }
    @Test fun s04b_unconfirmed() = shot { HomeScreen(home) {}; ToastHost(flowOf(UserMessage.SavedLocationOnly("테헤란로")), 60.dp, 140.dp) }

    @Test fun s05_remind() = shot { HomeScreen(home) {}; RemindCard(RemindUiState(RemindState.Asking("서울갈비", VoicePrompt.SavedWhen.LAST_WEEK, 300, true, Side.RIGHT, now))) {} }
    @Test fun s05b_routing() = shot { HomeScreen(home) {}; RemindCard(RemindUiState(RemindState.Routing("서울갈비", false))) {} }

    private val recCards = listOf(
        RecCard("1", 1, "서울갈비", "고깃집", 1200.0, null, now - 8 * day, 0),
        RecCard("2", 2, "카페 노을", "카페", 2000.0, "괜찮았어", now - 20 * day, 1),
        RecCard("3", 3, "국밥집", "국밥", 3400.0, null, now - 13 * day, 0),
    )
    @Test fun s06_recommend() = shot { RecommendScreen(RecommendUiState(RecommendState.Showing(null, recCards, 0, readingIndex = 1))) {} }
    @Test fun s06b_none() = shot { RecommendScreen(RecommendUiState(RecommendState.Empty("국밥", VoicePrompt.NoneReason.NONE_10KM))) {} }
    @Test fun s06c_selected() = shot {
        RecommendScreen(RecommendUiState(RecommendState.Showing(null, recCards, 0, selectedIndex = 1))) {}
        ToastHost(flowOf(UserMessage.Routing("할머니국밥", false)), 60.dp, 140.dp)
    }

    private val rows = listOf(
        PlaceRowModel("1", "서울갈비", "고깃집", 1200.0, now - 3 * day, 2, false, false),
        PlaceRowModel("2", "할머니국밥?", "국밥", 2400.0, now - day, 0, true, false),
        PlaceRowModel("3", "카페 온도", "카페", 3100.0, now - 10 * day, 0, false, false),
        PlaceRowModel("4", null, null, null, now - 2 * day, 0, true, false),
    )
    private val pins = listOf(
        MapPin("1", GeoPoint(37.5050, 127.0350), PinTone.VISITED),
        MapPin("2", GeoPoint(37.4950, 127.0250), PinTone.UNCONFIRMED),
        MapPin("3", GeoPoint(37.5080, 127.0200), PinTone.SAVED),
    )
    @Test fun s07_myplaces() = shot { MyPlacesScreen(MyPlacesUiState(true, 12, 3, 4, rows, pins, here)) {} }
    @Test fun s07b_empty() = shot { MyPlacesScreen(MyPlacesUiState(true, 0)) {} }

    private val detail = PlaceDetailUiState(
        true, "1", "서울갈비", "고깃집", 1200.0, "서울 강남구 테헤란로 123", now - 7 * day, 2, "줄 서 있던",
        listOf("고기가 맛있었어"), "방금 지난 빨간 간판 고깃집 저장해줘", GeoPoint(37.505, 127.035),
    )
    @Test fun s08_detail() = shot { PlaceDetailScreen(detail) {} }
    @Test fun s08b_edit() = shot { PlaceEditScreen(PlaceEditUiState(true, "서울갈비", "고깃집", "줄 서 있던", listOf("고깃집", "국밥", "카페", "빵집", "파스타"), false)) {} }
    @Test fun s08c_delete() = shot { PlaceDetailScreen(detail.copy(confirmDelete = true)) {} }
    @Test fun s08d_deleted() = shot { MyPlacesScreen(MyPlacesUiState(true, 11, 3, 3, rows.drop(1), pins.drop(1), here)) {}; ToastHost(flowOf(UserMessage.Deleted("서울갈비") {}), 60.dp, 140.dp) }

    private val cands = listOf(cand("할머니국밥", "한식 > 국밥", 40.0), cand("부산돼지국밥", "한식 > 국밥", 85.0, Side.LEFT), cand("김밥천국", "분식", 120.0))
    @Test fun s09_resolve() = shot {
        ResolveScreen(ResolveUiState(true, now - day, "테헤란로", "강남역 방향", "방금 지난 국밥집 저장해줘", false, GeoPoint(37.5003, 127.0303), cands, 0, title = "할머니국밥?")) {}
    }
    @Test fun s09b_search() = shot { DirectSearchScreen(DirectSearchUiState("국밥", cands, 0, resultsFor = "국밥")) {} }

    @Test fun s10_share() = shot {
        ShareScreen(
            ShareUiState(
                true,
                listOf(ShareRowModel("1", "서울갈비", "고깃집", true), ShareRowModel("2", "카페 온도", "카페", true, 1), ShareRowModel("3", "할머니국밥", "국밥", false)),
                "내 맛집 2곳", false, "https://easycd.github.io/nextplace/s#AbCdEf",
            ),
        ) {}
    }
    @Test fun s11_settings() = shot { SettingsScreen(SettingsUiState(AppSettings(), 12)) {} }
    @Test fun s11b_delete_all() = shot { SettingsScreen(SettingsUiState(AppSettings(), 12, confirmDeleteAll = true)) {} }
    @Test fun s11c_settings_car_volume() = shot { SettingsScreen(SettingsUiState(AppSettings(), 12, volumeAdjustable = false)) {} }
    @Test fun s12_drive_lock() = shot(restricted = true) { DriveLockScreen("내 맛집") {} }

    @Test fun s15_visit_note() = shot { HomeScreen(home) {}; VisitNoteCard(VisitNoteUiState(VisitNoteState.Asking("서울갈비", 1, now, false, ""))) {} }
    @Test fun s15b_note_saved() = shot { HomeScreen(home.copy(latest = home.latest!!.copy(chip = LatestChip.VISITED, visits = 1))) {}; ToastHost(flowOf(UserMessage.NoteSaved("서울갈비", "여기 괜찮았어")), 60.dp, 140.dp) }

    private val shared = listOf(
        SharedPlace("서울갈비", "고깃집", "테헤란로 123", 37.5, 127.03, "줄 서 있던", "kakao", "1"),
        SharedPlace("카페 온도", "카페", null, 37.51, 127.02, null, "kakao", "2"),
        SharedPlace("할머니국밥", "국밥", null, 37.49, 127.04, null, "kakao", "3"),
    )
    @Test fun s16_import() = shot {
        ImportScreen(ImportUiState(true, title = "재원의 강릉 맛집", createdAt = now - day, from = "재원", rows = shared.mapIndexed { i, p -> ImportRow(p, i == 1, i != 1) })) {}
    }

    @Test fun s03_confirm_light() = shot(dark = false) { HomeScreen(home) {}; ConfirmCard(ConfirmUiState(true, "k", false, "서울갈비", "고깃집", 120, false, Side.RIGHT, "테헤란로 123", now)) {} }
    @Test fun s06_recommend_light() = shot(dark = false) { RecommendScreen(RecommendUiState(RecommendState.Showing(null, recCards, 0, readingIndex = 1))) {} }
    @Test fun s07_myplaces_light() = shot(dark = false) { MyPlacesScreen(MyPlacesUiState(true, 12, 3, 4, rows, pins, here)) {} }
    @Test fun s08_detail_light() = shot(dark = false) { PlaceDetailScreen(detail) {} }
    @Test fun s11_settings_light() = shot(dark = false) { SettingsScreen(SettingsUiState(AppSettings(), 12)) {} }
    @Test fun s15_visit_note_light() = shot(dark = false) { HomeScreen(home) {}; VisitNoteCard(VisitNoteUiState(VisitNoteState.Asking("서울갈비", 1, now, false, ""))) {} }
    @Test fun s16_import_light() = shot(dark = false) {
        ImportScreen(ImportUiState(true, title = "재원의 강릉 맛집", createdAt = now - day, from = "재원", rows = shared.mapIndexed { i, p -> ImportRow(p, i == 1, i != 1) })) {}
    }
}
