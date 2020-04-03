package com.example.subway_for_pregnant

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp()
        startKotin{
          androidContext(this@MyApplication)
            modules(
              listOf(getNetworkModule("개인 서버 주소."))
            )
        }
    }
}