package com.easycd.nextplace.core.domain.contract

import com.easycd.nextplace.core.domain.model.UserMessage
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/** 화면 이동 요청 통로. app 이 모아서 NavController 로 옮긴다. */
@Singleton
class Navigator @Inject constructor() {
    private val _commands = MutableSharedFlow<Destination>(extraBufferCapacity = 8, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val commands: SharedFlow<Destination> = _commands.asSharedFlow()
    fun go(destination: Destination) {
        _commands.tryEmit(destination)
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
