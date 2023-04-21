package com.ltd_tech.core.widgets.pager.anim

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import com.ltd_tech.core.widgets.pager.Direction
import com.ltd_tech.core.widgets.pager.OnPageChangeListener
import com.ltd_tech.core.widgets.pager.PagerAnim
import java.util.*

class PagerScrollAnim(
    width: Int, high: Int,
    marginWidth: Int = 0, marginHeight: Int = 0,
    view: View, listener: OnPageChangeListener
) : PagerAnim(width, high, marginWidth, marginHeight, view, listener) {

    // 滑动追踪的时间
    private val VELOCITY_DURATION = 1000
    private var mVelocity: VelocityTracker? = null

    // 整个Bitmap的背景显示
    private var mBgBitmap: Bitmap? = null

    // 下一个展示的图片
    private var mNextBitmap: Bitmap? = null

    // 被废弃的图片列表
    private var mScrapViews: ArrayDeque<BitmapView>? = null

    // 正在被利用的图片列表
    private val mActiveViews: ArrayList<BitmapView> = ArrayList<BitmapView>(2)

    // 是否处于刷新阶段
    private var isRefresh = true

    // 底部填充
    private var downIt: MutableIterator<BitmapView>? = null

    private var upIt: MutableIterator<BitmapView>? = null

    private var tmpView: BitmapView? = null

    init {
        // 创建两个BitmapView
        initWidget()
    }

    override fun getBgBitmap(): Bitmap? {
        return mBgBitmap
    }

    override fun getNextBitmap(): Bitmap? {
        return mNextBitmap
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.run {
            val x = x.toInt()
            val y = y.toInt()

            // 初始化速度追踪器
            if (mVelocity == null) {
                mVelocity = VelocityTracker.obtain()
            }
            mVelocity?.addMovement(this)
            // 设置触碰点
            setTouchPoint(x.toFloat(), y.toFloat())
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    isRunning = false
                    // 设置起始点
                    setStartPoint(x.toFloat(), y.toFloat())
                    // 停止动画
                    abortAnim()
                }
                MotionEvent.ACTION_MOVE -> {
                    mVelocity?.computeCurrentVelocity(VELOCITY_DURATION)
                    isRunning = true
                    // 进行刷新
                    mView?.postInvalidate()
                }
                MotionEvent.ACTION_UP -> {
                    isRunning = false
                    // 开启动画
                    startAnim()
                    // 删除检测器
                    mVelocity?.recycle()
                    mVelocity = null
                }
                MotionEvent.ACTION_CANCEL -> try {
                    mVelocity?.recycle() // if velocityTracker won't be used should be recycled
                    mVelocity = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                else -> {}
            }
        }
        return true
    }

    override fun draw(canvas: Canvas?) {
        //进行布局
        onLayout()

        canvas?.run {
            //绘制背景
            mBgBitmap?.run {
                drawBitmap(this, 0f, 0f, null)
            }
            //绘制内容
            save()
            //移动位置
            translate(0f, mMarginHeight.toFloat())
            //裁剪显示区域
            clipRect(0, 0, mViewWidth, mViewHeight)
        }

        //绘制Bitmap
        for (i in mActiveViews.indices) {
            tmpView = mActiveViews[i]
            tmpView?.run {
                if (bitmap != null && destRect != null) {
                    canvas?.drawBitmap(bitmap!!, srcRect, destRect!!, null)
                }
            }

        }
        canvas?.restore()
    }

    override fun scrollAnim() {
        if (mScroller?.computeScrollOffset() == true) {
            val x = mScroller?.currX ?: 0
            val y = mScroller?.currY ?: 0
            setTouchPoint(x.toFloat(), y.toFloat())
            if (mScroller?.finalX == x && mScroller?.finalY == y) {
                isRunning = false
            }
            mView?.postInvalidate()
        }
    }

    override fun abortAnim() {
        if (mScroller?.isFinished == false) {
            mScroller?.abortAnimation()
            isRunning = false
        }
    }

    @Synchronized
    override fun startAnim() {
        isRunning = true
        mScroller?.fling(
            0,
            mTouchY.toInt(),
            0,
            mVelocity?.yVelocity?.toInt() ?: 0,
            0,
            0,
            Int.MAX_VALUE * -1,
            Int.MAX_VALUE
        )
    }

    private fun initWidget() {
        mBgBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.RGB_565)

        mScrapViews = ArrayDeque<BitmapView>(2)
        for (i in 0..1) {
            val view = BitmapView()
            view.bitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.RGB_565)
            view.srcRect = Rect(0, 0, mViewWidth, mViewHeight)
            view.destRect = Rect(0, 0, mViewWidth, mViewHeight)
            view.top = 0
            view.bottom = view.bitmap?.height ?: 0
            mScrapViews?.push(view)
        }
        onLayout()
        isRefresh = false
    }

    // 修改布局,填充内容
    private fun onLayout() {
        // 如果还没有开始加载，则从上到下进行绘制
        if (mActiveViews.size == 0) {
            fillDown(0, 0)
            mDirection = Direction.NONE
        } else {
            val offset = (mTouchY - mLastY).toInt()
            // 判断是下滑还是上拉 (下滑)
            if (offset > 0) {
                val topEdge = mActiveViews[0].top
                fillUp(topEdge, offset)
            } else {
                // 底部的距离 = 当前底部的距离 + 滑动的距离 (因为上滑，得到的值肯定是负的)
                val bottomEdge = mActiveViews[mActiveViews.size - 1].bottom
                fillDown(bottomEdge, offset)
            }
        }
    }

    /**
     * 创建View填充底部空白部分
     *
     * @param bottomEdge :当前最后一个View的底部，在整个屏幕上的位置,即相对于屏幕顶部的距离
     * @param offset     :滑动的偏移量
     */
    private fun fillDown(bottomEdge: Int, offset: Int) {
        downIt = mActiveViews.iterator()
        var view: BitmapView?

        // 进行删除
        while (downIt?.hasNext() == true) {
            view = downIt?.next()?.apply {
                top += offset
                bottom += offset
                // 设置允许显示的范围
                destRect?.top = top
                destRect?.bottom = bottom
            }

            // 判断是否越界了
            if ((view?.bottom ?: -1) <= 0) {
                // 添加到废弃的View中
                mScrapViews?.add(view)
                // 从Active中移除
                downIt?.remove()
                // 如果原先是从上加载，现在变成从下加载，则表示取消
                if (mDirection == Direction.UP) {
                    mListener?.pageCancel()
                    mDirection = Direction.NONE
                }
            }
        }

        // 滑动之后的最后一个 View 的距离屏幕顶部上的实际位置
        var realEdge = bottomEdge + offset

        // 进行填充
        while (realEdge < mViewHeight && mActiveViews.size < 2) {
            // 从废弃的Views中获取一个
            view = mScrapViews?.first
            if (view == null) return
            val cancelBitmap = mNextBitmap
            mNextBitmap = view.bitmap
            if (!isRefresh) {
                val hasNext = mListener?.hasNext() ?: false //如果不成功则无法滑动
                // 如果不存在next,则进行还原
                if (!hasNext) {
                    mNextBitmap = cancelBitmap
                    for (activeView in mActiveViews) {
                        activeView.top = 0
                        activeView.bottom = mViewHeight
                        // 设置允许显示的范围
                        activeView.destRect?.top = 0
                        activeView.destRect?.bottom = activeView.bottom
                    }
                    abortAnim()
                    return
                }
            }

            // 如果加载成功，那么就将View从ScrapViews中移除
            mScrapViews?.removeFirst()
            // 添加到存活的Bitmap中
            mActiveViews.add(view)
            mDirection = Direction.DOWN

            // 设置Bitmap的范围
            view.top = realEdge
            view.bottom = realEdge + (view.bitmap?.height ?: 0)
            // 设置允许显示的范围
            view.destRect?.top = view.top
            view.destRect?.bottom = view.bottom
            realEdge += view.bitmap?.height ?: 0
        }
    }

    /**
     * 创建View填充顶部空白部分
     *
     * @param topEdge : 当前第一个View的顶部，到屏幕顶部的距离
     * @param offset  : 滑动的偏移量
     */
    private fun fillUp(topEdge: Int, offset: Int) {
        // 首先进行布局的调整
        upIt = mActiveViews.iterator()
        var view: BitmapView?
        while (upIt?.hasNext() == true) {
            view = upIt?.next()?.apply {
                top += offset
                bottom += offset
                //设置允许显示的范围
                destRect?.top = top
                destRect?.bottom = bottom
            }

            // 判断是否越界了
            if ((view?.top ?: -1) >= mViewHeight) {
                // 添加到废弃的View中
                mScrapViews?.add(view)
                // 从Active中移除
                upIt?.remove()

                // 如果原先是下，现在变成从上加载了，则表示取消加载
                if (mDirection == Direction.DOWN) {
                    mListener?.pageCancel()
                    mDirection = Direction.NONE
                }
            }
        }

        // 滑动之后，第一个 View 的顶部距离屏幕顶部的实际位置。
        var realEdge = topEdge + offset

        // 对布局进行View填充
        while (realEdge > 0 && mActiveViews.size < 2) {
            // 从废弃的Views中获取一个
            view = mScrapViews?.first
            if (view == null) return

            // 判断是否存在上一章节
            val cancelBitmap = mNextBitmap
            mNextBitmap = view.bitmap
            if (!isRefresh) {
                val hasPrev = mListener?.hasPrev() ?: false // 如果不成功则无法滑动
                // 如果不存在next,则进行还原
                if (!hasPrev) {
                    mNextBitmap = cancelBitmap
                    for (activeView in mActiveViews) {
                        activeView.top = 0
                        activeView.bottom = mViewHeight
                        // 设置允许显示的范围
                        activeView.destRect?.top = 0
                        activeView.destRect?.bottom = activeView.bottom
                    }
                    abortAnim()
                    return
                }
            }
            // 如果加载成功，那么就将View从ScrapViews中移除
            mScrapViews!!.removeFirst()
            // 加入到存活的对象中
            mActiveViews.add(0, view)
            mDirection = Direction.UP
            // 设置Bitmap的范围
            view.top = realEdge - (view.bitmap?.height ?: 0)
            view.bottom = realEdge

            // 设置允许显示的范围
            view.destRect?.top = view.top
            view.destRect?.bottom = view.bottom
            realEdge -= (view.bitmap?.height ?: 0)
        }
    }

    /**
     * 重置位移
     */
    fun resetBitmap() {
        isRefresh = true
        // 将所有的Active加入到Scrap中
        for (view in mActiveViews) {
            mScrapViews?.add(view)
        }
        // 清除所有的Active
        mActiveViews.clear()
        // 重新进行布局
        onLayout()
        isRefresh = false
    }


}

class BitmapView {
    var bitmap: Bitmap? = null
    var srcRect: Rect? = null
    var destRect: Rect? = null
    var top = 0
    var bottom = 0
}