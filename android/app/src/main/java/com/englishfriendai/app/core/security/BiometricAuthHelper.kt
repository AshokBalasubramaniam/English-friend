package com.englishfriendai.app.core.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import javax.inject.Inject

/**
 * Thin wrapper around androidx.biometric so ViewModels/composables don't touch the
 * FragmentActivity-based API directly. Used to gate app re-entry / sensitive actions
 * (e.g. viewing saved transcripts) behind device biometrics when enabled in Settings.
 */
class BiometricAuthHelper @Inject constructor() {

    sealed class BiometricResult {
        data object Success : BiometricResult()
        data class Error(val message: String) : BiometricResult()
        data object Failed : BiometricResult()
    }

    /** Whether the device currently has usable biometric hardware enrolled. */
    fun isBiometricAvailable(activity: FragmentActivity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onResult: (BiometricResult) -> Unit
    ) {
        val executor = androidx.core.content.ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onResult(BiometricResult.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onResult(BiometricResult.Error(errString.toString()))
            }

            override fun onAuthenticationFailed() {
                onResult(BiometricResult.Failed)
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        prompt.authenticate(promptInfo)
    }
}
