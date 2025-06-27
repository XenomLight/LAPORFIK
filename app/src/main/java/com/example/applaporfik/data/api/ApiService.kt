package com.example.applaporfik.data.api

import com.example.applaporfik.model.FeedbackItem
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

// Data classes for authentication
data class LoginRequest(val nim: String, val password: String)
data class LoginResponse(val success: Boolean, val token: String?, val role: String?, val message: String?, val user: UserProfile?)

data class RegisterRequest(val nama: String, val nim: String, val password: String)
data class RegisterResponse(val success: Boolean, val message: String?)

// Data classes for user profile
data class UserProfile(
    val id: Int, 
    val nama: String, 
    val nim: String, 
    val jurusan: String, 
    val gmail: String?, 
    val profile_url: String?, 
    val role: String
)
data class UserProfileResponse(val success: Boolean, val user: UserProfile?, val message: String?)

interface ApiService {
    @GET("feedback")
    suspend fun getFeedbackList(): List<FeedbackItem>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse
    
    @GET("user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String,
        @Query("nim") nim: String
    ): UserProfileResponse
    
    // Add more endpoints as needed
    
    companion object {
        // TODO: Update this URL to your VPS domain
        // For local development: "http://10.0.2.2:5000/api/"
        // For production: "https://your-domain.com/api/"
        private const val BASE_URL = "http://70.153.16.232:5000/api/"
        
        fun create(): ApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            return retrofit.create(ApiService::class.java)
        }
    }
} 