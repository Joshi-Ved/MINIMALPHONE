package com.example.minimalphone

import android.app.Application

class FocusLiteApplication : Application() {
    val appContainer: AppContainer by lazy {
        AppContainer(this)
    }
}
