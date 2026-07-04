package com.englishfriendai.app.core.util

/**
 * Generic wrapper used by domain use cases to communicate outcome + loading state up to the
 * presentation layer, independent of how the data was actually fetched (network vs local DB).
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    /** Marker for use cases that are intentionally not implemented yet in this scaffold. */
    data object NotImplemented : Result<Nothing>()

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (String, Throwable?) -> Unit): Result<T> {
        if (this is Error) action(message, throwable)
        return this
    }
}
