package com.ipl.estg.mcif.eltonpastilha.api


import android.content.Context
import com.ipl.estg.mcif.eltonpastilha.db.Repository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

//To get cookies or information from repository to put on the cookies
class SendSavedCookiesInterceptor(private val context: Context) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val instance : Repository = Repository.getInstance(context)
        val builder = chain.request().newBuilder()
        val cookies = instance.getCookies()

        cookies.forEach {
            builder.addHeader("Cookie", it)
        }

        return chain.proceed(builder.build())
    }
}

//To intercept the cookies on API and save in repository
class SaveReceivedCookiesInterceptor(private val context: Context) : Interceptor {

    @JvmField
    val setCookieHeader = "Set-Cookie"

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val instance : Repository = Repository.getInstance(context)
        val originalResponse = chain.proceed(chain.request())

        if (!originalResponse.headers(setCookieHeader).isEmpty()) {
            val cookies = HashSet<String>()

            originalResponse.headers(setCookieHeader).forEach {
                cookies.add(it)
            }

            instance.setCookies(cookies)
        }

        return originalResponse
    }

}

// to add the intersections on the request and response in API
fun OkHttpClient.Builder.setCookieStore(context: Context) : OkHttpClient.Builder {
    return this
            .addInterceptor(SendSavedCookiesInterceptor(context))
            .addInterceptor(SaveReceivedCookiesInterceptor(context))
}