package com.lira.field.core.network

import com.squareup.moshi.Json

data class ApiErrorDetail(
    @Json(name = "code") val code: String,
    @Json(name = "message") val message: String,
    @Json(name = "request_id") val requestId: String? = null
)

data class ApiErrorResponse(
    @Json(name = "error") val error: ApiErrorDetail
)

sealed class LiraNetworkResult<out T> {
    data class Success<T>(val data: T) : LiraNetworkResult<T>()
    data class Error(val code: String, val message: String, val httpStatus: Int? = null) : LiraNetworkResult<Nothing>()
    data class OfflineQueued(val actionId: String, val message: String) : LiraNetworkResult<Nothing>()
}

