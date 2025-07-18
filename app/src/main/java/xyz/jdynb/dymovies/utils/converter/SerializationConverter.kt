@file:Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")

package xyz.jdynb.dymovies.utils.converter

import com.drake.net.NetConfig
import com.drake.net.convert.NetConverter
import com.drake.net.exception.ConvertException
import com.drake.net.exception.RequestParamsException
import com.drake.net.exception.ResponseException
import com.drake.net.exception.ServerResponseException
import com.drake.net.request.kType
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import xyz.jdynb.dymovies.model.result.SimpleResult
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class SerializationConverter(
  val success: String = "200",
  val code: String = "code",
  val message: String = "msg",
) : NetConverter {

  companion object {
    val jsonDecoder = Json {
      ignoreUnknownKeys = true // 忽略未知属性名
      ignoreUnknownKeys = true // 数据类可以不用声明Json的所有字段
      coerceInputValues = true // 如果Json字段是Null则使用数据类字段默认值
    }
  }

  override fun <R> onConvert(succeed: Type, response: Response): R? {
    try {
      return NetConverter.onConvert<R>(succeed, response)
    } catch (e: ConvertException) {
      val code = response.code
      when {
        code in 200..299 -> { // 请求成功
          val bodyString = response.body?.string() ?: return null
          val kType = response.request.kType
            ?: throw ConvertException(response, "Request does not contain KType")
          return try {
            val json = JSONObject(bodyString) // 获取JSON中后端定义的错误码和错误信息
            val srvCode = json.getString(this.code)
            if (srvCode == success) { // 对比后端自定义错误码
              if (!json.has("data")) {
                // 当 data 为空时，返回的只是简单的状态信息，使用 SimpleResult 进行解析
                if (kType == typeOf<SimpleResult>()) {
                  return bodyString.parseBody<R>(kType)
                }
                return null
              }
              json.getString("data").parseBody<R>(kType)
            } else { // 错误码匹配失败, 开始写入错误异常
              val errorMessage = json.optString(message, NetConfig.app.getString(com.drake.net.R.string.no_error_message))
              throw ResponseException(response, errorMessage, tag = srvCode) // 将业务错误码作为tag传递
            }
          } catch (e: JSONException) { // 固定格式JSON分析失败直接解析JSON
            bodyString.parseBody<R>(kType)
          }
        }
        code in 400..499 -> throw RequestParamsException(response, code.toString()) // 请求参数错误
        code >= 500 -> throw ServerResponseException(response, code.toString()) // 服务器异常错误
        else -> throw ConvertException(response, message = "Http status code not within range")
      }
    }
  }

  fun <R> String.parseBody(succeed: KType): R? {
    return jsonDecoder.decodeFromString(Json.serializersModule.serializer(succeed), this) as R
  }
}