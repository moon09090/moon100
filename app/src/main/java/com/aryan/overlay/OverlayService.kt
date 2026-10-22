package com.aryan.overlay

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.webkit.WebView

/**
 * Keeps a tiny foreground service alive. When Termux sends the SHOW broadcast,
 * it draws a transparent, touch-through WebView over the live home screen
 * (comets + calligraphy text), lightly blurs what is behind it, then removes it.
 */
class OverlayService : Service() {

    companion object {
        const val ACTION_SHOW = "com.aryan.overlay.SHOW"
        private const val CHANNEL = "aryan_overlay"
        private const val BLUR_RADIUS = 8   // "very low" blur. 0 = off, bigger = blurrier
    }

    private lateinit var wm: WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var web: WebView? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val text = intent?.getStringExtra("text") ?: "Yes Aryan"
            val ms = intent?.getIntExtra("ms", 6000) ?: 6000
            show(text, ms)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Aryan Overlay", NotificationManager.IMPORTANCE_MIN)
        )
        val n = Notification.Builder(this, CHANNEL)
            .setContentTitle("Aryan Overlay is ready")
            .setSmallIcon(android.R.drawable.star_on)
            .build()
        startForeground(1, n)

        val filter = IntentFilter(ACTION_SHOW)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun show(text: String, ms: Int) {
        hide()

        val w = WebView(this)
        w.setBackgroundColor(Color.TRANSPARENT)
        w.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        w.settings.javaScriptEnabled = true
        w.isClickable = false
        w.isFocusable = false

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or      // touches go to the home screen
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        )
        if (Build.VERSION.SDK_INT >= 28) {
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        if (Build.VERSION.SDK_INT >= 31) {
            lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            lp.blurBehindRadius = BLUR_RADIUS
        }

        w.loadUrl("file:///android_asset/index.html?text=" + Uri.encode(text) + "&ms=" + ms)
        try {
            wm.addView(w, lp)
            web = w
            handler.postDelayed({ hide() }, ms.toLong())
        } catch (e: Exception) {
            w.destroy()
        }
    }

    private fun hide() {
        handler.removeCallbacksAndMessages(null)
        web?.let {
            try { wm.removeView(it) } catch (_: Exception) {}
            it.destroy()
        }
        web = null
    }

    override fun onDestroy() {
        hide()
        try { unregisterReceiver(receiver) } catch (_: Exception) {}
        super.onDestroy()
    }
}
