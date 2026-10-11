package me.rerere.rikkahub.data.nsfw

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * NSFW 功能全局开关检测器
 *
 * 检查后台 API 返回值：
 * - KAIGUAN=开 → 所有用户可见 NSFW 按钮
 * - KAIGUAN=关 → 功能完全隐藏
 */
object NsfwConfigChecker {
    private const val CHECK_URL = "https://api.y11.top/fileecho.php?id=2775"
    private const val TIMEOUT_SECONDS = 10L

    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    /**
     * 检查 NSFW 功能是否全局启用
     * @return true = 启用（KAIGUAN=开），false = 禁用（KAIGUAN=关）
     */
    suspend fun isNsfwFeatureEnabled(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(CHECK_URL)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext false
                val body = response.body?.string() ?: return@withContext false
                return@withContext body.trim().contains("KAIGUAN=开", ignoreCase = true)
            }
        } catch (e: Exception) {
            // 网络异常时默认关闭
            e.printStackTrace()
            return@withContext false
        }
    }
}
