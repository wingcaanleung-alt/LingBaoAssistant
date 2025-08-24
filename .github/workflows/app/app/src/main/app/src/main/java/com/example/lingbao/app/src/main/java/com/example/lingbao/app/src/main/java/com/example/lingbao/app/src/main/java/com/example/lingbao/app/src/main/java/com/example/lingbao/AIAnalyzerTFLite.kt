package com.example.lingbao

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

object AIAnalyzerTFLite {
    private var interpreter: Interpreter? = null

    fun init(context: Context) {
        if (interpreter != null) return
        try {
            // 读取 assets/predict.tflite
            val assetFileDescriptor = context.assets.openFd("predict.tflite")
            val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            interpreter = Interpreter(modelBuffer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * features: FloatArray -> example: [enemyCount, lastSpottedTimeDiff, someEncodedChampionIds...]
     * 返回：String 建议（你可以改为返回概率数组等）
     */
    fun predictSuggestion(features: FloatArray): String {
        if (interpreter == null) return "模型未初始化"
        // 将 features 转为 shape [1, N]
        val inputBuffer = arrayOf(features)
        val output = Array(1) { FloatArray(10) } // 假设输出长度 10（按模型调整）
        interpreter?.run(inputBuffer, output)
        val out = output[0]
        var bestIdx = 0
        var bestVal = out.getOrNull(0) ?: 0f
        for (i in out.indices) {
            val v = out[i]
            if (v > bestVal) {
                bestVal = v
                bestIdx = i
            }
        }
        return when (bestIdx) {
            0 -> "建议：推进中路"
            1 -> "建议：防守防反"
            2 -> "建议：打野抓人"
            else -> "建议：保持观望"
        }
    }
}

> 注：AIAnalyzerTFLite 假设模型输入/输出格式。你需要根据训练时的模型实际输入输出修改代码。
