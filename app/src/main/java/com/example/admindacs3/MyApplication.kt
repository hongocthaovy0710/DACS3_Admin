package com.example.admindacs3

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config: MutableMap<String, String> = HashMap()
        config["cloud_name"] = "dogljqijs"
        config["api_key"] = "257597465547465"
        config["api_secret"] = "fW87431vn3ockgOd81r8HTscF-w"

        MediaManager.init(this, config)
    }
}
