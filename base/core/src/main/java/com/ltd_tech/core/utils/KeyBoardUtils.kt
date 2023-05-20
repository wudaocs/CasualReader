package com.ltd_tech.core.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object KeyBoardUtils {
    /**
     * 打开软键盘
     *
     * @param view
     */
    fun openKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.RESULT_UNCHANGED_SHOWN)
    }

    /**
     * 打开软键盘
     *
     * @param mEditText
     * @return
     */
    fun isOpen(mEditText: EditText): Boolean {
        val imm = mEditText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.isActive
    }

    /**
     * 关闭软键盘
     *
     * @param view
     */
    fun closeKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * 设置EditText可输入和不可输入状态
     *
     * @param view
     * @param editable
     */
    fun editTextAble(view: View, editable: Boolean) {
        if (!editable) { // disable editing password
            view.isFocusable = false
            view.isFocusableInTouchMode = false // user touches widget on phone with touch screen
            view.isClickable = false // user navigates with wheel and selects widget
        } else { // enable editing of password
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            view.isClickable = true
        }
    }
}