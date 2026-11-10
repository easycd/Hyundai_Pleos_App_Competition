package com.easycd.nextplace.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.Density
import com.android.resources.ScreenOrientation
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.ui.component.MapPin
import com.easycd.nextplace.core.ui.component.PinTone
import com.easycd.nextplace.core.ui.drive.LocalCompact
import com.easycd.nextplace.core.ui.drive.LocalDriveRestricted
import com.easycd.nextplace.core.ui.drive.LocalPortrait
import com.easycd.nextplace.core.ui.theme.LocalReduceMotion
import com.easycd.nextplace.core.ui.theme.NextPlaceTheme
import com.easycd.nextplace.core.ui.theme.Np
import com.easycd.nextplace.feature.myplaces.ui.common.PlaceRowModel
import com.easycd.nextplace.feature.myplaces.ui.detail.PlaceDetailScreen
import com.easycd.nextplace.feature.myplaces.ui.detail.PlaceDetailUiState
import com.easycd.nextplace.feature.myplaces.ui.list.MyPlacesScreen
import com.easycd.nextplace.feature.myplaces.ui.list.MyPlacesUiState
import com.easycd.nextplace.feature.recommend.session.RecCard
import com.easycd.nextplace.feature.recommend.session.RecommendState
import com.easycd.nextplace.feature.recommend.ui.RecommendScreen
import com.easycd.nextplace.feature.recommend.ui.RecommendUiState
import com.easycd.nextplace.feature.voicesave.ui.home.HomeScreen
import com.easycd.nextplace.feature.voicesave.ui.home.HomeUiState
import com.easycd.nextplace.feature.voicesave.ui.home.LatestCard
import com.easycd.nextplace.feature.voicesave.ui.home.LatestChip
import com.easycd.nextplace.feature.voicesave.ui.home.LocationLabel
import org.junit.Rule
import org.junit.Test

/**
 * 좁은 가로 창 (에뮬레이터 MVR_Fold 2076×2152px · 420dpi ≈ 791×820dp). 실제 점검에서 글자가 잘렸던 화면들.
 * 가게 이름은 점검 때 나온 긴 이름을 쓴다.
 */
class CompactScreensTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig(
            screenWidth = 2076, screenHeight = 2152, xdpi = 420, ydpi = 420,
            density = Density.create(420), orientation = ScreenOrientation.PORTRAIT,
        ),
        maxPercentDifference = 0.1,
    )

    private val now = System.currentTimeMillis()
    private val day = 86_400_000L
    private val here = GeoPoint(37.5000, 127.0300)

    private fun shot(content: @Composable () -> Unit) {
        paparazzi.snapshot {
            CompositionLocalProvider(
                LocalDriveRestricted provides false, LocalPortrait provides false, LocalCompact provides true, LocalReduceMotion provides true,
            ) {
                NextPlaceTheme(dark = true) {
                    Box(Modifier.fillMaxSize().background(Np.colors.background)) { content() }
                }
            }
        }
    }

    private val home = HomeUiState(
        locationLabel = LocationLabel("테헤란로", "강남역 방향"),
        total = 12, unconfirmed = 3, nearbyCount = 0,
        latest = LatestCard("p1", "고봉삼계탕 강남직영점", "삼계탕 · 202m", false, now - 60_000, LatestChip.SAVED, 0, false),
        simulated = true,
    )

    @Test fun c01_home() = shot { HomeScreen(home) {} }
    @Test fun c01b_home_empty() = shot { HomeScreen(HomeUiState(locationLabel = LocationLabel("테헤란로", "강남역 방향"), simulated = true)) {} }

    @Test fun c06_recommend() = shot {
        val cards = listOf(
            RecCard("1", 1, "고봉삼계탕 강남직영점", "삼계탕", 200.0, null, now, 0),
            RecCard("2", 2, "모퉁이 커피", "카페", 900.0, "괜찮았어", now - 20 * day, 1),
            RecCard("3", 3, "할머니국밥", "국밥", 3400.0, null, now - 13 * day, 0),
        )
        RecommendScreen(RecommendUiState(RecommendState.Showing(null, cards, 0, readingIndex = 1))) {}
    }

    @Test fun c07_myplaces() = shot {
        val rows = listOf(
            PlaceRowModel("1", "고봉삼계탕 강남직영점", "삼계탕", 200.0, now - 60_000, 0, false, false),
            PlaceRowModel("2", "할머니국밥?", "국밥", 2400.0, now - day, 0, true, false),
            PlaceRowModel("3", "모퉁이 커피", "카페", 3100.0, now - 10 * day, 2, false, false),
        )
        val pins = listOf(MapPin("1", GeoPoint(37.5050, 127.0350), PinTone.SAVED), MapPin("2", GeoPoint(37.4950, 127.0250), PinTone.UNCONFIRMED))
        MyPlacesScreen(MyPlacesUiState(true, 3, 1, 0, rows, pins, here)) {}
    }

    @Test fun c08_detail() = shot {
        PlaceDetailScreen(
            PlaceDetailUiState(
                true, "1", "고봉삼계탕 강남직영점", "삼계탕", 200.0, "서울 강남구 테헤란로 123", now - 60_000, 0, null,
                emptyList(), "방금 지난 고깃집 저장해줘", GeoPoint(37.505, 127.035),
            ),
        ) {}
    }
}
