package com.woocommerce.android

import android.app.Activity
import android.app.Application
import com.woocommerce.android.di.AppComponent
import com.woocommerce.android.di.DaggerAppComponent
import com.yarolegovich.wellsql.WellSql
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import org.wordpress.android.fluxc.persistence.WellSqlConfig
import javax.inject.Inject

open class WooCommerce : Application(), HasActivityInjector {
    @Inject lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    protected open val component: AppComponent by lazy {
        DaggerAppComponent.builder()
                .application(this)
                .build()
    }

    override fun onCreate() {
        super.onCreate()
        component.inject(this)
        WellSql.init(WellSqlConfig(applicationContext))
    }

    override fun activityInjector(): AndroidInjector<Activity> = activityInjector
}