package com.easycd.nextplace.registry

import com.easycd.nextplace.core.ui.nav.Feature
import com.easycd.nextplace.feature.myplaces.MyPlacesFeature
import com.easycd.nextplace.feature.recommend.RecommendFeature
import com.easycd.nextplace.feature.settings.SettingsFeature
import com.easycd.nextplace.feature.share.ShareFeature
import com.easycd.nextplace.feature.voicesave.VoiceSaveFeature

/**
 * 기능 등록표 (기술서 6장). 새 기능은 모듈 하나 + 여기 한 줄. 리마인드·방문 메모는 화면 대신
 * 앱 창 위 카드라 [AppNavGraph] 의 겹침 층에 올린다.
 */
object FeatureRegistry {
    val features: List<Feature> = listOf(
        VoiceSaveFeature,
        RecommendFeature,
        MyPlacesFeature,
        ShareFeature,
        SettingsFeature,
    )
}
