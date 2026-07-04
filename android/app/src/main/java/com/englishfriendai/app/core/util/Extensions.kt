package com.englishfriendai.app.core.util

import android.util.Patterns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/** True for a syntactically valid email address; used by auth screen input validation. */
fun String.isValidEmail(): Boolean =
    isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

/** Simple password strength gate for registration; scaffold-level only. */
fun String.isValidPassword(): Boolean = length >= 8

fun String.orDash(): String = ifBlank { "-" }

/** Maps a Flow of domain values into [Result], funneling upstream exceptions into [Result.Error]. */
fun <T> Flow<T>.asResultFlow(): Flow<Result<T>> = this
    .map<T, Result<T>> { Result.Success(it) }
    .catch { throwable -> emit(Result.Error(throwable.message ?: "Unknown error", throwable)) }
