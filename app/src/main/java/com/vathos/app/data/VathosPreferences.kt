
package com.vathos.app.data

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

data class VathosPreferences(
    val offlineMode: Boolean = true,
    val theme: AppTheme = AppTheme.SYSTEM
)
