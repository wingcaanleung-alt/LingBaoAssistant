package com.example.lingbao

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow

class FloatingWidgetService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = layoutInflater.inflate(R.layout.floating_widget, null)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 50
        params.y = 200

        windowManager.addView(floatingView, params)

        floatingView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        // 可加“吸附边缘”或保存位置逻辑（可选）
                        return true
                    }
                }
                return false
            }
        })

        val icon = floatingView.findViewById<ImageView>(R.id.widget_icon)
        icon.setOnClickListener {
            showMenu()
        }
    }

    private fun showMenu() {
        val menuView = layoutInflater.inflate(R.layout.floating_menu, null)
        val popup = PopupWindow(menuView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true)
        // 计算显示位置（显示在widget附近）
        val x = params.x + 60
        val y = params.y
        popup.showAtLocation(floatingView, Gravity.NO_GRAVITY, x, y)

        val btnSpotted = menuView.findViewById<Button>(R.id.btn_report_spotted)
        val btnMissing = menuView.findViewById<Button>(R.id.btn_report_missing)
        val btnOpen = menuView.findViewById<Button>(R.id.btn_open_main)

        btnSpotted.setOnClickListener {
            EventLogger.logEvent(this, "spotted", "位置:未知,time:${System.currentTimeMillis()}")
            // 可调用预测并TTS播报
            val events = EventLogger.readEvents(this)
            val suggestion = AIAnalyzerTFLite.predictSuggestion(prepareFeaturesFromEvents(events))
            // 使用 Voice TTS 简单播报（通过启动短期 TTS 服务或 intent，示例用系统日志/占位）
            // 这里我们通过发送广播或其他方式让 VoiceCommandService 处理 TTS（简化示例）
            EventLogger.logEvent(this, "suggestion", suggestion)
            popup.dismiss()
        }

        btnMissing.setOnClickListener {
            EventLogger.logEvent(this, "missing", "位置:未知,time:${System.currentTimeMillis()}")
            popup.dismiss()
        }

        btnOpen.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            startActivity(intent)
            popup.dismiss()
        }
    }

    private fun prepareFeaturesFromEvents(eventsString: String): FloatArray {
        // 简单示例：构造固定长度的特征向量
        // 实际应根据模型训练时使用的特征结构来组织
        val f = FloatArray(10) { 0f }
        // 例如：若最近有上报则设置第0位为1
        if (eventsString.contains("spotted")) f[0] = 1f
        return f
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }
}
