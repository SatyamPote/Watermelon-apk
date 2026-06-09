package com.watermelon.app

import android.app.Application
import coil.Coil
import coil.ImageLoader
import com.yausername.youtubedl_android.YoutubeDL
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class WatermelonApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Coil.setImageLoader {
            ImageLoader.Builder(this@WatermelonApplication)
                .crossfade(true)
                .build()
        }
        // YoutubeDL init off main thread — prevents ANR at startup.
        Thread {
            runCatching {
                YoutubeDL.getInstance().init(this@WatermelonApplication)
                YoutubeDL.getInstance().updateYoutubeDL(this@WatermelonApplication)
                Timber.i("YoutubeDL initialized in background")
            }.onFailure { Timber.e(it, "YoutubeDL init failed") }
        }.start()
    }
}
