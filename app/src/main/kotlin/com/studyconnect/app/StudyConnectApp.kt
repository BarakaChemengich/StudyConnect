package com.studyconnect.app

import android.app.Application
import com.studyconnect.app.work.NotificationHelper

class StudyConnectApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannel(this)
    }
}
