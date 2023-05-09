package com.ltd_tech.core.utils

import android.content.Context
import android.content.SharedPreferences
import com.ltd_tech.core.exts.nullTo
import com.ltd_tech.core.exts.todo
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File

/**
 * MMKV key定义
 */
enum class SPType(var key: String, var mode: Int) {

    SP_TYPE_USER("SP_TYPE_USER", MMKV.MULTI_PROCESS_MODE),  //双进程, 用户相关
    DESKTOP("desktop", MMKV.MULTI_PROCESS_MODE),  //双进程, 桌面相关
    SP_TYPE_DEVICE("SP_TYPE_DEVICE", Context.MODE_PRIVATE), // 设备相关

}

/**
 * mmkv 封装类
 */
object TPS {
    // TPS 初始化mmkv状态值
    private var isInit = false

    private val selfDir = application?.applicationContext?.obbDir?.absolutePath + "/mmkv"

    /**
     * 公共mmkv存储空间key
     */
    private const val SPACE = "tpln_mmkv_common_space"

    /**
     * 初始化mmkv
     */
    fun initMMKV() {
//        MMKV.initialize(selfDir)
        //一些 Android 设备（API level 19）在安装/更新 APK 时可能出错, 导致 libmmkv.so 找不到。然后就会遇到 java.lang.UnsatisfiedLinkError 之类的 crash
        MMKV.initialize(application, selfDir)
    }

    /**
     * 根据[key]设置存储值[value]
     */
    fun set(key: String, value: Any) {
        if (!isInit) {
            initMMKV()
        }
        setValue(key = key, value = value)
    }

    /**
     * 指定空间[spType]根据[key]设置存储值[value]
     */
    fun set(spType: SPType, key: String, value: Any) {
        if (!isInit) {
            initMMKV()
        }
        setValue(spType.key, key, value, spType.mode)
    }

    /**
     * 指定空间[spType]根据[key]设置存储值[value]到目录[relativePath]
     */
    fun set(spType: SPType, key: String, value: Any, relativePath: String) {
        if (!isInit) {
            initMMKV()
        }
        setValue(spType.key, key, value, spType.mode, relativePath)
    }

    /**
     * 根据[key]获取默认空间的存储值[defValue]
     */
    fun <T> get(key: String?, defValue: T): T {
        return getValue(key = key, defValue = defValue)
    }

    /**
     * 指定空间[spType]根据[key]获取的存储值[defValue]
     */
    fun <T> get(spType: SPType, key: String?, defValue: T): T {
        return getValue(spType.key, key, defValue, spType.mode)
    }

    /**
     * 从目录[relativePath]指定空间[spType]根据[key]获取的存储值[defValue]
     */
    fun <T> get(spType: SPType, key: String?, relativePath: String, defValue: T): T {
        return getValue(spType.key, key, defValue, spType.mode, relativePath)
    }

    /**
     * 删除[spType]空间中的某个[key]对应的存储
     */
    fun remove(spType: SPType, key: String) {
        getMMKV(spType.key, spType.mode)?.removeValueForKey(key)
    }

    /**
     * 删除默认空间中的某个[key]对应的存储
     */
    fun remove(key: String) {
        getMMKV()?.removeValueForKey(key)
    }

    /**
     * 清除指定空间[spType]的存储,[spType]可以为空
     */
    fun clearKey(spType: SPType? = null) {
        spType.todo({
            getMMKV()
        }, { getMMKV(spType?.key, spType?.mode ?: Context.MODE_PRIVATE) })?.clearAll()
    }

    /**
     * sp数据迁移
     */
    fun migrationDataAsync(context: Context) = GlobalScope.async {
        //遍历枚举类迁移全部老数据至mmkv
        for (spType in SPType.values()) {
            try {
                getSharedPreferences(context, spType)?.let {
                    it.all?.run {
                        if (isNotEmpty()) {
                            getMMKV(spType.key)?.importFromSharedPreferences(it)
                            it.edit().clear().apply()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getSharedPreferences(context: Context, spType: SPType): SharedPreferences? {
        return context.getSharedPreferences(
            spType.key, spType.mode
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getValue(
        mmapId: String? = null,
        key: String?,
        defValue: T,
        mode: Int = Context.MODE_PRIVATE
    ): T {
        if (!isInit) {
            throw IllegalArgumentException("please init mmkv -> TPS initMMKV()")
        }
        val kv = getMMKV(mmapId, mode)
        var result: Any? = null
        when (defValue) {
            is Int -> {
                result = kv?.decodeInt(key, (defValue as Int))
            }

            is Long -> {
                result = kv?.decodeLong(key, (defValue as Long))
            }

            is Float -> {
                result = kv?.decodeFloat(key, (defValue as Float))
            }

            is Double -> {
                result = kv?.decodeDouble(key, (defValue as Double))
            }

            is Boolean -> {
                result = kv?.decodeBool(key, (defValue as Boolean))
            }

            is String -> {
                result = kv?.decodeString(key, defValue as String)
            }

            is ByteArray -> {
                result = kv?.decodeBytes(key)
            }
        }
        result.nullTo {
            result = defValue
            result
        }
        return result as T
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getValue(
        mmapId: String? = null,
        key: String?,
        defValue: T,
        mode: Int = Context.MODE_PRIVATE,
        relativePath: String
    ): T {
        if (!isInit) {
            throw IllegalArgumentException("please init mmkv -> TPS initMMKV()")
        }
        val kv = MMKV.mmkvWithID(mmapId, mode, null, relativePath)
        var result: Any? = null
        when (defValue) {
            is Int -> {
                result = kv?.decodeInt(key, (defValue as Int))
            }

            is Long -> {
                result = kv?.decodeLong(key, (defValue as Long))
            }

            is Float -> {
                result = kv?.decodeFloat(key, (defValue as Float))
            }

            is Double -> {
                result = kv?.decodeDouble(key, (defValue as Double))
            }

            is Boolean -> {
                result = kv?.decodeBool(key, (defValue as Boolean))
            }

            is String -> {
                result = kv?.decodeString(key, defValue as String)
            }

            is ByteArray -> {
                result = kv?.decodeBytes(key)
            }
        }
        result.nullTo {
            result = defValue
            result
        }
        return result as T
    }

    private fun setValue(
        mmapId: String? = null,
        key: String,
        value: Any,
        mode: Int = Context.MODE_PRIVATE
    ) {
        if (!isInit) {
            throw IllegalArgumentException("please init mmkv -> TPS initMMKV()")
        } else {
            val kv = getMMKV(mmapId, mode)
            val result = when (value) {
                is Int -> {
                    kv?.encode(key, value)
                }

                is Long -> {
                    kv?.encode(key, value)
                }

                is Float -> {
                    kv?.encode(key, value)
                }

                is Double -> {
                    kv?.encode(key, value)
                }

                is Boolean -> {
                    kv?.encode(key, value)
                }

                is String -> {
                    kv?.encode(key, value)
                }

                is ByteArray -> {
                    kv?.encode(key, value)
                }

                else -> false
            }
            L.d("tps-set-$result")
        }

    }

    private fun setValue(
        mmapId: String? = null,
        key: String,
        value: Any,
        mode: Int = Context.MODE_PRIVATE,
        relativePath: String
    ) {
        if (!isInit) {
            throw IllegalArgumentException("please init mmkv -> TPS initMMKV()")
        } else {
            val kv = MMKV.mmkvWithID(mmapId, mode, null, relativePath)
            val result = when (value) {
                is Int -> {
                    kv?.encode(key, value)
                }

                is Long -> {
                    kv?.encode(key, value)
                }

                is Float -> {
                    kv?.encode(key, value)
                }

                is Double -> {
                    kv?.encode(key, value)
                }

                is Boolean -> {
                    kv?.encode(key, value)
                }

                is String -> {
                    kv?.encode(key, value)
                }

                is ByteArray -> {
                    kv?.encode(key, value)
                }

                else -> false
            }
            L.d("tps-set-$result")
        }

    }

    /**
     * 根据唯一表示id[mmapId]和类型[mode]获取mmkv对象
     */
    private fun getMMKV(mmapId: String? = null, mode: Int = Context.MODE_PRIVATE) =
        mmapId.todo({ MMKV.defaultMMKV() }, { MMKV.mmkvWithID(mmapId, mode) })


    /**  ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓  公共存储目录操作 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓*/


    /**
     * 设置跨应用间公共参数
     */
    fun putString(key: String, value: String?) {
        putCommonValue(key, value)
    }

    /**
     * 设置跨应用间公共参数
     */
    fun putInt(key: String, value: Int) {
        putCommonValue(key, value)
    }

    /**
     * 设置跨应用间公共参数
     */
    fun putFloat(key: String, value: Float) {
        putCommonValue(key, value)
    }

    /**
     * 设置跨应用间公共参数
     */
    fun putDouble(key: String, value: Double) {
        putCommonValue(key, value)
    }

    /**
     * 设置跨应用间公共参数
     */
    fun putLong(key: String, value: Long) {
        putCommonValue(key, value)
    }

    /**
     * 设置跨应用间公共参数
     */
    fun putBoolean(key: String, value: Boolean) {
        putCommonValue(key, value)
    }


    /**
     * 设置跨应用间公共参数
     */
    fun putByteArray(key: String, value: ByteArray) {
        putCommonValue(key, value)
    }

    /**
     * 存储跨应用间公共mmkv参数
     */
    private fun putCommonValue(key: String, value: Any?) {
        // 切换目录
        MMKV.initialize(application, mmkvSharedDir)
        // 设置公共空间目录
        val mmkv = MMKV.mmkvWithID(SPACE, MMKV.MULTI_PROCESS_MODE)
        // 设置值
        when (value) {
            is String -> {
                mmkv.encode(key, value)
            }

            is Int -> {
                mmkv.encode(key, value)
            }

            is Float -> {
                mmkv.encode(key, value)
            }

            is Double -> {
                mmkv.encode(key, value)
            }

            is Long -> {
                mmkv.encode(key, value)
            }

            is Boolean -> {
                mmkv.encode(key, value)
            }

            is ByteArray -> {
                mmkv.encode(key, value)
            }

            else -> {
                mmkv.encode(key, "")
            }
        }
        // 在切换回原始目录
        MMKV.initialize(application, selfDir)
    }


    /**
     * 获取跨应用间公共参数
     */
    fun getCommonString(key: String): String? =
        getCommonMMKV().decodeString(key).also { MMKV.initialize(selfDir) }

    /**
     * 获取跨应用间公共参数
     */
    fun getCommonInt(key: String): Int =
        getCommonMMKV().decodeInt(key).also { MMKV.initialize(selfDir) }

    /**
     * 获取跨应用间公共参数
     */
    fun getCommonDouble(key: String): Double =
        getCommonMMKV().decodeDouble(key).also { MMKV.initialize(selfDir) }

    /**
     * 获取跨应用间公共参数
     */
    fun getCommonFloat(key: String): Float =
        getCommonMMKV().decodeFloat(key).also { MMKV.initialize(selfDir) }

    /**
     * 获取跨应用间公共参数
     */
    fun getCommonLong(key: String): Long =
        getCommonMMKV().decodeLong(key).also { MMKV.initialize(selfDir) }

    /**
     * 获取跨应用间公共参数
     */
    fun getCommonBoolean(key: String): Boolean =
        getCommonMMKV().decodeBool(key).also { MMKV.initialize(selfDir) }

    /**
     * 获取跨应用间公共参数
     */
    fun getCommonByteArray(key: String): ByteArray? =
        getCommonMMKV().decodeBytes(key).also { MMKV.initialize(selfDir) }


    /**
     * 获取跨应用间公共的mmkv文件内容
     */
    private fun getCommonMMKV(): MMKV {
        try {
            // 删除已经存在的文件并拷贝
            val destFile = File("$selfDir/$SPACE")
            val destCrcFile = File("$selfDir/$SPACE.crc")
            if (destFile.exists()) {
                destFile.delete()
            }
            if (destCrcFile.exists()) {
                destCrcFile.delete()
            }
            // 获取公共文件
            val file = File("${mmkvSharedDir}/$SPACE")
            val fileCrc = File("${mmkvSharedDir}/$SPACE.crc")
            val dir = File(selfDir)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            copyFileUsingFileStreams(file, destFile)
            copyFileUsingFileStreams(fileCrc, destCrcFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return MMKV.mmkvWithID(SPACE, MMKV.MULTI_PROCESS_MODE)
    }

    /** ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ */

}