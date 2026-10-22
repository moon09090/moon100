package com.aryan.overlay

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = (24 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(pad, pad, pad, pad)
        }
        status = TextView(this).apply { textSize = 18f; gravity = Gravity.CENTER }
        val test = Button(this).apply {
            text = "Test Yes Aryan overlay"
            setOnClickListener {
                sendBroadcast(Intent(OverlayService.ACTION_SHOW)
                    .putExtra("text", "Yes Aryan").putExtra("ms", 5000))
            }
        }
        root.addView(status)
        root.addView(test)
        setContentView(root)

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 1)
        }
    }

    override fun onResume() {
        super.onResume()
        if (Settings.canDrawOverlays(this)) {
            status.text = "Ready.\nOverlay service is running.\nTermux can now trigger it."
            startForegroundService(Intent(this, OverlayService::class.java))
        } else {
            status.text = "Please allow \"Display over other apps\" for Aryan Overlay, then come back."
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"))
            )
        }
    }
}
