package com.stardust.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Stardust on 2018/3/22.
 */
object GlobalAppContext {

    const val TAG = "GlobalAppContext"

    @SuppressLint("StaticFieldLeak")
    private var sApplicationContext: Context? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    @JvmStatic
    lateinit var buildConfig: BuildConfig
        private set

    @JvmStatic
    val autojsPackageName by lazy { buildConfig.APPLICATION_ID }

    fun set(a: Application, buildConfig: BuildConfig) {
        this.buildConfig = buildConfig
        Log.i(TAG, buildConfig.toString())
        sApplicationContext = a.applicationContext
    }

    @JvmStatic
    fun get(): Context {
        checkNotNull(sApplicationContext) { "Call GlobalAppContext.set() to set a application context" }
        return sApplicationContext!!
    }

    @JvmStatic
    fun getString(resId: Int): String {
        return get().getString(resId)
    }

    @JvmStatic
    fun getString(resId: Int, vararg formatArgs: Any?): String {
        return get().getString(resId, *formatArgs)
    }

    fun getColor(id: Int): Int {
        return ContextCompat.getColor(get(), id)
    }

    @JvmStatic
    fun toast(message: String?) {
        scope.launch { com.stardust.toast(get(), message) }
    }

    @JvmStatic
    fun toast(resId: Int) {
        scope.launch { com.stardust.toast(get(), resId) }
    }

    @JvmStatic
    fun toast(resId: Int, vararg args: Any?) {
        toast(false, resId, *args)
    }

    @JvmStatic
    fun toast(isLongToast: Boolean, resId: Int, vararg args: Any?) {
        scope.launch {
            com.stardust.toast(get(), getString(resId, *args), isLongToast)
        }
    }

    @JvmStatic
    fun post(r: Runnable) {
        scope.launch { r.run() }
    }

    @JvmStatic
    fun postDelayed(r: Runnable, m: Long) {
        scope.launch { delay(m); r.run() }
    }

    @JvmStatic
    @get:Synchronized
    val appName: String
        get() = try {
            val packageManager = get().packageManager
            val packageInfo = packageManager.getPackageInfo(
                get().packageName, 0
            )
            packageManager.getApplicationLabel(packageInfo.applicationInfo).toString()
        } catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())
            ""
        }


    @JvmStatic
    @get:Synchronized
    val appIcon: Drawable?
        get() {
            try {
                val packageManager = get().packageManager
                val packageInfo = packageManager.getPackageInfo(
                    get().packageName, 0
                )
                return packageManager.getApplicationIcon(packageInfo.applicationInfo)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
}