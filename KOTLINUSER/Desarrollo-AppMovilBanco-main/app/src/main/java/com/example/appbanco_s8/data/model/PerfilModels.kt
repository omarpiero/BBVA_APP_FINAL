package com.example.appbanco_s8.data.model

import com.google.gson.annotations.SerializedName

data class Perfil(
    val id: String? = null,
    @SerializedName("user_id") val userId: String,
    val nombre: String? = null,
    val telefono: String? = null,
    val email: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
