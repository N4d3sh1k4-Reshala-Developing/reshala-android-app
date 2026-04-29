package com.bignerdranch.android.reshalaalfa01.ui.util

import android.util.Patterns

object Validator {
    private val PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()])[a-zA-Z\\d!@#$%^&*()]+$".toRegex()

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && 
               email.length <= 50 && 
               Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidRegisterPassword(password: String): Boolean {
        return password.isNotBlank() && 
               password.length in 8..50 && 
               PASSWORD_PATTERN.matches(password)
    }

    fun isValidLoginPassword(password: String): Boolean {
        return password.isNotBlank() && password.length <= 50
    }
}
