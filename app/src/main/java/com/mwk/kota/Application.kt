package com.mwk.kota

import android.app.Application
import com.mwk.data.reminder.ReminderDbMonitor
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Application : Application() {

    @Inject
    lateinit var reminderDbMonitor: ReminderDbMonitor

    override fun onCreate() {
        super.onCreate()
        reminderDbMonitor.start()
    }

}
