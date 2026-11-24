
package com.vathos.app

import android.app.Application
import com.vathos.app.di.VathosAppContainer

class VathosApplication : Application() {

    lateinit var container: VathosAppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        container = VathosAppContainer(this)
    }
}
