package com.example.soundforsilence.domain.model

data class ChildProfileState(
    val name: String = "",
    val age: String = "",
    val notes: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

data class ChildProfile(
    val name: String = "",
    val age: String = "",
    val notes: String = ""
)