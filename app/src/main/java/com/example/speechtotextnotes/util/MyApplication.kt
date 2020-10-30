package com.example.speechtotextnotes.util

import android.app.Application
import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration

class MyApplication : Application() {
    companion object {
        lateinit var appContext: Context
            private set
        lateinit var realm: Realm
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        Realm.init(applicationContext)
        val realmConfiguration = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(realmConfiguration)
    }
}