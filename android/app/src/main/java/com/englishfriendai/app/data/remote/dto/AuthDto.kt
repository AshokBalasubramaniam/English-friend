package com.englishfriendai.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class GoogleLoginRequest(
    @SerializedName("id_token") val idToken: String
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

// The backend wraps every response as { success, data: {...} } — see authController.js —
// so the actual payload sits one level deeper than these response DTOs alone can express.
data class AuthEnvelope(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: LoginResponse
)

data class RefreshEnvelope(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: RefreshTokenResponse
)

data class LoginResponse(
    @SerializedName("user") val user: UserDto,
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)

data class RefreshTokenResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("streak_days") val streakDays: Int
)
