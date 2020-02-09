package org.monicahq.phoebe.api

import android.annotation.SuppressLint
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.monicahq.phoebe.BuildConfig
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

class RestApi(val Url: String) {

    private var authServiceInterceptor: AuthServiceInterceptor = AuthServiceInterceptor()

    private fun getRetrofit(Url: String): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val client = getUnsafeOkHttpClient()
            .addInterceptor(authServiceInterceptor)
            .build()

        val builder = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .baseUrl(Url)

        return builder.build()
    }

    fun setToken(token: String) = apply {
        authServiceInterceptor.setSessionToken(token)
    }

    companion object {
        fun getApi(url: String?): RestApi {
            var url2 = url
            if (url2 == null) {
                url2 = BuildConfig.APP_URL
            }
            if (! url2.endsWith('/')) {
                url2 += "/"
            }
            return RestApi(url2)
        }
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        if (BuildConfig.DEBUG) {
            return try { // Create a trust manager that does not validate certificate chains
                val trustAllCerts: Array<TrustManager> = arrayOf(
                    object : X509TrustManager {
                        @SuppressLint("TrustAllX509TrustManager")
                        @Throws(CertificateException::class)
                        override fun checkClientTrusted(
                            chain: Array<X509Certificate?>?,
                            authType: String?
                        ) {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        @Throws(CertificateException::class)
                        override fun checkServerTrusted(
                            chain: Array<X509Certificate?>?,
                            authType: String?
                        ) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> {
                            return arrayOf()
                        }
                    }
                )

                // Install the all-trusting trust manager
                val sslContext: SSLContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())
                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

                val builder = OkHttpClient.Builder()
                builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                builder.hostnameVerifier(HostnameVerifier { _, _ -> true })
                builder
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        } else {
            return OkHttpClient.Builder()
        }
    }

    private class AuthServiceInterceptor : Interceptor {

        private var sessionToken: String? = null

        fun setSessionToken(sessionToken: String?) {
            this.sessionToken = sessionToken
        }

        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            if (request.header("No-Authentication") == null && sessionToken != null) {
                request = request.newBuilder()
                    .header("Authorization", "Bearer $sessionToken")
                    .build()
            }
            return chain.proceed(request)
        }
    }

    private var api: Retrofit

    init {
        api = getRetrofit(Url)
    }

    val oauthApi: OAuthApi
        get() {
            return api.create(OAuthApi::class.java)
        }

    val meApi: MeApi
        get() {
            return api.create(MeApi::class.java)
        }

    val contacts: Contacts
        get() {
            return api.create(Contacts::class.java)
        }
}
