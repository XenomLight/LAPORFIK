package com.example.applaporfik

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.applaporfik.util.SessionManager

class AppLifecycleService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Make the service sticky so it survives as long as possible
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d("AppLifecycleService", "onTaskRemoved called")
        val sessionManager = SessionManager(this)
        if (!sessionManager.isRememberMeEnabled()) {
            sessionManager.clearSession()
            Log.d("AppLifecycleService", "Session cleared (Remember Me not enabled)")
        } else {
            Log.d("AppLifecycleService", "Session NOT cleared (Remember Me enabled)")
        }
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }
} 