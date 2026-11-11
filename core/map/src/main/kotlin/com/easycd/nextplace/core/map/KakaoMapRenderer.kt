package com.easycd.nextplace.core.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.easycd.nextplace.core.domain.model.GeoPoint
import com.easycd.nextplace.core.ui.component.MapPin
import com.easycd.nextplace.core.ui.component.MapRenderer
import com.easycd.nextplace.core.ui.component.PinCanvas
import com.easycd.nextplace.core.ui.component.PinTone
import com.easycd.nextplace.core.ui.theme.Np
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles

/**
 * 카카오맵 위에 핀을 그린다. 핀 모양·색은 [PinCanvas]와 같다 (저장 주황 · 방문 초록 · 미확인 노랑 + 가운데 구멍).
 * - 화면 맞춤은 핀 목록(가게·위치)이 바뀔 때만 한다. 차가 움직여 내 위치만 바뀌면 점만 옮기고 지도는 그대로 둔다.
 * - 지도 엔진이 실패하면(인증 실패 · 엔진 없음) 그 자리에서 배경 없는 핀 지도로 바꾼다.
 */
internal object KakaoMapRenderer : MapRenderer {
    @Composable
    override fun Map(pins: List<MapPin>, me: GeoPoint?, modifier: Modifier, onPin: ((String) -> Unit)?) {
        var failed by remember { mutableStateOf(false) }
        if (failed) return PinCanvas(pins, me, modifier, onPin)

        val context = LocalContext.current
        val view = remember {
            // 엔진(.so)을 못 불러오면 여기서 Error 가 난다 → 핀 지도로
            runCatching { MapView(context) }.onFailure { Log.w(KakaoMaps.TAG, "MapView failed", it) }.getOrNull()
        }
        if (view == null) return PinCanvas(pins, me, modifier, onPin)

        var map by remember { mutableStateOf<KakaoMap?>(null) }
        val onPinNow by rememberUpdatedState(onPin)
        val start = remember { (pins.firstOrNull()?.point ?: me ?: SEOUL_CITY_HALL).let { LatLng.from(it.lat, it.lon) } }
        val lifecycle = LocalLifecycleOwner.current.lifecycle

        DisposableEffect(view, lifecycle) {
            view.start(
                object : MapLifeCycleCallback() {
                    override fun onMapDestroy() {
                        map = null
                    }

                    override fun onMapError(error: Exception) {
                        Log.w(KakaoMaps.TAG, "map error", error)
                        failed = true
                    }
                },
                object : KakaoMapReadyCallback() {
                    override fun getPosition(): LatLng = start
                    override fun getZoomLevel(): Int = START_ZOOM
                    override fun onMapReady(k: KakaoMap) {
                        k.setOnLabelClickListener { _, _, label ->
                            val id = label.tag as? String
                            if (id != null && id != ME_ID) onPinNow?.invoke(id)
                            true
                        }
                        map = k
                    }
                },
            )
            // 화면이 이미 RESUMED 면 addObserver 가 ON_RESUME 까지 바로 보내 준다
            val observer = LifecycleEventObserver { _, e ->
                when (e) {
                    Lifecycle.Event.ON_RESUME -> view.resume()
                    Lifecycle.Event.ON_PAUSE -> view.pause()
                    else -> Unit
                }
            }
            lifecycle.addObserver(observer)
            onDispose {
                lifecycle.removeObserver(observer)
                runCatching { view.finish() }
                map = null
            }
        }

        AndroidView(factory = { view }, modifier = modifier.clip(RoundedCornerShape(36.dp)))

        val c = Np.colors
        val density = LocalDensity.current.density
        val icons = remember(c, density) { PinIcons(c.brand.toArgb(), c.green.toArgb(), c.amber.toArgb(), c.background.toArgb(), density) }
        var fitted by remember { mutableStateOf<List<Pair<String, GeoPoint>>?>(null) }
        val padding = with(LocalDensity.current) { FIT_PADDING.roundToPx() }

        LaunchedEffect(map, pins, me, icons) {
            val k = map ?: return@LaunchedEffect
            val layer = k.labelManager?.layer ?: return@LaunchedEffect
            layer.removeAll()
            me?.let {
                layer.addLabel(LabelOptions.from(ME_ID, LatLng.from(it.lat, it.lon)).setStyles(icons.me).setTag(ME_ID).setClickable(false).setRank(0))
            }
            pins.forEach { p ->
                layer.addLabel(
                    LabelOptions.from(p.id, LatLng.from(p.point.lat, p.point.lon))
                        .setStyles(icons.of(p.tone, p.selected))
                        .setTag(p.id)
                        .setClickable(onPinNow != null)
                        .setRank(if (p.selected) 2 else 1),
                )
            }
            // 화면 맞춤: 핀이 바뀌었을 때만. 핀이 없으면 처음 한 번만 내 위치로.
            val key = pins.map { it.id to it.point }
            if (key == fitted) return@LaunchedEffect
            if (key.isEmpty() && fitted != null) return@LaunchedEffect
            val points = (pins.map { it.point } + listOfNotNull(me)).map { LatLng.from(it.lat, it.lon) }
            when {
                points.isEmpty() -> return@LaunchedEffect
                points.size == 1 -> k.moveCamera(CameraUpdateFactory.newCenterPosition(points[0], SINGLE_ZOOM))
                else -> k.moveCamera(CameraUpdateFactory.fitMapPoints(points.toTypedArray(), padding))
            }
            fitted = key
        }
    }

    private val SEOUL_CITY_HALL = GeoPoint(37.5663, 126.9779)
    private const val ME_ID = "__me"
    private const val START_ZOOM = 15
    private const val SINGLE_ZOOM = 16
    private val FIT_PADDING = 72.dp
}

/** 핀 비트맵 묶음. [PinCanvas]의 drawPin 과 같은 모양을 Android Canvas 로 그린다. */
private class PinIcons(private val brand: Int, private val green: Int, private val amber: Int, private val hole: Int, private val density: Float) {
    private val cache = HashMap<Pair<PinTone, Boolean>, LabelStyles>()

    val me: LabelStyles = LabelStyles.from(LabelStyle.from(meBitmap()).setAnchorPoint(0.5f, 0.5f).setApplyDpScale(false))

    fun of(tone: PinTone, selected: Boolean): LabelStyles = cache.getOrPut(tone to selected) {
        val color = when (tone) {
            PinTone.SAVED, PinTone.CANDIDATE -> brand
            PinTone.VISITED -> green
            PinTone.UNCONFIRMED, PinTone.ESTIMATED -> amber
        }
        val bmp = pinBitmap(color, if (selected) 1.2f else 1f, tone == PinTone.UNCONFIRMED)
        LabelStyles.from(LabelStyle.from(bmp).setAnchorPoint(0.5f, 1f).setApplyDpScale(false))
    }

    private fun pinBitmap(color: Int, scale: Float, question: Boolean): Bitmap {
        val r = 18f * density * scale
        val stroke = 2f * density
        val w = (r * 2.2f + stroke * 2).toInt()
        val h = (r * 3.1f + stroke * 2).toInt()
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val cv = Canvas(bmp)
        val x = w / 2f
        val y = h - stroke
        val path = Path().apply {
            moveTo(x, y)
            cubicTo(x - r * 0.6f, y - r * 1.2f, x - r * 1.1f, y - r * 1.6f, x - r * 1.1f, y - r * 2.2f)
            cubicTo(x - r * 1.1f, y - r * 3.4f, x + r * 1.1f, y - r * 3.4f, x + r * 1.1f, y - r * 2.2f)
            cubicTo(x + r * 1.1f, y - r * 1.6f, x + r * 0.6f, y - r * 1.2f, x, y)
            close()
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = color
        cv.drawPath(path, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = stroke
        paint.color = 0x99FFFFFF.toInt()
        cv.drawPath(path, paint)
        paint.style = Paint.Style.FILL
        paint.color = if (question) hole else 0xFFFFFFFF.toInt()
        cv.drawCircle(x, y - r * 2.2f, r * 0.45f, paint)
        if (question) {
            paint.color = color
            cv.drawCircle(x, y - r * 2.2f, r * 0.18f, paint)
        }
        return bmp
    }

    /** 내 위치: 옅은 주황 원 + 흰 테 + 주황 점 (핀 지도와 같음). */
    private fun meBitmap(): Bitmap {
        val outer = 28f * density
        val size = (outer * 2).toInt()
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val cv = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cx = size / 2f
        paint.color = (brand and 0x00FFFFFF) or (0x2E shl 24)
        cv.drawCircle(cx, cx, outer, paint)
        paint.color = 0xFFFFFFFF.toInt()
        cv.drawCircle(cx, cx, 12f * density, paint)
        paint.color = brand
        cv.drawCircle(cx, cx, 8f * density, paint)
        return bmp
    }
}
