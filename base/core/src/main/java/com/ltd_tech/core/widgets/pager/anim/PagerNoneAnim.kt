package com.ltd_tech.core.widgets.pager.anim

import android.graphics.Canvas
import android.view.View
import com.ltd_tech.core.widgets.pager.OnPageChangeListener

class PagerNoneAnim(
    width: Int, high: Int,
    view: View, listener: OnPageChangeListener
) : PagerHorizonAnim(width, high, 0,0, view, listener) {


    override fun drawStatic(canvas: Canvas?) {
        if (isCancel) {
            mCurBitmap?.run {
                canvas?.drawBitmap(this, 0f, 0f, null)
            }
        } else {
            mNextBitmap?.run {
                canvas?.drawBitmap(this, 0f, 0f, null)
            }
        }
    }

    override fun drawMove(canvas: Canvas?) {
        if (isCancel) {
            mCurBitmap?.run {
                canvas?.drawBitmap(this, 0f, 0f, null)
            }
        } else {
            mNextBitmap?.run {
                canvas?.drawBitmap(this, 0f, 0f, null)
            }
        }
    }

}