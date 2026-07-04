package com.englishfriendai.app.core.util

/**
 * App-wide constant values. Anything environment-specific (BASE_URL, API keys) belongs in
 * BuildConfig / local.properties instead of here.
 */
object Constants {

    // Persistence
    const val DATABASE_NAME = "english_friend_ai.db"
    const val DATASTORE_NAME = "english_friend_ai_prefs"
    const val ENCRYPTED_PREFS_NAME = "english_friend_ai_secure_prefs"

    // Networking
    const val NETWORK_TIMEOUT_SECONDS = 30L
    const val HEADER_AUTHORIZATION = "Authorization"
    const val BEARER_PREFIX = "Bearer "

    // Socket.IO events (real-time streaming chat) — names must match backend/src/sockets/chatSocket.js
    const val SOCKET_EVENT_CONNECT = "connect"
    const val SOCKET_EVENT_DISCONNECT = "disconnect"
    const val SOCKET_EVENT_MESSAGE_CHUNK = "ai_reply_chunk"
    const val SOCKET_EVENT_MESSAGE_COMPLETE = "ai_reply_done"
    const val SOCKET_EVENT_SEND_MESSAGE = "user_message"
    const val SOCKET_EVENT_ERROR = "ai_reply_error"

    // WorkManager
    const val WORK_DAILY_REMINDER = "work_daily_reminder"
    const val NOTIFICATION_CHANNEL_REMINDERS = "channel_daily_reminders"
    const val NOTIFICATION_ID_DAILY_REMINDER = 1001

    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
}
