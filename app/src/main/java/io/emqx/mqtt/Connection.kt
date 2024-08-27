package io.emqx.mqtt

import android.content.Context
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import java.io.InputStream
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class Connection(
    private val context: Context,
    private var host: String,
    private var port: Int,
    private var clientId: String,
    private var username: String,
    private var password: String,
    private val tls: Boolean
) {
    fun getMqttAndroidClient(context: Context?): MqttAndroidClient {
        val uri: String = if (tls) {
            "ssl://$host:$port"
        } else {
            "tcp://$host:$port"
        }
        return MqttAndroidClient(context, uri, clientId)
    }

    val mqttConnectOptions: MqttConnectOptions
        get() {
            val options = MqttConnectOptions()
            options.isCleanSession = false
            if (tls) {
                try {
                    val caCrtFileI = context.resources.openRawResource(R.raw.cacert)
                    options.socketFactory = getSingleSocketFactory(caCrtFileI)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (username.isNotEmpty()) {
                options.userName = username
            }
            if (password.isNotEmpty()) {
                options.password = password.toCharArray()
            }
            return options
        }

    companion object {
        @Throws(java.lang.Exception::class)
        fun getSingleSocketFactory(caCrtFileInputStream: InputStream?): SSLSocketFactory {
            val trustAllCerts: Array<TrustManager> =
                arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {

                    }

                    override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {

                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate?> {
                        val arrayOfNulls = arrayOfNulls<X509Certificate?>(0)
                        return arrayOfNulls
                    }

                })
            val sslContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(null, trustAllCerts, null)
            return sslContext.socketFactory
        }
    }
}