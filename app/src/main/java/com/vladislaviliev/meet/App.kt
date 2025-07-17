package com.vladislaviliev.meet

import android.app.Application
import com.vladislaviliev.meet.koin.appDeclaration
import org.koin.core.context.GlobalContext.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin { appDeclaration(this@App) }
    }
}