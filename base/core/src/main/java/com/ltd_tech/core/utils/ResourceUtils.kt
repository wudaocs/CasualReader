package com.ltd_tech.core.utils

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

object ResourceUtils {

    private fun getResource(): Resources? = application?.applicationContext?.resources

    fun getDimens(@DimenRes dimensId: Int): Int? {
        return getResource()?.getDimension(dimensId)?.toInt()
    }

    fun getColor(@ColorRes colorId: Int): Int? {
        return getResource()?.getColor(colorId)
    }

    fun getDrawable(drawableName: String): Int {
        return getResource()?.getIdentifier(drawableName, "drawable", appPackageName) ?: -1
    }

    fun getScreenWidth(): Int? {
        return getResource()?.displayMetrics?.widthPixels
    }

    fun getString(@StringRes strId: Int): String {
        return getResource()?.getString(strId) ?: ""
    }

    fun getStringArray(@ArrayRes arrayId: Int): Array<String>? {
        return getResource()?.getStringArray(arrayId)
    }

    fun getIntArray(@ArrayRes arrayId: Int): IntArray? {
        return getResource()?.getIntArray(arrayId)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun getDrawable(@DrawableRes resId: Int): Drawable? {
        return getResource()?.getDrawable(resId)
    }

    fun stringFormat(@StringRes str: Int, value: Float): String? {
        getString(str).run {
            return String.format(this, value)
        }
    }

    fun stringFormat(@StringRes str: Int, value: Int): String? {
        getString(str).run {
            return String.format(this, value)
        }
    }

    fun stringFormat(@StringRes str: Int, value1: Int? = null, value2: Int? = null): String? {
        getString(str).run {
            return String.format(this, value1, value2)
        }
    }

    fun stringFormat(@StringRes str: Int, value: String?): String? {
        getString(str).run {
            return String.format(this, value)
        }
    }

    fun stringFormat(@StringRes str: Int, value1: String? = null, value2: String? = null): String? {
        getString(str).run {
            return String.format(this, value1, value2)
        }
    }

    fun stringFormat(
        @StringRes str: Int,
        value1: String?,
        value2: String?,
        value3: String?,
        value4: String?
    ): String? {
        getString(str).run {
            return String.format(this, value1, value2, value3, value4)
        }
    }

}