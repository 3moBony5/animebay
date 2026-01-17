package io.animebay.stream.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

private const val TAG = "ANIME_PLAYER_DEBUG"

@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    Log.d(TAG, "[LockScreenOrientation]: Composable is being evaluated. Desired orientation: ${if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) "LANDSCAPE" else "PORTRAIT"}")

    DisposableEffect(orientation) {
        val activity = context.findActivity() ?: run {
            Log.e(TAG, "[LockScreenOrientation]: Activity is NULL. Cannot set orientation.")
            return@DisposableEffect onDispose {}
        }
        
        val currentOrientation = activity.requestedOrientation
        Log.d(TAG, "[LockScreenOrientation]: DisposableEffect RUNNING. Key (orientation) changed to ${if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) "LANDSCAPE" else "PORTRAIT"}. Current system orientation is ${if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) "LANDSCAPE" else if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) "PORTRAIT" else "UNSPECIFIED"}.")
        
        activity.requestedOrientation = orientation
        Log.d(TAG, "[LockScreenOrientation]: ---> SETTING orientation to ${if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) "LANDSCAPE" else "PORTRAIT"}")

        onDispose {
            Log.w(TAG, "[LockScreenOrientation]: onDispose CALLED. This composable is being removed or recomposed. Resetting orientation to UNSPECIFIED.")
            // لا نستخدم originalOrientation لتجنب إعادة التعيين الخاطئة أثناء إعادة البناء
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            Log.w(TAG, "[LockScreenOrientation]: ---> RESET orientation to UNSPECIFIED.")
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun HideSystemBars() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        if (window == null) {
            Log.e(TAG, "[HideSystemBars]: Window is NULL.")
            return@DisposableEffect onDispose {}
        }
        
        Log.d(TAG, "[HideSystemBars]: Hiding system bars.")
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            Log.d(TAG, "[HideSystemBars]: onDispose CALLED. Showing system bars again.")
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
