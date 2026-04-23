package com.jellyflix.sample.trending

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Present solely to carry the plugin meta-data in the APK manifest and to
 * appear in PackageManager.queryIntentServices(). The host never actually
 * binds to this service — it reads meta-data and loads [TrendingPlugin] via
 * DexClassLoader.
 */
class TrendingEntry : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
