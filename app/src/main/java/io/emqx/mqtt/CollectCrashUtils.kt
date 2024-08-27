package io.emqx.mqtt

import java.io.File


/**
 * @author bzw [workbzw@outlook.com]
 * @date   2024/8/26 08:50
 * @desc   []
 */
class CollectCrashUtils {
    companion object {

        fun initColleteCrash() {
            //初始化Handler,收集java层崩溃
            val handler = MyJavaCrashHandler()
            Thread.setDefaultUncaughtExceptionHandler(handler)

            //收集native层崩溃
//            val file = File("sdcard/Crashlog")
//            if (!file.exists()) {
//                file.mkdirs()
//            }
//            NativeBreakpad.init(file.absolutePath)
        }
    }
}
