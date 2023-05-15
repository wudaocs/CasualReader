package com.ltd_tech.core.utils.gson

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import kotlin.Throws
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.internal.bind.ObjectTypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.ArrayList

class GsonTypeAdapter internal constructor(private val gson: Gson) : TypeAdapter<Any?>() {
    @Throws(IOException::class)
    override fun read(jsonReader: JsonReader): Any? {
        return when (jsonReader.peek()) {
            JsonToken.BEGIN_ARRAY -> {
                val list: MutableList<Any?> = ArrayList()
                jsonReader.beginArray()
                while (jsonReader.hasNext()) {
                    list.add(read(jsonReader))
                }
                jsonReader.endArray()
                list
            }
            JsonToken.BEGIN_OBJECT -> {
                val map: MutableMap<String, Any?> = LinkedTreeMap()
                jsonReader.beginObject()
                while (jsonReader.hasNext()) {
                    map[jsonReader.nextName()] = read(jsonReader)
                }
                jsonReader.endObject()
                map
            }
            JsonToken.STRING -> jsonReader.nextString()
            JsonToken.NUMBER -> {
                // 自定义的number类型处理
                val s = jsonReader.nextString()
                if (s.contains(".")) {
                    java.lang.Double.valueOf(s)
                } else {
                    try {
                        Integer.valueOf(s)
                    } catch (e: Exception) {
                        java.lang.Long.valueOf(s)
                    }
                }
            }
            JsonToken.BOOLEAN -> jsonReader.nextBoolean()
            JsonToken.NULL -> {
                jsonReader.nextNull()
                null
            }
            else -> throw IllegalStateException()
        }
    }

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Any?) {
        if (value == null) {
            out.nullValue()
            return
        }
        val typeAdapter = gson.getAdapter(value.javaClass) as TypeAdapter<Any>
        if (typeAdapter is ObjectTypeAdapter) {
            out.beginObject()
            out.endObject()
            return
        }
        typeAdapter.write(out, value)
    }

    companion object {
        val FACTORY: TypeAdapterFactory = object : TypeAdapterFactory {
            override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
                return if (type.rawType == Any::class.java) {
                    GsonTypeAdapter(gson) as TypeAdapter<T>
                } else null
            }
        }
    }
}