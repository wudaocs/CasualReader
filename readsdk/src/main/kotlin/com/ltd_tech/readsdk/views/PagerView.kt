package com.ltd_tech.core.widgets.pager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.ltd_tech.core.widgets.pager.anim.PagerHorizonAnim
import com.ltd_tech.core.widgets.pager.anim.PagerNoneAnim
import com.ltd_tech.core.widgets.pager.anim.PagerScrollAnim
import com.ltd_tech.core.widgets.pager.anim.PagerTurnPageAnim
import com.ltd_tech.readsdk.entities.BookEntity
import com.ltd_tech.readsdk.loader.LocalPageLoader
import com.ltd_tech.readsdk.loader.NetPageLoader
import com.ltd_tech.readsdk.loader.PageLoader
import kotlin.math.abs

/**
 * 自定义 pagerView
 */
class PagerView @JvmOverloads constructor(
    context: Context,
    attributes: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attributes, defStyleAttr), OnPageChangeListener {
    // 当前View的宽
    private var mViewWidth = 0

    // 当前View的高
    private var mViewHeight = 0


    private var mStartX = 0
    private var mStartY = 0
    private var isMove = false

    // 初始化参数
    private var mBgColor = 0

    // 是否允许点击
    private var canTouch = true

    // 唤醒菜单的区域
    private var mCenterRect: RectF? = null
    private var isPrepare = false

    private var mPageAnim: PagerAnim? = null

    private var mPageMode: PageMode = PageMode.TURN_PAGE

    //点击监听
    private var mTouchListener: TouchListener? = null

    //内容加载器
    private var mPageLoader: PageLoader? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewWidth = w
        mViewHeight = h

        isPrepare = true
        mPageLoader?.prepareDisplay(w, h)
    }

    /**
     * 判断是否存在上一页
     */
    override fun hasPrev(): Boolean {
        mTouchListener?.prePage()
        return mPageLoader?.prev() ?: false
    }

    /**
     * 判断是否下一页存在
     */
    override fun hasNext(): Boolean {
        mTouchListener?.nextPage()
        return mPageLoader?.next() ?: false
    }

    override fun pageCancel() {
        mTouchListener?.cancel()
        mPageLoader?.pageCancel()
    }

    private fun startPageAnim(direction: Direction) {
        if (mTouchListener == null) return
        //是否正在执行动画
        abortAnimation()
        if (direction == Direction.NEXT) {
            val x = mViewWidth
            val y = mViewHeight
            //初始化动画
            mPageAnim?.setStartPoint(x.toFloat(), y.toFloat())
            //设置点击点
            mPageAnim?.setTouchPoint(x.toFloat(), y.toFloat())
            //设置方向
            val hasNext: Boolean = hasNext()
            mPageAnim?.setDirection(direction)
            if (!hasNext) {
                return
            }
        } else {
            val x = 0
            val y = mViewHeight
            //初始化动画
            mPageAnim?.setStartPoint(x.toFloat(), y.toFloat())
            //设置点击点
            mPageAnim?.setTouchPoint(x.toFloat(), y.toFloat())
            mPageAnim?.setDirection(direction)
            //设置方向方向
            val hashPrev: Boolean = hasPrev()
            if (!hashPrev) {
                return
            }
        }
        mPageAnim?.startAnim()
        this.postInvalidate()
    }

    //如果滑动状态没有停止就取消状态，重新设置Anim的触碰点
    private fun abortAnimation() {
        mPageAnim?.abortAnim()
    }

    override fun onDraw(canvas: Canvas) {
        //绘制背景
        canvas.drawColor(mBgColor)
        //绘制动画
        mPageAnim?.draw(canvas)
    }

    override fun computeScroll() {
        //进行滑动
        mPageAnim?.scrollAnim()
        super.computeScroll()
    }

    fun isRunning(): Boolean = mPageAnim?.isRunning ?: false

    fun isPrepare(): Boolean = isPrepare

    fun setTouchListener(mTouchListener: TouchListener?) {
        this.mTouchListener = mTouchListener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mPageAnim?.abortAnim()
        mPageAnim?.clear()
        mPageLoader = null
        mPageAnim = null
    }

    /**
     * 获取 PageLoader
     */
    fun getPageLoader(bookEntity: BookEntity): PageLoader? {
        // 判是否已经存在
        if (mPageLoader != null) {
            return mPageLoader
        }
        // 根据书籍类型，获取具体的加载器
        mPageLoader = if (bookEntity.isLocal()) {
            LocalPageLoader(this, bookEntity)
        } else {
            NetPageLoader(this, bookEntity)
        }
        // 判断是否 PageView 已经初始化完成
        if (mViewWidth != 0 || mViewHeight != 0) {
            // 初始化 PageLoader 的屏幕大小
            mPageLoader?.prepareDisplay(mViewWidth, mViewHeight)
        }
        return mPageLoader
    }


    /**
     * 绘制当前页。
     */
    fun drawCurPage(isUpdate: Boolean) {
        if (!isPrepare) return
        if (!isUpdate) {
            if (mPageAnim is PagerScrollAnim) {
                (mPageAnim as PagerScrollAnim).resetBitmap()
            }
        }
        mPageLoader?.drawPage(mPageAnim?.getNextBitmap(), isUpdate)
    }

    fun drawNextPage() {
        if (!isPrepare) return
        if (mPageAnim is PagerHorizonAnim) {
            (mPageAnim as PagerHorizonAnim).changePage()
        }
        mPageLoader?.drawPage(mPageAnim?.getNextBitmap(), false)
    }

    fun autoPrevPage(): Boolean {
        //滚动暂时不支持自动翻页
        return if (mPageAnim is PagerScrollAnim) {
            false
        } else {
            startPageAnim(Direction.PRE)
            true
        }
    }

    fun autoNextPage(): Boolean {
        return if (mPageAnim is PagerScrollAnim) {
            false
        } else {
            startPageAnim(Direction.NEXT)
            true
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        event?.run {
            if (!canTouch && action != MotionEvent.ACTION_DOWN) return true
            val x = x.toInt()
            val y = y.toInt()
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    mStartX = x
                    mStartY = y
                    isMove = false
                    canTouch = mTouchListener?.onTouch() ?: false
                    mPageAnim?.onTouchEvent(event)
                }
                MotionEvent.ACTION_MOVE -> {
                    // 判断是否大于最小滑动值。
                    val slop = ViewConfiguration.get(context).scaledTouchSlop
                    if (!isMove) {
                        isMove = abs(mStartX - event.x) > slop || abs(mStartY - event.y) > slop
                    }
                    // 如果滑动了，则进行翻页。
                    if (isMove) {
                        mPageAnim?.onTouchEvent(event)
                    } else {
                        Log.v("onTouchEvent", "pagerView onTouchEvent 非滑动日志")
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (!isMove) {
                        //设置中间区域范围
                        if (mCenterRect == null) {
                            mCenterRect = RectF(
                                (mViewWidth / 5).toFloat(), (mViewHeight / 3).toFloat(),
                                (mViewWidth * 4 / 5).toFloat(), (mViewHeight * 2 / 3).toFloat()
                            )
                        }

                        //是否点击了中间
                        if (mCenterRect?.contains(x.toFloat(), y.toFloat()) == true) {
                            mTouchListener?.center()
                            return true
                        }
                    }
                    mPageAnim?.onTouchEvent(event)
                }
                else -> {}
            }
        }
        return true
    }

    //设置翻页的模式
    fun setPageMode(pageMode: PageMode?) {
        if (pageMode != null) {
            mPageMode = pageMode
            //视图未初始化的时候，禁止调用
            if (mViewWidth == 0 || mViewHeight == 0) return
            mPageAnim = when (mPageMode) {
                PageMode.TURN_PAGE -> PagerTurnPageAnim(mViewWidth, mViewHeight, this, this)
                PageMode.NONE -> PagerNoneAnim(mViewWidth, mViewHeight, this, this)
                PageMode.SCROLL -> PagerScrollAnim(
                    mViewWidth, mViewHeight, 0,
                    mPageLoader?.getMarginHeight() ?: 0, this, this
                )
            }
        }
    }

    fun setBgColor(color: Int) {
        mBgColor = color
    }

    fun getNextBitmap(): Bitmap? {
        return mPageAnim?.getNextBitmap()
    }

    fun getBgBitmap(): Bitmap? {
        return  mPageAnim?.getBgBitmap()
    }

}

interface TouchListener {
    fun onTouch(): Boolean
    fun center()
    fun prePage()
    fun nextPage()
    fun cancel()
}