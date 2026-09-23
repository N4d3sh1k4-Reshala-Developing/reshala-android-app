package com.bignerdranch.android.reshalaalfa01

import android.app.Application
import com.vk.id.VKID

class ReshalaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        VKID.init(this)
    }
}
