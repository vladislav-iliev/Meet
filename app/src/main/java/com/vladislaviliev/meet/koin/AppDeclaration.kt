package com.vladislaviliev.meet.koin

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication

fun KoinApplication.appDeclaration(context: Context) {
    androidLogger()
    androidContext(context)
    modules(appModule)
}