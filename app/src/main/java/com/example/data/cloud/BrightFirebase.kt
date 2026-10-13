package com.example.data.cloud

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Firebase for project "bright-a9edc", set up in code (no google-services.json, no plugin).
 *
 * The web API key is injected at build time as BuildConfig.FIREBASE_API_KEY (GitHub secret
 * FIREBASE_API_KEY). When it is empty, every call here returns null and the app keeps working
 * fully offline: history stays in Room on the phone, community features show "not connected".
 */
object BrightFirebase {
    private const val TAG = "BrightFirebase"
    private const val PROJECT_ID = "bright-a9edc"
    private const val APPLICATION_ID = "1:1088910074786:android:25e5e761e5c918ccc0c8fb"
    private const val GCM_SENDER_ID = "1088910074786"
    private const val STORAGE_BUCKET = "bright-a9edc.firebasestorage.app"
    private const val NETWORK_TIMEOUT_MS = 15_000L

    @Volatile
    private var app: FirebaseApp? = null

    @Volatile
    private var initFailed = false

    /** True when this build carries a Firebase API key. */
    val isConfigured: Boolean
        get() = BuildConfig.FIREBASE_API_KEY.isNotBlank()

    /** The initialised FirebaseApp, or null when Firebase is not configured or failed to start. */
    fun app(context: Context): FirebaseApp? {
        if (!isConfigured || initFailed) return null
        app?.let { return it }
        synchronized(this) {
            app?.let { return it }
            return try {
                val ctx = context.applicationContext
                val existing = FirebaseApp.getApps(ctx).firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
                val created = existing ?: FirebaseApp.initializeApp(
                    ctx,
                    FirebaseOptions.Builder()
                        .setApplicationId(APPLICATION_ID)
                        .setProjectId(PROJECT_ID)
                        .setGcmSenderId(GCM_SENDER_ID)
                        .setStorageBucket(STORAGE_BUCKET)
                        .setApiKey(BuildConfig.FIREBASE_API_KEY)
                        .build()
                )
                app = created
                created
            } catch (e: Exception) {
                Log.w(TAG, "Firebase unavailable; staying offline", e)
                initFailed = true
                null
            }
        }
    }

    fun firestore(context: Context): FirebaseFirestore? {
        val a = app(context) ?: return null
        return try {
            FirebaseFirestore.getInstance(a)
        } catch (e: Exception) {
            Log.w(TAG, "Firestore unavailable", e)
            null
        }
    }

    /** Current Firebase user id, signing in anonymously when needed. Null when offline/unconfigured. */
    suspend fun uid(context: Context): String? {
        val a = app(context) ?: return null
        return try {
            val auth = FirebaseAuth.getInstance(a)
            auth.currentUser?.uid ?: withTimeoutOrNull(NETWORK_TIMEOUT_MS) {
                auth.signInAnonymously().await().user?.uid
            }
        } catch (e: Exception) {
            Log.w(TAG, "Anonymous sign-in failed", e)
            null
        }
    }

    /** Uid only if already signed in (no network). */
    fun currentUid(context: Context): String? {
        val a = app(context) ?: return null
        return try {
            FirebaseAuth.getInstance(a).currentUser?.uid
        } catch (e: Exception) {
            null
        }
    }

    /** Used by Delete account only. Plain sign-out of Bright keeps the Firebase session. */
    fun signOut(context: Context) {
        val a = app(context) ?: return
        try {
            FirebaseAuth.getInstance(a).signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase sign-out failed", e)
        }
    }

    /** Runs [block] with a timeout; returns null on timeout or error. */
    suspend fun <T> network(block: suspend () -> T): T? = try {
        withTimeoutOrNull(NETWORK_TIMEOUT_MS) { block() }
    } catch (e: Exception) {
        Log.w(TAG, "Firebase call failed", e)
        null
    }
}

/** Minimal Task.await() so we don't need kotlinx-coroutines-play-services. */
suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            @Suppress("UNCHECKED_CAST")
            cont.resume(task.result as T)
        } else {
            cont.resumeWithException(task.exception ?: IllegalStateException("Firebase task failed"))
        }
    }
}
