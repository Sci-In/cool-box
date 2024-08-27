package io.emqx.mqtt

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.cache.CacheFactory
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import tv.danmaku.ijk.media.exo2.ExoPlayerCacheManager


class MainActivity : AppCompatActivity(), MqttCallback {
    private var mClient: MqttAndroidClient? = null
    private var player: StandardGSYVideoPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        try {
            findViewById<TextView>(R.id.tv_version_name).text =
                "版本号:${getVersionName(MyApplication.appContext)}"
            player = findViewById(R.id.video_player)

            //EXOPlayer内核，支持格式更多
            PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
            //系统内核模式
            //PlayerFactory.setPlayManager(SystemPlayerManager::class.java)
            //ijk内核，默认模式
            //PlayerFactory.setPlayManager(IjkPlayerManager::class.java)
            //aliplay 内核，默认模式
            //PlayerFactory.setPlayManager(AliPlayerManager::class.java)

            //exo缓存模式，支持m3u8，只支持exo
            CacheFactory.setCacheManager(ExoPlayerCacheManager::class.java)
            //代理缓存模式，支持所有模式，不支持m3u8等，默认
            //CacheFactory.setCacheManager(ProxyCacheManager::class.java)


            //切换绘制模式
            //GSYVideoType.setRenderType(GSYVideoType.SUFRACE);
            //GSYVideoType.setRenderType(GSYVideoType.GLSURFACE);
            GSYVideoType.setRenderType(GSYVideoType.TEXTURE)

            //默认显示比例
            //GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT);

            //16:9
            GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_16_9);

            //全屏裁减显示，为了显示正常 CoverImageView 建议使用FrameLayout作为父布局
            //GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL);

            //全屏拉伸显示，使用这个属性时，surface_container建议使用FrameLayout
            //GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);
            //player.isLooping = true
            //4:3
            //GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_4_3);
            player?.clearCurrentCache()
            player?.setGSYStateUiListener { state ->
                when (state) {
                    0 -> {}
                    1 -> {}
                    2 -> {}
                    6 -> {
                        setAdUrlAndPlay(player!!)
                    }
                }
            }
            setADListStrMovieSP("https://www.w3school.com.cn/example/html5/mov_bbb.mp4,https://media.kg-portal.ru/movies/m/moana2/trailers/moana2_teaser_1280.mp4")
            setAdUrlAndPlay(player!!)
            connect(connection, mqttListener)
//        } catch (e: Exception) {
//            Toast.makeText(MyApplication.appContext, "错误！！！${e.message}", Toast.LENGTH_LONG).show()
//        }
    }

    private fun connect(connection: Connection, listener: IMqttActionListener?) {
        mClient = connection.getMqttAndroidClient(this)
        try {
            mClient!!.setCallback(this@MainActivity)
            mClient!!.connect(connection.mqttConnectOptions, null, listener)
        } catch (e: MqttException) {
            e.printStackTrace()
            Toast.makeText(MyApplication.appContext, "Failed to connect", Toast.LENGTH_SHORT).show()
        }
    }

    private fun disconnect() {
        if (notConnected(true)) {
            return
        }
        Toast.makeText(MyApplication.appContext, "断开连接！！！", Toast.LENGTH_SHORT).show()
        try {
            mClient!!.disconnect()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private val subscriptionMovie: Subscription
        get() {
            //cmd/movie/###
            val topic = "c/m/${getAndroidId()}"
            val qos = 1
            return Subscription(topic, qos)
        }

    private val subscriptionImage: Subscription
        get() {
            //cmd/image/###
            val topic = "c/i/${getAndroidId()}"
            val qos = 1
            return Subscription(topic, qos)
        }

    private val subscriptionListener = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken) {
            Toast.makeText(
                MyApplication.appContext,
                "订阅成功！！！",
                Toast.LENGTH_LONG
            ).show()
            publish(publishOn, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Toast.makeText(MyApplication.appContext, "消息发布成功！！！", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Toast.makeText(MyApplication.appContext, "消息发布失败！！！", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }

        override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
            Toast.makeText(
                MyApplication.appContext,
                "订阅失败！！！",
                Toast.LENGTH_LONG
            ).show()
            subscribe(subscriptionMovie, this)
        }
    }

    fun subscribe(subscription: Subscription, listener: IMqttActionListener?) {
        if (notConnected(true)) {
            return
        }
        try {
            mClient?.subscribe(subscription.topic, subscription.qos, null, listener)
        } catch (e: MqttException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to subscribe", Toast.LENGTH_SHORT).show()
        }
    }

    private val publishOn: Publish
        get() {
            //sys/on/###
            val topic = "s/o/${getAndroidId()}"
            val message = ""
            val qos = 1
            val retained = false
            return Publish(topic, message, qos, retained)
        }

    fun publish(publish: Publish, callback: IMqttActionListener?) {
        if (notConnected(true)) {
            return
        }
        try {
            mClient!!.publish(
                publish.topic,
                publish.payload.toByteArray(),
                publish.qos,
                publish.isRetained,
                null,
                callback
            )
        } catch (e: MqttException) {
            e.printStackTrace()
            Toast.makeText(this, "消息发送失败！！！", Toast.LENGTH_SHORT).show()
        }
    }

    private fun notConnected(showNotify: Boolean): Boolean {
        if (mClient == null || !mClient!!.isConnected) {
            if (showNotify) {
                Toast.makeText(this, "Client is not connected", Toast.LENGTH_SHORT).show()
            }
            return true
        }
        return false
    }


    private val connection = Connection(
        MyApplication.appContext,
        "s2c982e0.ala.cn-hangzhou.emqxsl.cn",
        8883,
        getAndroidId(),
        "zhangtiezhu",
        "zhangtiezhu001",
        true
    )

    private val mqttListener = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Toast.makeText(
                MyApplication.appContext,
                "连接成功!!!",
                Toast.LENGTH_LONG
            ).show()
            subscribe(subscriptionMovie, subscriptionListener)
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            Toast.makeText(
                MyApplication.appContext,
                "连接失败!!!" + exception?.cause?.message,
                Toast.LENGTH_LONG
            ).show()
            Log.i("TAG666", "onFailure: 连接失败!!" + exception?.stackTraceToString())
            //connect(connection, this)
        }
    }

    override fun connectionLost(cause: Throwable?) {
        Log.i("TAG666", "connectionLost: " + cause?.message)
        if (notConnected(false)) {
            connect(connection, mqttListener)
        }
    }

    @Throws(Exception::class)
    override fun messageArrived(topic: String, message: MqttMessage) {
        Toast.makeText(
            MyApplication.appContext,
            "topic:${topic},msg:${String(message.payload)}",
            Toast.LENGTH_SHORT
        ).show()
        try {
            //cmd/movie/###
            if (topic.startsWith("c/m/")) {
                setADListStrMovieSP(String(message.payload))
                //cmd/image/###
            } else if (topic.startsWith("c/i/")) {
                setADListStrImgSP(String(message.payload))
            }
            setAdUrlAndPlay(player!!)
        } catch (e: Exception) {
            Toast.makeText(
                MyApplication.appContext,
                "消息解析失败！！！",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken) {}

    /**
     * 1、从 SP 中拿到 String
     * 2、用逗号,拆分开，得到 List<String>
     * 3、取出第 adIndex 位 String 做为当前 ad 的url
     * 4、adIndex++
     *
     * @return 当前ad播放需要的url
     */
    private fun setAdUrlAndPlay(player: StandardGSYVideoPlayer) {
        adListStrMovie = getADListStrMovieSP()
        adList = adListStrMovie.split(",")
        adUrlStrCurrent = adList[adIndex % adList.size].trim()
        adIndex++
        player.setUp(
            adUrlStrCurrent,
            true,
            ""
        )
        player.startPlayLogic()
    }

    override fun onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        GSYVideoManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        GSYVideoManager.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }

    private fun setADListStrMovieSP(adListMovieStr: String) {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("ad_list_movie_str", adListMovieStr)
            apply()
        }
    }

    private fun getADListStrMovieSP(): String {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return STR_EMPTY
        val defaultValue = STR_EMPTY
        val adListJsonStr = sharedPref.getString("ad_list_movie_str", defaultValue)
        return adListJsonStr ?: STR_EMPTY
    }

    private fun setADListStrImgSP(adListImgStr: String) {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("ad_list_img_str", adListImgStr)
            apply()
        }
    }

    private fun getADListStrImgSP(): String {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return STR_EMPTY
        val defaultValue = STR_EMPTY
        val adListJsonStr = sharedPref.getString("ad_list_img_str", defaultValue)
        return adListJsonStr ?: STR_EMPTY
    }

    companion object {

        @JvmStatic
        val STR_EMPTY = ""

        @JvmStatic
        var adIndex = 0

        @JvmStatic
        var adListStrMovie = STR_EMPTY

        @JvmStatic
        var adList: List<String> = listOf()

        @JvmStatic
        var adUrlStrCurrent: String = STR_EMPTY
        fun getAndroidId(): String {
            return Settings.Secure.getString(
                MyApplication.appContext.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        }

        /**
         * 获取当前apk的版本号
         *
         * @param mContext
         * @return
         */
        fun getVersionCode(mContext: Context): Int {
            var versionCode = 0
            try {
                //获取软件版本号，对应AndroidManifest.xml下android:versionCode
                versionCode = mContext.packageManager.getPackageInfo(
                    mContext.packageName, 0
                ).versionCode
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return versionCode
        }

        /**
         * 获取当前apk的版本名
         *
         * @param context 上下文
         * @return
         */
        fun getVersionName(context: Context): String {
            var versionName = ""
            try {
                //获取软件版本号，对应AndroidManifest.xml下android:versionName
                versionName = context.packageManager.getPackageInfo(
                    context.packageName, 0
                ).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return versionName
        }
    }
}