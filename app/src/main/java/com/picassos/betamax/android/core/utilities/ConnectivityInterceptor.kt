package com.picassos.betamax.android.core.utilities

import com.picassos.betamax.android.core.utilities.Response.INTERNET_CONNECTION_EXCEPTION
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ConnectivityInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!Connectivity.instance.isOnline()) {
            throw IOException(INTERNET_CONNECTION_EXCEPTION)
        } else {
            return chain.proceed(chain.request())
        }
    }
}