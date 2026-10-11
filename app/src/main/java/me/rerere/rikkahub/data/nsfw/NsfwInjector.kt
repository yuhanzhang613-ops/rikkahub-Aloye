package me.rerere.rikkahub.data.nsfw

import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart

/**
 * NSFW 消息注入器
 *
 * 负责在实际消息发送前，将预设的隐藏对话注入到消息列表中。
 * 这些消息对用户不可见，仅发送给 AI 用于建立上下文。
 */
object NsfwInjector {

    /** 将 NSFW 预设对话转换为 AI SDK 的 UIMessage。 */
    private fun convertToAiMessages(
        nsfwMessages: List<NsfwPrompts.Message>
    ): List<UIMessage> {
        return nsfwMessages.map { nsfwMessage ->
            val role = when (nsfwMessage.role.lowercase()) {
                "assistant" -> MessageRole.ASSISTANT
                else -> MessageRole.USER
            }
            UIMessage(
                role = role,
                parts = listOf(UIMessagePart.Text(nsfwMessage.content)),
                isSynthetic = true,
            )
        }
    }

    /**
     * 将 NSFW 隐藏对话注入到消息列表开头。
     */
    fun injectHiddenMessages(
        originalMessages: List<UIMessage>,
        enabled: Boolean,
    ): List<UIMessage> {
        if (!enabled) return originalMessages
        return convertToAiMessages(NsfwPrompts.ALL_INJECTION_MESSAGES) + originalMessages
    }

    /**
     * 检查当前对话是否需要注入 NSFW 上下文。
     */
    fun shouldInject(
        conversationId: String,
        nsfwEnabled: Boolean,
        globalFeatureEnabled: Boolean,
    ): Boolean {
        // conversationId 保留在接口中，便于后续按会话做审计或策略控制。
        return conversationId.isNotBlank() && globalFeatureEnabled && nsfwEnabled
    }
}
