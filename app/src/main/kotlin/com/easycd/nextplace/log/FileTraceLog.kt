package com.easycd.nextplace.log

import android.content.Context
import android.util.Log
import com.easycd.nextplace.core.domain.pipeline.TraceLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 단계별 시간·오류 로그 (기술서 9장 측정, 10장 '로그 파일 1개(최근 1MB)').
 * 쓰기는 한 줄씩 채널에 넣고 IO 스레드 하나가 파일에 붙인다. 1MB 를 넘으면 앞 파일을 .old 로 돌린다.
 * 발화 원문·좌표는 남기지 않는다 (기획서 8장 개인정보).
 */
@Singleton
class FileTraceLog @Inject constructor(@ApplicationContext context: Context) : TraceLog {
    private val dir = File(context.filesDir, "logs")
    private val file = File(dir, "trace.log")
    private val lines = Channel<String>(CAPACITY, BufferOverflow.DROP_OLDEST)
    private val time = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            dir.mkdirs()
            for (line in lines) {
                runCatching {
                    if (file.length() > MAX_BYTES) file.renameTo(File(dir, "trace.old.log"))
                    file.appendText(line + "\n")
                }
            }
        }
    }

    override fun stage(pipeline: String, stage: String, elapsedMs: Long, outcome: String) =
        write("STAGE", "$pipeline/$stage ${elapsedMs}ms $outcome")

    override fun event(tag: String, message: String) = write("EVENT", "$tag $message")

    override fun error(tag: String, error: Throwable) {
        Log.w(LOG_TAG, tag, error)
        write("ERROR", "$tag ${error::class.java.simpleName}: ${error.message}")
    }

    private fun write(kind: String, text: String) {
        Log.d(LOG_TAG, "$kind $text")
        val stamp = synchronized(time) { time.format(Date()) }
        lines.trySend("$stamp $kind $text")
    }

    private companion object {
        const val LOG_TAG = "NextPlace"
        const val CAPACITY = 256
        const val MAX_BYTES = 1_000_000L
    }
}
