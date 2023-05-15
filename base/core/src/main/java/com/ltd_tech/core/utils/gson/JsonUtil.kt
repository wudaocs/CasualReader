package com.ltd_tech.core.utils.gson

import android.text.TextUtils
import com.google.gson.*
import com.google.gson.internal.bind.ObjectTypeAdapter
import com.google.gson.reflect.TypeToken
import com.ltd_tech.core.exts.notnullTo
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.*

/**
 * Description : json 转换工具类
 * Created by YW on 2019-12-26.
 * Email：1809267944@qq.com
 */
object JsonUtil {
    private var mGson: Gson? = Gson()
    var mMapGson: Gson? = getMapGson()

    fun getGson(): Gson? {
        if (mGson == null) mGson = Gson()
        return mGson
    }


    private fun getMapGson(): Gson? {
        val gson = GsonBuilder().create()
        try {
            val factories = Gson::class.java.getDeclaredField("factories")
            factories.isAccessible = true
            val o = factories[gson]
            val declaredClasses = Collections::class.java.declaredClasses
            for (c in declaredClasses) {
                if ("java.util.Collections\$UnmodifiableList" == c.name) {
                    val listField = c.getDeclaredField("list")
                    listField.isAccessible = true
                    val list = listField[o] as MutableList<TypeAdapterFactory>
                    val i = list.indexOf(ObjectTypeAdapter.FACTORY)
                    list[i] = GsonTypeAdapter.FACTORY
                    break
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return gson
    }

    /**
     * 对象转换成String
     *
     * @param src 待转换的对象
     * @return json string
     */
    fun toJson(src: Any?): String {
        return if (src == null) {
            ""
        } else getGson()!!.toJson(src)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> fromJson(json: String, cls: Class<T>): T? {
        return try {
            if (TextUtils.isEmpty(json)) {
                null
            } else {
                getGson()?.fromJson(json, cls)
            }
        } catch (e: JsonSyntaxException) {
            e.stackTrace
            null
        }
    }

    /**
     * json string 转换成object
     * @param json 待转换的json string
     * @param typeOfT 需要转换的类型
     * @param <T> 需要转换的反省
     * @return 返回的转换结果
     */
    fun <T> fromJsonT(json: String, typeOfT: Type, t: T): T? {
        return try {
            if (TextUtils.isEmpty(json)) {
                null
            } else getGson()?.fromJson<T>(json, typeOfT)
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    inline fun <reified T> jsonToT(json: String): T? {
        return getGson()?.fromJson(json, object : TypeToken<T>() {}.type)
    }

    inline fun <reified T> fromJson(json: String): T? {
        return mMapGson?.fromJson(json, object : TypeToken<T>() {}.type)
    }

    fun <T> fromJsonMap(json: String, typeOfT: Type): T? {
        return mMapGson?.fromJson(json, typeOfT)
    }

//    fun <T> T.serializeToMap(): Map<String, Any> {
//        return convert()
//    }

//    inline fun <reified T> Map<String, Any>.toDataClass(): T {
//        return convert()
//    }

    //convert an object of type I to type O
    inline fun <reified O> convert(json: String): O? {
        return mMapGson?.fromJson(json, object : TypeToken<O>() {}.type)
    }

    @Suppress("DEPRECATION")
    fun <T> fromJsonList(json: String, cls: Class<T>): MutableList<T>? {
        try {
            return if (TextUtils.isEmpty(json)) {
                null
            } else {
                val list = ArrayList<T>()
                val array = JsonParser().parse(json).asJsonArray
                if (null != array && array.size() > 0) {
                    val iterator = array.iterator()
                    while (iterator.hasNext()) {
                        val elem = iterator.next() as JsonElement
                        list.add(getGson()!!.fromJson(elem, cls)!!)
                    }
                }
                list
            }
        } catch (e: JsonSyntaxException) {
            return null
        }
    }

    /**
     * 判断是否为json格式数据
     */
    fun isJson(str: String): Boolean {
        return !TextUtils.isEmpty(str) && ((str.startsWith("{") && str.endsWith("}")) || (str.startsWith(
            "["
        ) && str.endsWith("]")))
    }

    /**
     * 格式化json数据
     */
    fun formatJson(jsonStr: String?): String {
        val sb = StringBuilder()
        jsonStr?.notnullTo {
            var last: Char
            var current = '\u0000'
            var index = 0
            var isInQuotationMarks = false
            for (element in jsonStr) {
                last = current
                current = element
                when (current) {
                    '"' -> {
                        if (last != '\\') {
                            isInQuotationMarks = !isInQuotationMarks
                        }
                        sb.append(current)
                    }
                    '{', '[' -> {
                        sb.append(current)
                        if (!isInQuotationMarks) {
                            sb.append('\n')
                            index++
                            addIndentBlank(sb, index)
                        }
                    }
                    '}', ']' -> {
                        if (!isInQuotationMarks) {
                            sb.append('\n')
                            index--
                            addIndentBlank(sb, index)
                        }
                        sb.append(current)
                    }
                    ',' -> {
                        sb.append(current)
                        if (last != '\\' && !isInQuotationMarks) {
                            sb.append('\n')
                            addIndentBlank(sb, index)
                        }
                    }
                    else -> sb.append(current)
                }
            }
        }
        return sb.toString()
    }

    /**
     * 添加空行
     */
    private fun addIndentBlank(sb: StringBuilder, indent: Int) {
        for (i in 0 until indent) {
            sb.append('\t')
        }
    }

    /**
     * 转换成hashMap
     * @param isCleanNull 是否清除空对象
     */
    @JvmOverloads
    fun toHashMap(data: Any?, isCleanNull : Boolean = false): HashMap<String, String> {
        if (data == null) {
            return hashMapOf()
        }
        return try {
            val jsonObject = JSONObject(toJson(data))
            val keys = jsonObject.keys()
            val map: HashMap<String, String> = HashMap()
            for (key in keys) {
                if (TextUtils.isEmpty(key) || key == "null") {
                    if (isCleanNull){
                        continue
                    }
                    map[key] = ""
                } else {
                    map[key] = jsonObject[key].toString()
                }
            }
            map
        } catch (e: Exception) {
            e.printStackTrace()
            hashMapOf()
        }
    }
}