package com.example.applaporfik.data.api

import com.example.applaporfik.model.FeedbackItem
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.PATCH
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

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

// Data classes for reports
data class Report(
    val id: Int,
    val user_id: Int,
    val kategori: String,
    val judul: String,
    val rincian: String,
    val images: List<String>?,
    val status: String,
    val feedback: String?,
    val created_at: String,
    val updated_at: String,
    val user_nama: String,
    val user_nim: String,
    val user_jurusan: String
)

data class ReportResponse(val success: Boolean, val reports: List<Report>?, val message: String?)
data class ReportStatsResponse(val success: Boolean, val stats: ReportStats?, val message: String?)

data class ReportStats(
    val today: Int,
    val this_week: Int,
    val all_time: Int,
    val unread_count: Int
)

// Data classes for report submission
data class SubmitReportRequest(
    val kategori: String,
    val judul: String,
    val rincian: String
)

data class SubmitReportResponse(
    val success: Boolean,
    val message: String?,
    val report_id: Int?
)

data class ReportDetailResponse(
    val success: Boolean,
    val report: Report?,
    val message: String?
)

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

    // Reports endpoints
    @GET("reports")
    suspend fun getReports(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("status") status: String? = null,
        @Query("kategori") kategori: String? = null,
        @Query("sort") sort: String = "created_at",
        @Query("order") order: String = "desc"
    ): ReportResponse

    @GET("reports/latest")
    suspend fun getLatestReports(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 2
    ): ReportResponse

    @GET("reports/stats")
    suspend fun getReportStats(
        @Header("Authorization") token: String
    ): ReportStatsResponse

    @GET("reports/recent")
    suspend fun getRecentReportsPublic(
        @Query("limit") limit: Int = 5
    ): ReportResponse

    @GET("reports")
    suspend fun getReportsPublic(
        @Query("public") public: String = "true",
        @Query("limit") limit: Int = 5
    ): ReportResponse

    @POST("reports")
    suspend fun submitReport(
        @Header("Authorization") token: String,
        @Body request: SubmitReportRequest
    ): SubmitReportResponse

    @Multipart
    @POST("reports/upload-images")
    suspend fun uploadImages(
        @Header("Authorization") token: String,
        @Part("report_id") reportId: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): SubmitReportResponse

    @PATCH("reports/{id}/feedback")
    suspend fun sendFeedback(
        @Path("id") reportId: Int,
        @Header("Authorization") token: String,
        @Body feedbackRequest: FeedbackRequest
    ): SubmitReportResponse

    @GET("reports/{id}")
    suspend fun getReportById(
        @Header("Authorization") token: String,
        @Path("id") reportId: Int
    ): ReportDetailResponse

    @PATCH("reports/{id}/resolve")
    suspend fun markReportAsResolved(
        @Path("id") reportId: Int,
        @Header("Authorization") token: String
    ): SubmitReportResponse

    @PATCH("reports/{id}/status")
    suspend fun updateReportStatus(
        @Path("id") reportId: Int,
        @Header("Authorization") token: String,
        @Body statusRequest: StatusRequest
    ): SubmitReportResponse

    companion object {
        // TODO: Update this URL to your VPS domain
        // For local development: "http://10.0.2.2:5000/api/"
        // For production: "https://your-domain.com/api/"
        private const val BASE_URL = "http://70.153.16.232:5000/api/"

        fun create(): ApiService {
            // Create OkHttpClient with timeout settings
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }
}