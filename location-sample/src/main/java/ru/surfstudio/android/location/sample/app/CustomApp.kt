package ru.surfstudio.android.location.sample.app

import android.app.Activity
import androidx.multidex.MultiDexApplication
import ru.surfstudio.android.activity.holder.ActiveActivityHolder
import ru.surfstudio.standard.app_injector.ui.DefaultActivityLifecycleCallbacks
import ru.surfstudio.android.sample.dagger.app.dagger.DefaultAppModule
import ru.surfstudio.android.location.sample.app.dagger.CustomAppComponent
import ru.surfstudio.android.location.sample.app.dagger.DaggerCustomAppComponent

/**
 * Класс приложения
 */
open class CustomApp : MultiDexApplication() {

    val activeActivityHolder = ActiveActivityHolder()
    var customAppComponent: CustomAppComponent? = null

    override fun onCreate() {
        super.onCreate()
        initInjector()
        registerActiveActivityListener()
    }

    protected open fun initInjector() {
        customAppComponent = DaggerCustomAppComponent.builder()
                .defaultAppModule(DefaultAppModule(this, activeActivityHolder))
                .build()
    }

    /**
     * Регистрирует слушатель аткивной активити
     */
    private fun registerActiveActivityListener() {
        registerActivityLifecycleCallbacks(object : ru.surfstudio.standard.app_injector.ui.DefaultActivityLifecycleCallbacks() {
            override fun onActivityResumed(activity: Activity) {
                activeActivityHolder.activity = activity
            }

            override fun onActivityStopped(activity: Activity) {
                activeActivityHolder.clearActivity()
            }
        })
    }
}