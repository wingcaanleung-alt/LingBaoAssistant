package com.example.lingbao

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 如果没有悬浮窗权限，引导用户去系统设置开启
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        } else {
            startService(Intent(this, FloatingWidgetService::class.java))
        }

        // 启动语音服务（注意：RECORDER 权限在运行时请求）
        startService(Intent(this, VoiceCommandService::class.java))

        // 初始化 TFLite 分析器（若存在）
        AIAnalyzerTFLite.init(this)
    }
}
