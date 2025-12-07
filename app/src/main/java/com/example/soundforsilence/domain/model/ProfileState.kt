package com.example.soundforsilence.domain.model

data class ProfileState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val newPassword: String = "",
    val imageUrl: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showReauthDialog: Boolean = false
)