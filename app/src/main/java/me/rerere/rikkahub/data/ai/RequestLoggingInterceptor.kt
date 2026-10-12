package me.rerere.rikkahub.data.ai

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.rerere.common.android.LogEntry
import me.rerere.common.android.Logging
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

class RequestLoggingInterceptor : Interceptor {
    private val json = Json { ignoreUnknownKeys = true }

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!Logging.isRequestLoggingEnabled()) {
            return chain.proceed(chain.request())
        }

        val request = chain.request()
        val startTime = System.currentTimeMillis()

        val requestHeaders = request.headers.toMap()
        val requestBody = request.body?.let { body ->
            val buffer = Buffer()
            body.writeTo(buffer)
            val originalBody = buffer.readUtf8()
            
            // 过滤掉 synthetic 消息
            filterSyntheticMessages(originalBody)
        }

        val response: Response
        var error: String? = null

        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            error = e.message
            Logging.logRequest(
                LogEntry.RequestLog(
                    tag = "HTTP",
                    url = request.url.toString(),
                    method = request.method,
                    requestHeaders = requestHeaders,
                    requestBody = requestBody,
                    error = error
                )
            )
            throw e
        }

        val durationMs = System.currentTimeMillis() - startTime
        val responseHeaders = response.headers.toMap()

        Logging.logRequest(
            LogEntry.RequestLog(
                tag = "HTTP",
                url = request.url.toString(),
                method = request.method,
                requestHeaders = requestHeaders,
                requestBody = requestBody,
                responseCode = response.code,
                responseHeaders = responseHeaders,
                durationMs = durationMs,
                error = error
            )
        )

        return response
    }

    /**
     * 过滤掉 messages 数组中 isSynthetic = true 的消息
     * 保护 NSFW 预设内容不被请求日志泄露
     */
    private fun filterSyntheticMessages(body: String): String {
        return try {
            val jsonElement = json.parseToJsonElement(body)
            if (jsonElement !is JsonObject) return body

            val messages = jsonElement["messages"]?.jsonArray ?: return body
            
            // 过滤掉 synthetic 消息
            val filteredMessages = messages.filter { message ->
                val isSynthetic = message.jsonObject["isSynthetic"]?.jsonPrimitive?.content?.toBoolean() ?: false
                !isSynthetic
            }

            // 重建 JSON
            val newJson = JsonObject(
                jsonElement.toMutableMap().apply {
                    put("messages", JsonArray(filteredMessages))
                }
            )

            json.encodeToString(JsonElement.serializer(), newJson)
        } catch (e: Exception) {
            // 解析失败时返回原始 body（避免日志记录失败）
            body
        }
    }

    private fun okhttp3.Headers.toMap(): Map<String, String> {
        return names().associateWith { name ->
            if (name.equals("Proxy-Authorization", ignoreCase = true)) {
                "██"
            } else {
                get(name) ?: ""
            }
        }
    }
}
