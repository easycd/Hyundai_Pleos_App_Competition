package com.easycd.nextplace

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.easycd.nextplace.start.AppStartup
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/** Hilt 루트 (기술서 2장). 시작 작업은 [AppStartup] 하나에 모은다. WorkManager 는 Hilt 작업 공장을 쓴다. */
@HiltAndroidApp
class NextPlaceApp : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var startup: AppStartup

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        startup.run()
    }
}
