package com.englishfriendai.app.di

import com.englishfriendai.app.BuildConfig
import com.englishfriendai.app.core.network.ApiService
import com.englishfriendai.app.core.network.AuthInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideBaseUrl(): String = BuildConfig.BASE_URL

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    // AuthInterceptor and SocketManager both use @Inject constructors (see their source files),
    // so Hilt constructs them automatically wherever they're requested below — no explicit
    // @Provides needed, and adding one here would create a duplicate-binding compile error.

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    /**
     * Certificate pinning stub.
     *
     * TODO: before a production release, populate a real [okhttp3.CertificatePinner] with the
     * SHA-256 SPKI pins for the production BASE_URL host (and its backup CA), e.g.:
     *
     * CertificatePinner.Builder()
     *     .add("api.englishfriendai.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
     *     .add("api.englishfriendai.com", "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=")
     *     .build()
     *
     * and pass it to the OkHttpClient.Builder below via `.certificatePinner(pinner)`. Leaving
     * this unset for the scaffold relies solely on the platform trust store (see
     * res/xml/network_security_config.xml) rather than pinning a specific certificate.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(com.englishfriendai.app.core.util.Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(com.englishfriendai.app.core.util.Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(com.englishfriendai.app.core.util.Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        // .certificatePinner(...) // TODO: see doc comment above.
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson, baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}
