package me.rerere.rikkahub.data.nsfw

import me.rerere.ai.core.Message
import me.rerere.ai.core.MessageContent
import me.rerere.ai.core.MessageRole

/**
 * NSFW 消息注入器
 * 
 * 负责在实际消息发送前，将预设的隐藏对话注入到消息列表中
 * 这些消息对用户不可见，仅发送给 AI 用于建立上下文
 */
object NsfwInjector {
    
    /**
     * 将 NSFW 注入对话转换为 AI SDK 的 Message 格式
     * 
     * @param nsfwMessages NSFW 预设对话列表
     * @return 转换后的 Message 列表
     */
    private fun convertToAiMessages(nsfwMessages: List<NsfwPrompts.Message>): List<Message> {
        return nsfwMessages.map { nsfwMsg ->
            Message(
                role = when (nsfwMsg.role) {
                    "user" -> MessageRole.User
                    "assistant" -> MessageRole.Assistant
                    else -> MessageRole.User
                },
                content = listOf(
                    MessageContent.Text(nsfwMsg.content)
                )
            )
        }
    }
    
    /**
     * 将 NSFW 隐藏对话注入到消息列表开头
     * 
     * @param originalMessages 用户的原始消息列表
     * @param enabled 是否启用 NSFW 模式
     * @return 注入后的完整消息列表
     */
    fun injectHiddenMessages(
        originalMessages: List<Message>,
        enabled: Boolean
    ): List<Message> {
        if (!enabled) {
            return originalMessages
        }
        
        // 获取所有注入对话
        val hiddenMessages = convertToAiMessages(NsfwPrompts.ALL_INJECTION_MESSAGES)
        
        // 将隐藏对话插入到原始消息之前
        return hiddenMessages + originalMessages
    }
    
    /**
     * 检查当前对话是否需要注入 NSFW 上下文
     * 
     * @param conversationId 对话 ID
     * @param nsfwEnabled 数据库中的 NSFW 开关状态
     * @param globalFeatureEnabled 后台全局功能开关
     * @return true = 需要注入，false = 不需要
     */
    fun shouldInject(
        conversationId: String,
        nsfwEnabled: Boolean,
        globalFeatureEnabled: Boolean
    ): Boolean {
        // 只有当全局功能开启 且 当前对话开启 NSFW 时才注入
        return globalFeatureEnabled && nsfwEnabled
    }
}
