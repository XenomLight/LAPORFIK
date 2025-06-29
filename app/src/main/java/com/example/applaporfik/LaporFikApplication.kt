package com.example.applaporfik

import android.app.Application
import com.example.applaporfik.util.SessionManager

class LaporFikApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
    
    override fun onTerminate() {
        super.onTerminate()
        // This is called when the app is being terminated
        handleAppTermination()
    }
    
    private fun handleAppTermination() {
        val sessionManager = SessionManager(this)
        
        // If remember me is not enabled, clear session data
        if (!sessionManager.isRememberMeEnabled()) {
            sessionManager.clearSession()
        }
    }
} 