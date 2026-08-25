package com.vigizoomato.customer.data.network

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Single place that talks HTTP to the OrderAra backend.
 *
 * Every call is blocking, so callers must already be on a background
 * dispatcher (Dispatchers.IO).
 */
object ApiClient {

    private const val TAG = "ApiClient"
    private const val TIMEOUT_MS = 8000

    data class Response(val code: Int, val body: JSONObject?) {
        val isSuccess: Boolean get() = code in 200..299 && body?.optBoolean("success", false) == true
        val message: String get() = body?.optString("message") ?: "Network error ($code)"
        val data: JSONObject? get() = body?.optJSONObject("data")
        val dataArray: JSONArray? get() = body?.optJSONArray("data")
    }

    fun get(path: String): Response = request("GET", path, null)

    fun post(path: String, payload: JSONObject? = null): Response = request("POST", path, payload)

    fun patch(path: String, payload: JSONObject? = null): Response = request("PATCH", path, payload)

    fun delete(path: String): Response = request("DELETE", path, null)

    private fun request(method: String, path: String, payload: JSONObject?): Response {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(if (path.startsWith("http")) path else "${ApiConfig.BASE_URL}$path")
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                setRequestProperty("Accept", "application/json")
                // HttpURLConnection rejects PATCH/DELETE on some Android builds,
                // so those go out as POST with the real verb in a header that
                // the backend restores before routing.
                if (method == "PATCH" || method == "DELETE") {
                    requestMethod = "POST"
                    setRequestProperty("X-HTTP-Method-Override", method)
                } else {
                    requestMethod = method
                }
            }

            if (payload != null) {
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use {
                    it.write(payload.toString())
                    it.flush()
                }
            }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.let { BufferedReader(InputStreamReader(it, Charsets.UTF_8)).use { r -> r.readText() } }
            val json = text?.takeIf { it.isNotBlank() }?.let {
                runCatching { JSONObject(it) }.getOrNull()
            }
            Response(code, json)
        } catch (e: Exception) {
            Log.e(TAG, "$method $path failed: ${e.message}")
            Response(-1, null)
        } finally {
            conn?.disconnect()
        }
    }
}

/** Reads a JSONArray of strings into a Kotlin list. */
fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return (0 until length()).map { optString(it) }
}

/** Iterates a JSONArray of objects. */
fun JSONArray?.objects(): List<JSONObject> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { optJSONObject(it) }
}
