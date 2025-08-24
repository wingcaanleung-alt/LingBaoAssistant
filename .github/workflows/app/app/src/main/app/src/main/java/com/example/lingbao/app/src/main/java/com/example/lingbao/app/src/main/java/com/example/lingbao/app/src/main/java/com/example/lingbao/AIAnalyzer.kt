package com.example.lingbao

object AIAnalyzer {
    fun getAnswer(command: String): String {
        val s = command.lowercase()
        return when {
            s.contains("出装") || s.contains("装备") -> "建议参考：打野刀 + 攻速鞋 + 暴击输出装。灵宝建议先观察敌方AP/AD倾向。"
            s.contains("打野") -> "建议优先做视野，支援线上；如果对面打野强势，注意呼叫队友带线。"
            s.contains("技能连招") -> "常见连招：技能一 -> 平A -> 技能二，视英雄而定。"
            s.contains("视野") -> "灵宝提示：关键草丛与河道点放置视野，有利预测敌方动向。"
            else -> "灵宝还在学习中，能不能换个问法试试？"
        }
    }
}
