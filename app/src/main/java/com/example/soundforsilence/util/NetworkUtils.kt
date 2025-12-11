package com.example.soundforsilence.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class NetworkUtils(
    private val client: OkHttpClient
) {
    suspend fun isUrlReachable(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url(url).head().build()
            val resp = client.newCall(req).execute()
            val ok = resp.isSuccessful
            resp.close()
            ok
        } catch (e: Exception) {
            false
        }
    }
}

