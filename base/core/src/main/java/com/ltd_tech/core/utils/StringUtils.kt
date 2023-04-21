package com.ltd_tech.core.utils


object StringUtils {

    //繁簡轉換
    fun convertCC(input: String?): String? {
//        var currentConversionType: ConversionType = ConversionType.S2TWP
//        val convertType: Int = SharedPreUtils.getInstance().getInt(SHARED_READ_CONVERT_TYPE, 0)
//        if (input.length == 0) return ""
//        when (convertType) {
//            1 -> currentConversionType = ConversionType.TW2SP
//            2 -> currentConversionType = ConversionType.S2HK
//            3 -> currentConversionType = ConversionType.S2T
//            4 -> currentConversionType = ConversionType.S2TW
//            5 -> currentConversionType = ConversionType.S2TWP
//            6 -> currentConversionType = ConversionType.T2HK
//            7 -> currentConversionType = ConversionType.T2S
//            8 -> currentConversionType = ConversionType.T2TW
//            9 -> currentConversionType = ConversionType.TW2S
//            10 -> currentConversionType = ConversionType.HK2S
//        }
//        return if (convertType != 0) ChineseConverter.convert(
//            input,
//            currentConversionType,
//            context
//        ) else input
        return input
    }

    /**
     * 将文本中的半角字符，转换成全角字符
     */
    fun halfToFull(input: String?): String {
        val c = input?.toCharArray() ?: "".toCharArray()
        for (i in c.indices) {
            //半角空格
            if (c[i].code == 32) {
                c[i] = 12288.toChar()
                continue
            }
            //根据实际情况，过滤不需要转换的符号
            //if (c[i] == 46) //半角点号，不转换
            // continue;
            //其他符号都转换为全角
            if (c[i].code in 33..126) {
                c[i] = (c[i].code + 65248).toChar()
            }
        }
        return String(c)
    }
}