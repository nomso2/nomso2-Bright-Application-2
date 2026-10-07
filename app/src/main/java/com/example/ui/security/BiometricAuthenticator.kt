package com.example.ui.security

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Thin wrapper around androidx.biometric's [BiometricPrompt].
 *
 * Only [authenticate]'s onSuccess callback (fired from
 * [BiometricPrompt.AuthenticationCallback.onAuthenticationSucceeded]) should ever unlock anything.
 */
object BiometricAuthenticator {

    private const val ALLOWED_AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_STRONG

    /** True when the device has strong biometrics enrolled and ready to use. */
    fun canAuthenticate(context: Context): Boolean {
        return BiometricManager.from(context)
            .canAuthenticate(ALLOWED_AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Shows the system biometric prompt.
     *
     * @param onSuccess called only when the system confirms a successful biometric match.
     * @param onError called with a user-readable message when the prompt ends without success.
     *   Not called when the user simply cancels or taps the negative button.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButtonText: String = "Use PIN",
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                val userDismissed = errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_CANCELED
                if (!userDismissed) {
                    onError(errString.toString())
                }
            }
        }
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .build()
        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }
}

/** Walks the ContextWrapper chain to find the hosting [FragmentActivity], if any. */
fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is FragmentActivity) return current
        current = current.baseContext
    }
    return null
}
