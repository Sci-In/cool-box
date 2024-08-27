package io.emqx.mqtt

import android.app.Application
import android.content.Context

/**
 * @author bzw [workbzw@outlook.com]
 * @date   2024/8/13 10:09
 * @desc   []
 */
class MyApplication : Application() {
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        CollectCrashUtils.initColleteCrash()
    }
}