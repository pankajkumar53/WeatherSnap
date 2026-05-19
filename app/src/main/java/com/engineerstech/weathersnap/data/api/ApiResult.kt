package com.engineerstech.weathersnap.data.api

import retrofit2.Response
import java.io.IOException

sealed class ApiResult<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : ApiResult<T>(data)
    class Error<T>(message: String?, data: T? = null) : ApiResult<T>(data, message)
    class Loading<T> : ApiResult<T>()
    class Idle<T> : ApiResult<T>()
}


suspend fun <T> makeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            response.body()?.let {
                ApiResult.Success(it)
            } ?: ApiResult.Error("Empty response body")
        } else {
            ApiResult.Error("API call failed with error: ${response.errorBody()?.string()}")
        }
    } catch (e: IOException) {
        ApiResult.Error("Network error: ${e.message}")
    } catch (e: Exception) {
        ApiResult.Error("Unexpected error: ${e.message}")
    }
}