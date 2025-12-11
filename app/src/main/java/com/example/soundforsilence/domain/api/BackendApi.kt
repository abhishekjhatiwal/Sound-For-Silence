package com.example.soundforsilence.domain.api

import retrofit2.http.GET
import retrofit2.http.Path

data class SignedUrlResponse(val videoUrl: String)

interface BackendApi {
    // Expected backend endpoint: GET /videos/{id}/signed-url
    @GET("videos/{id}/signed-url")
    suspend fun getSignedUrl(@Path("id") videoId: String): SignedUrlResponse
}
