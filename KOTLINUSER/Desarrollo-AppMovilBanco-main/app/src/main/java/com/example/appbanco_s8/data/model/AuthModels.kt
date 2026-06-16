
package com.example.appbanco_s8.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email:    String,
    val password: String
)

data class AuthResponse(
    @SerializedName("access_token")  val accessToken:  String = "",
    @SerializedName("refresh_token") val refreshToken: String = "",
    @SerializedName("expires_in")    val expiresIn:    Int    = 0,
    @SerializedName("token_type")    val tokenType:    String = "",
    val user:  UserData? = null,
    val error: String?   = null,
    @SerializedName("error_description")
    val errorDescription: String? = null
)

data class UserData(
    val id:    String = "",
    val email: String = ""
)