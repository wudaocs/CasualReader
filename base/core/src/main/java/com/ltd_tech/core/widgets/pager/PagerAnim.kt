package com.ltd_tech.core.widgets.pager

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Scroller
import java.util.*

/**
 * 翻页动画
 */
abstract class PagerAnim(
    width: Int, high: Int,
    marginWidth: Int = 0, marginHeight: Int = 0,
    view: View, listener: OnPageChangeListener
) {

    //正在使用的View
    protected var mView: View? = null

    //滑动装置
    protected var mScroller: Scroller? = null

    //监听器
    protected var mListener: OnPageChangeListener? = null

    //移动方向
    protected var mDirection: Direction = Direction.NONE

    var isRunning = false

    //屏幕的尺寸
    protected var mScreenWidth = 0
    protected var mScreenHeight = 0

    //屏幕的间距
    protected var mMarginWidth = 0
    protected var mMarginHeight = 0

    //视图的尺寸
    protected var mViewWidth = 0
    protected var mViewHeight = 0

    //起始点
    protected var mStartX = 0f
    protected var mStartY = 0f

    //触碰点
    protected var mTouchX = 0f
    protected var mTouchY = 0f

    //上一个触碰点
    protected var mLastX = 0f
    protected var mLastY = 0f

    init {
        mScreenWidth = width
        mScreenHeight = high

        mMarginWidth = marginWidth
        mMarginHeight = marginHeight

        mViewWidth = mScreenWidth - mMarginWidth * 2
        mViewHeight = mScreenHeight - mMarginHeight * 2

        mView = view
        mListener = listener

        mScroller = Scroller(mView?.context, LinearInterpolator())
    }

    /**
     * 开启翻页动画
     */
    open fun startAnim() {
        if (isRunning) {
            return
        }
        isRunning = true
    }

    open fun setStartPoint(x: Float, y: Float) {
        mStartX = x
        mStartY = y
        mLastX = mStartX
        mLastY = mStartY
    }

    open fun setTouchPoint(x: Float, y: Float) {
        mLastX = mTouchX
        mLastY = mTouchY
        mTouchX = x
        mTouchY = y
    }

    open fun setDirection(direction: Direction) {
        mDirection = direction
    }

    /**
     * 获取背景板
     */
    abstract fun getBgBitmap(): Bitmap?

    /**
     * 获取内容显示版面
     */
    abstract fun getNextBitmap(): Bitmap?

    /**
     * 点击事件的处理
     * @param event
     */
    abstract fun onTouchEvent(event: MotionEvent?): Boolean

    /**
     * 绘制图形
     * @param canvas
     */
    abstract fun draw(canvas: Canvas?)

    /**
     * 滚动动画
     * 必须放在computeScroll()方法中执行
     */
    abstract fun scrollAnim()

    /**
     * 取消动画
     */
    abstract fun abortAnim()

    fun clear() {
        mView = null
    }



}

/**
 * 方向
 */
enum class Direction(val isHorizontal: Boolean) {
    NONE(true), NEXT(true), PRE(true), UP(false), DOWN(false);
}

/**
 * 页面切换回调
 */
interface OnPageChangeListener {
    fun hasPrev(): Boolean
    operator fun hasNext(): Boolean
    fun pageCancel()
}