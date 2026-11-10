package com.easycd.nextplace.core.domain.contract

import com.easycd.nextplace.core.domain.model.UserMessage
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 화면 이동 요청 통로. app 이 모아서 NavController 로 옮긴다.
 * 받는 쪽(NavHost)이 아직 없을 때 온 요청도 버리지 않고 쌓아 두었다가 한 번만 넘긴다
 * (앱이 꺼진 상태에서 공유 링크로 열면 링크 처리가 시작 화면보다 먼저 온다). 넘긴 요청은 다시 재생하지 않는다.
 */
@Singleton
class Navigator @Inject constructor() {
    private val _commands = Channel<Destination>(capacity = PENDING_MAX, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val commands: Flow<Destination> = _commands.receiveAsFlow()
    fun go(destination: Destination) {
        _commands.trySend(destination)
    }

    private companion object {
        const val PENDING_MAX = 8
    }
}

/** 토스트 통로. 한 번에 1개 (새 것이 오면 앞의 것을 바꾼다). */
@Singleton
class Messages @Inject constructor() {
    private val _messages = MutableSharedFlow<UserMessage>(extraBufferCapacity = 4, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val messages: SharedFlow<UserMessage> = _messages.asSharedFlow()
    fun show(message: UserMessage) {
        _messages.tryEmit(message)
    }
}

/** 앱 창이 보이는가 (app 이 ProcessLifecycleOwner 로 갱신). 리마인드·방문 메모 카드를 앱 창에 띄울지 고를 때 쓴다. */
@Singleton
class AppForeground @Inject constructor() {
    private val _visible = kotlinx.coroutines.flow.MutableStateFlow(false)
    val visible: kotlinx.coroutines.flow.StateFlow<Boolean> = _visible
    fun set(visible: Boolean) {
        _visible.value = visible
    }
}

/** S-08 "공유에 담기"로 고른 가게 (S-10 에서 미리 체크). 메모리에만 둔다. */
@Singleton
class ShareCart @Inject constructor() {
    private val _ids = kotlinx.coroutines.flow.MutableStateFlow<Set<String>>(emptySet())
    val ids: kotlinx.coroutines.flow.StateFlow<Set<String>> = _ids
    fun add(id: String) {
        _ids.value = _ids.value + id
    }
    fun clear() {
        _ids.value = emptySet()
    }
}

/** 방금 가져온 가게 (S-16b: 내 맛집 목록에서 새 줄을 주황 톤으로 잠깐 강조). */
@Singleton
class RecentImport @Inject constructor() {
    private val _ids = kotlinx.coroutines.flow.MutableStateFlow<Set<String>>(emptySet())
    val ids: kotlinx.coroutines.flow.StateFlow<Set<String>> = _ids
    fun set(ids: Set<String>) {
        _ids.value = ids
    }
}
