package com.bignerdranch.android.reshalaalfa01.data.remote

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

class PersistentCookieJar(context: Context) : CookieJar {
    private val sharedPreferences = context.getSharedPreferences("app_cookies", Context.MODE_PRIVATE)
    private val cookieStore = mutableMapOf<String, List<Cookie>>()

    init {
        // Load existing cookies from SP
        sharedPreferences.all.forEach { (url, serializedCookies) ->
            val cookieStrings = (serializedCookies as? String)?.split("|") ?: emptyList()
            cookieStore[url] = cookieStrings.mapNotNull { Cookie.parse("http://$url".toHttpUrl(), it) }
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url.host] = cookies
        sharedPreferences.edit().putString(url.host, cookies.joinToString("|") { it.toString() }).apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host] ?: emptyList()
    }

    fun clear() {
        cookieStore.clear()
        sharedPreferences.edit().clear().apply()
    }
}
