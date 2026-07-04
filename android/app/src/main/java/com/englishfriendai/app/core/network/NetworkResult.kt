package com.englishfriendai.app.core.network

/** Outcome of a single remote (Retrofit) call, before it is mapped into domain [com.englishfriendai.app.core.util.Result]. */
sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int?, val message: String) : NetworkResult<Nothing>()
    data class Exception(val throwable: Throwable) : NetworkResult<Nothing>()
}

/** Runs [call] and converts Retrofit/OkHttp failures into [NetworkResult] instead of throwing. */
suspend fun <T> safeApiCall(call: suspend () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(call())
    } catch (httpException: retrofit2.HttpException) {
        NetworkResult.Error(httpException.code(), httpException.message())
    } catch (throwable: Throwable) {
        NetworkResult.Exception(throwable)
    }
}
