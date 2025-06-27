package com.example.applaporfik.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object NetworkUtils {
    
    /**
     * Check if device has internet connectivity
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
    
    /**
     * Test connection to backend server
     */
    suspend fun testBackendConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://70.153.16.232:5000/api/health")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"
            
            val responseCode = connection.responseCode
            connection.disconnect()
            
            responseCode == 200
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Get connection error message
     */
    fun getConnectionErrorMessage(exception: Exception): String {
        return when {
            exception.message?.contains("CLEARTEXT") == true -> 
                "Network security error: HTTP connections are blocked. Please check network configuration."
            exception.message?.contains("timeout") == true -> 
                "Connection timeout: Server is not responding. Please try again."
            exception.message?.contains("UnknownHostException") == true -> 
                "Cannot reach server: Please check your internet connection."
            else -> "Network error: ${exception.message}"
        }
    }
} 