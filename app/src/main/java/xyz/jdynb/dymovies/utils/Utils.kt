package xyz.jdynb.dymovies.utils

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Base64
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.drake.net.request.BodyRequest
import com.drake.net.request.MediaConst
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.RequestBody.Companion.toRequestBody
import xyz.jdynb.dymovies.DyMoviesApplication
import kotlin.reflect.typeOf

/**
 * 启动一个 Activity
 */
inline fun <reified T> startActivity(vararg args: Pair<String, Any>) {
  val context = DyMoviesApplication.context
  context.startActivity(Intent(context, T::class.java).apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    args.forEach {
      when (val second = it.second) {
        is String -> putExtra(it.first, second)
        is Int -> putExtra(it.first, second)
        is Float -> putExtra(it.first, second)
        is Parcelable -> putExtra(it.first, second)
      }
    }
  })
}

/**
 *  显示一个原生土司
 */
fun String?.showToast(duration: Int = Toast.LENGTH_SHORT) {
  if (this == null) return
  Toast.makeText(DyMoviesApplication.context, this, duration).show()
}

/**
 * 通过字节数获取对应单位
 * @param bytes 字节大小
 */
fun formatBytes(bytes: Long): String {
  val units = arrayOf("B", "KB", "MB", "GB", "TB")
  if (bytes == 0L) return "0 B"
  var currentBytes = bytes.toDouble()
  var unitIndex = 0

  while (currentBytes >= 1024 && unitIndex < units.size - 1) {
    currentBytes /= 1024.0
    unitIndex++
  }

  return "%.2f %s".format(currentBytes, units[unitIndex])
}

/**
 * 判断字符串是不是一个网址
 */
fun String.isUrl(): Boolean {
  if (this.isEmpty()) {
    return false
  }
  val url = this.trim()
  return url.matches("^(http|https)://.+".toRegex())
}

/**
 * 字符串转 16 进制
 */
fun String.toHexString(): String {
  return this.map {
    "%02X".format(it.code)
  }.joinToString("").lowercase()
}

/**
 * BASE64 转 16 进制字符串
 */
fun String.base64ToHex(): String {
  // 将Base64字符串解码为字节数组
  val decodedBytes: ByteArray = Base64.decode(this, Base64.DEFAULT)
  // 将字节数组转换为十六进制字符串
  val hexString = StringBuilder()
  for (b in decodedBytes) {
    // 将每个字节转换为两位十六进制数
    val hex = Integer.toHexString(0xff and b.toInt())
    if (hex.length == 1) {
      hexString.append('0') // 如果是一位，则在前面补0
    }
    hexString.append(hex)
  }
  return hexString.toString()
}

/*fun BodyRequest.gson(vararg body: Pair<String, Any?>) {
  this.body = Gson().toJson(body.toMap()).toRequestBody(MediaConst.JSON)
}*/

/**
 * Bundle 中添加序列化参数
 */
inline fun <reified T : @Serializable Any> Bundle.putSerializable(key: String, value: T) {
  val jsonString = Json.encodeToString(value)
  putString(key, jsonString)
}

inline fun <reified T : @Serializable Any> Intent.putSerializable(key: String, value: T) {
  putExtra(key, Json.encodeToString(value))
}

/**
 * Bundle 中获取序列化参数
 */
inline fun <reified T : @Serializable Any> Bundle.getSerializableForKey(key: String): T? {
  val jsonString = getString(key) ?: return null
  return Json.decodeFromString<T>(jsonString)
}

/**
 * Fragment 中添加序列化参数
 */
inline fun <reified T : @Serializable Any> Fragment.setSerializableArguments(
  key: String,
  value: T
) {
  arguments = (arguments ?: Bundle()).apply {
    putSerializable(key, value)
  }
}

/**
 * Fragment 中获取序列化参数
 */
inline fun <reified T : @Serializable Any> Fragment.getSerializableArguments(key: String): T? {
  return arguments?.getSerializableForKey(key)
}

inline fun <reified T : @Serializable Any> Activity.setSerializableArguments(
  key: String,
  value: T
) {
  intent.extras?.putSerializable(key, value)
}

inline fun <reified T : @Serializable Any> Activity.getSerializableArguments(key: String): T? {
  return intent.extras?.getSerializableForKey(key)
}

val json = Json {
  // 序列化默认值
  encodeDefaults = true
}

inline fun <reified T> BodyRequest.json(body: T) {
  this.body = json.encodeToString(body)
    .toRequestBody(MediaConst.JSON)
}