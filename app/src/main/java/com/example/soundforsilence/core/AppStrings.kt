package com.example.soundforsilence.core

data class AppStrings(
    val appName: String,
    val homeTitle: String,
    val settingsTitle: String,
    val progressTitle: String,
    val profileTitle: String,
    val childDetailsTitle: String,
    val darkMode: String,
    val language: String,
    val logout: String,
)

fun stringsFor(language: AppLanguage): AppStrings {
    return when (language) {
        AppLanguage.ENGLISH -> AppStrings(
            appName = "Sound for Silence",
            homeTitle = "Home",
            settingsTitle = "Settings",
            progressTitle = "Progress",
            profileTitle = "My Profile",
            childDetailsTitle = "Child Details",
            darkMode = "Dark Mode",
            language = "Language / भाषा",
            logout = "Logout"
        )
        AppLanguage.HINDI -> AppStrings(
            appName = "साउंड फॉर साइलेंस",
            homeTitle = "होम",
            settingsTitle = "सेटिंग्स",
            progressTitle = "प्रोग्रेस",
            profileTitle = "मेरा प्रोफ़ाइल",
            childDetailsTitle = "बच्चे की जानकारी",
            darkMode = "डार्क मोड",
            language = "भाषा / Language",
            logout = "लॉग आउट"
        )
    }
}
