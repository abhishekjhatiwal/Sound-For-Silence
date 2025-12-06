package com.example.soundforsilence.util

object ValidationUtils {
    fun isValidPhone(phone: String): Boolean = phone.length >= 10
    fun isValidPassword(password: String): Boolean = password.length >= 4
}