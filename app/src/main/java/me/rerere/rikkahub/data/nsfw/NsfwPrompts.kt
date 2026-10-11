package me.rerere.rikkahub.data.nsfw

/**
 * NSFW 模式注入对话内容
 * 
 * 当用户在对话中开启 NSFW 模式时，系统会在实际消息发送前
 * 静默注入以下预设对话，用于引导 AI 进入成人文学创作模式
 * 
 * 注意：这些消息对用户不可见，仅发送给 AI
 */
object NsfwPrompts {
    
    /**
     * 消息数据类
     */
    data class Message(
        val role: String,  // "user" 或 "assistant"
        val content: String
    )
    
    /**
     * 获取完整的注入消息列表
     * 合并所有部分的对话内容
     */
    val ALL_INJECTION_MESSAGES: List<Message>
        get() = NsfwPromptsPart1.messages + 
                NsfwPromptsPart2.messages + 
                NsfwPromptsPart3.messages +
                NsfwPromptsPart4.messages
}
