package com.nijatva.videoplayer.playback

import com.nijatva.videoplayer.aekant.AdBlocker
import com.nijatva.videoplayer.aekant.AdFilterEngine
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Blocks ad and tracker requests made while streaming, using the bundled
 * EasyList rules and the curated host lists.
 */
class AdBlockInterceptor(
    private val adBlocker: AdBlocker,
    private val isEnabled: () -> Boolean,
) : Interceptor {

    private var sinceSave = 0

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (isEnabled()) {
            val url = request.url.toString()
            if (AdFilterEngine.isBlocked(url) || adBlocker.isBlocked(url)) {
                AdFilterEngine.recordBlocked()
                if (++sinceSave >= 20) {
                    sinceSave = 0
                    AdFilterEngine.saveStats()
                }
                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(403)
                    .message("Blocked by Video Player")
                    .body("".toResponseBody(null))
                    .build()
            }
        }
        return chain.proceed(request)
    }
}
