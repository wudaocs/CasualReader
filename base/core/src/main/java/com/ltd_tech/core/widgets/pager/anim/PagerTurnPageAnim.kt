package com.ltd_tech.core.widgets.pager.anim

import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import com.ltd_tech.core.widgets.pager.Direction
import com.ltd_tech.core.widgets.pager.OnPageChangeListener
import kotlin.math.*

class PagerTurnPageAnim(
    width: Int, high: Int,
    view: View, listener: OnPageChangeListener
) : PagerHorizonAnim(width, high, 0, 0, view, listener) {

    private var mCornerX = 1 // 拖拽点对应的页脚

    private var mCornerY = 1
    private var mPath0: Path? = null
    private var mPath1: Path? = null

    private val mBezierStart1 = PointF() // 贝塞尔曲线起始点

    private val mBezierControl1 = PointF() // 贝塞尔曲线控制点

    private val mBeziervertex1 = PointF() // 贝塞尔曲线顶点

    private var mBezierEnd1 = PointF() // 贝塞尔曲线结束点


    private val mBezierStart2 = PointF() // 另一条贝塞尔曲线

    private val mBezierControl2 = PointF()
    private val mBeziervertex2 = PointF()
    private var mBezierEnd2 = PointF()

    private var mMiddleX = 0f
    private var mMiddleY = 0f
    private var mDegrees = 0f
    private var mTouchToCornerDis = 0f
    private var mColorMatrixFilter: ColorMatrixColorFilter? = null
    private var mMatrix: Matrix? = null
    private val mMatrixArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 1.0f)

    // 是否属于右上左下
    private var mIsRTandLB = false
    private var mMaxLength = 0f

    // 有阴影的GradientDrawable
    private var mBackShadowDrawableLR: GradientDrawable? = null
    private var mBackShadowDrawableRL: GradientDrawable? = null
    private var mFolderShadowDrawableLR: GradientDrawable? = null
    private var mFolderShadowDrawableRL: GradientDrawable? = null

    private var mFrontShadowDrawableHBT: GradientDrawable? = null
    private var mFrontShadowDrawableHTB: GradientDrawable? = null
    private var mFrontShadowDrawableVLR: GradientDrawable? = null
    private var mFrontShadowDrawableVRL: GradientDrawable? = null

    private var mPaint: Paint? = null

    // 适配 android 高版本无法使用 XOR 的问题
    private var mXORPath: Path? = null

    init {
        mPath0 = Path()
        mPath1 = Path()
        mXORPath = Path()
        mMaxLength = hypot(mScreenWidth.toDouble(), mScreenHeight.toDouble()).toFloat()
        mPaint = Paint()

        mPaint?.style = Paint.Style.FILL

        createDrawable()

        val cm = ColorMatrix() //设置颜色数组

        val array = floatArrayOf(
            1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 1f,
            0f, 0f, 0f, 0f, 0f, 1f, 0f
        )
        cm.set(array)
        mColorMatrixFilter = ColorMatrixColorFilter(cm)
        mMatrix = Matrix()

        mTouchX = 0.01f // 不让x,y为0,否则在点计算时会有问题

        mTouchY = 0.01f
    }

    /**
     * 创建阴影的GradientDrawable
     */
    private fun createDrawable() {
        val color = intArrayOf(0x333333, -0x4fcccccd)
        mFolderShadowDrawableRL = GradientDrawable(
            GradientDrawable.Orientation.RIGHT_LEFT, color
        )
        mFolderShadowDrawableRL?.gradientType = GradientDrawable.LINEAR_GRADIENT
        mFolderShadowDrawableLR = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, color
        )
        mFolderShadowDrawableLR?.gradientType = GradientDrawable.LINEAR_GRADIENT

        // 背面颜色组
        val mBackShadowColors = intArrayOf(-0xeeeeef, 0x111111)
        mBackShadowDrawableRL = GradientDrawable(
            GradientDrawable.Orientation.RIGHT_LEFT, mBackShadowColors
        )
        mBackShadowDrawableRL?.gradientType = GradientDrawable.LINEAR_GRADIENT
        mBackShadowDrawableLR = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors
        )
        mBackShadowDrawableLR?.gradientType = GradientDrawable.LINEAR_GRADIENT

        // 前面颜色组
        val mFrontShadowColors = intArrayOf(-0x7feeeeef, 0x111111)
        mFrontShadowDrawableVLR = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, mFrontShadowColors
        )
        mFrontShadowDrawableVLR?.gradientType = GradientDrawable.LINEAR_GRADIENT
        mFrontShadowDrawableVRL = GradientDrawable(
            GradientDrawable.Orientation.RIGHT_LEFT, mFrontShadowColors
        )
        mFrontShadowDrawableVRL?.gradientType = GradientDrawable.LINEAR_GRADIENT
        mFrontShadowDrawableHTB = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, mFrontShadowColors
        )
        mFrontShadowDrawableHTB?.gradientType = GradientDrawable.LINEAR_GRADIENT
        mFrontShadowDrawableHBT = GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP, mFrontShadowColors
        )
        mFrontShadowDrawableHBT?.gradientType = GradientDrawable.LINEAR_GRADIENT
    }


    override fun drawStatic(canvas: Canvas?) {
        if (isCancel) {
            mNextBitmap = mCurBitmap?.copy(Bitmap.Config.RGB_565, true)
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
        when (mDirection) {
            Direction.NEXT -> {
                calcPoints()
                drawCurrentPageArea(canvas, mCurBitmap, mPath0)
                drawNextPageAreaAndShadow(canvas, mNextBitmap)
                drawCurrentPageShadow(canvas)
                drawCurrentBackArea(canvas, mCurBitmap)
            }
            else -> {
                calcPoints()
                drawCurrentPageArea(canvas, mNextBitmap, mPath0)
                drawNextPageAreaAndShadow(canvas, mCurBitmap)
                drawCurrentPageShadow(canvas)
                drawCurrentBackArea(canvas, mNextBitmap)
            }
        }
    }

    private fun calcPoints() {
        mMiddleX = (mTouchX + mCornerX) / 2
        mMiddleY = (mTouchY + mCornerY) / 2
        mBezierControl1.x =
            mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX)
        mBezierControl1.y = mCornerY.toFloat()
        mBezierControl2.x = mCornerX.toFloat()
        val f4 = mCornerY - mMiddleY
        if (f4 == 0f) {
            mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / 0.1f
        } else {
            mBezierControl2.y =
                mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY)
        }
        mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x) / 2
        mBezierStart1.y = mCornerY.toFloat()

        // 当mBezierStart1.x < 0或者mBezierStart1.x > 480时
        // 如果继续翻页，会出现BUG故在此限制
        if (mTouchX > 0 && mTouchX < mScreenWidth) {
            if (mBezierStart1.x < 0 || mBezierStart1.x > mScreenWidth) {
                if (mBezierStart1.x < 0) mBezierStart1.x = mScreenWidth - mBezierStart1.x
                val f1 = Math.abs(mCornerX - mTouchX)
                val f2 = mScreenWidth * f1 / mBezierStart1.x
                mTouchX = Math.abs(mCornerX - f2)
                val f3 = Math.abs(mCornerX - mTouchX) * Math.abs(mCornerY - mTouchY) / f1
                mTouchY = Math.abs(mCornerY - f3)
                mMiddleX = (mTouchX + mCornerX) / 2
                mMiddleY = (mTouchY + mCornerY) / 2
                mBezierControl1.x =
                    mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX)
                mBezierControl1.y = mCornerY.toFloat()
                mBezierControl2.x = mCornerX.toFloat()
                val f5 = mCornerY - mMiddleY
                if (f5 == 0f) {
                    mBezierControl2.y =
                        mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / 0.1f
                } else {
                    mBezierControl2.y =
                        mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY)
                }
                mBezierStart1.x = (mBezierControl1.x - (mCornerX - mBezierControl1.x) / 2)
            }
        }
        mBezierStart2.x = mCornerX.toFloat()
        mBezierStart2.y = mBezierControl2.y - (mCornerY - mBezierControl2.y) / 2
        mTouchToCornerDis =
            Math.hypot((mTouchX - mCornerX).toDouble(), (mTouchY - mCornerY).toDouble()).toFloat()
        mBezierEnd1 =
            getCross(PointF(mTouchX, mTouchY), mBezierControl1, mBezierStart1, mBezierStart2)
        mBezierEnd2 =
            getCross(PointF(mTouchX, mTouchY), mBezierControl2, mBezierStart1, mBezierStart2)
        mBeziervertex1.x = (mBezierStart1.x + 2 * mBezierControl1.x + mBezierEnd1.x) / 4
        mBeziervertex1.y = (2 * mBezierControl1.y + mBezierStart1.y + mBezierEnd1.y) / 4
        mBeziervertex2.x = (mBezierStart2.x + 2 * mBezierControl2.x + mBezierEnd2.x) / 4
        mBeziervertex2.y = (2 * mBezierControl2.y + mBezierStart2.y + mBezierEnd2.y) / 4
    }

    /**
     * 求解直线P1P2和直线P3P4的交点坐标
     */
    private fun getCross(P1: PointF?, P2: PointF?, P3: PointF?, P4: PointF?): PointF {
        val crossP = PointF()
        // 二元函数通式： y=ax+b
        val a1 = ((P2?.y ?: 0f) - (P1?.y ?: 0f)) / ((P2?.x ?: 1f) - (P1?.x ?: 0f))
        val b1 = ((P1?.x ?: 1f) * (P2?.y ?: 1f) - (P2?.x ?: 1f) * (P1?.y ?: 1f)) / ((P1?.x
            ?: 1f) - (P2?.x ?: 0f))
        val a2 = ((P4?.y ?: 0f) - (P3?.y ?: 0f)) / ((P4?.x ?: 1f) - (P3?.x ?: 0f))
        val b2 = ((P3?.x ?: 1f) * (P4?.y ?: 1f) - (P4?.x ?: 1f) * (P3?.y ?: 1f)) / ((P3?.x
            ?: 1f) - (P4?.x ?: 0f))
        crossP.x = (b2 - b1) / (a1 - a2)
        crossP.y = a1 * crossP.x + b1
        return crossP
    }

    private fun drawCurrentPageArea(canvas: Canvas?, bitmap: Bitmap?, path: Path?) {
        mPath0?.run {
            reset()
            moveTo(mBezierStart1.x, mBezierStart1.y)
            quadTo(
                mBezierControl1.x, mBezierControl1.y, mBezierEnd1.x,
                mBezierEnd1.y
            )
            lineTo(mTouchX, mTouchY)
            lineTo(mBezierEnd2.x, mBezierEnd2.y)
            quadTo(
                mBezierControl2.x, mBezierControl2.y, mBezierStart2.x,
                mBezierStart2.y
            )
            lineTo(mCornerX.toFloat(), mCornerY.toFloat())
            close()
        }
        canvas?.save()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mXORPath?.run {
                reset()
                moveTo(0f, 0f)
                lineTo(canvas?.width?.toFloat() ?: 0f, 0f)
                lineTo(canvas?.width?.toFloat() ?: 0f, canvas?.height?.toFloat() ?: 0f)
                lineTo(0f, canvas?.height?.toFloat() ?: 0f)
                close()
                // 取 path 的补给，作为 canvas 的交集
                op(path ?: Path(), Path.Op.XOR)
                canvas?.clipPath(this)
            }
        } else {
            canvas?.clipPath(path ?: Path(), Region.Op.XOR)
        }
        if (bitmap != null) {
            canvas?.drawBitmap(bitmap, 0f, 0f, null)
        }
        try {
            canvas?.restore()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun drawNextPageAreaAndShadow(canvas: Canvas?, bitmap: Bitmap?) {
        mPath1?.run {
            reset()
            moveTo(mBezierStart1.x, mBezierStart1.y)
            lineTo(mBeziervertex1.x, mBeziervertex1.y)
            lineTo(mBeziervertex2.x, mBeziervertex2.y)
            lineTo(mBezierStart2.x, mBezierStart2.y)
            lineTo(mCornerX.toFloat(), mCornerY.toFloat())
            close()
        }

        mDegrees = Math.toDegrees(
            atan2(
                (mBezierControl1.x
                        - mCornerX).toDouble(), (mBezierControl2.y - mCornerY).toDouble()
            )
        ).toFloat()
        val leftX: Int
        val rightX: Int
        val mBackShadowDrawable: GradientDrawable
        if (mIsRTandLB) {  //左下及右上
            leftX = (mBezierStart1.x).toInt()
            rightX = (mBezierStart1.x + mTouchToCornerDis / 4).toInt()
            mBackShadowDrawable = (mBackShadowDrawableLR)!!
        } else {
            leftX = (mBezierStart1.x - mTouchToCornerDis / 4).toInt()
            rightX = mBezierStart1.x.toInt()
            mBackShadowDrawable = (mBackShadowDrawableRL)!!
        }
        canvas?.run {
            save()
            try {
                if (mPath0 != null) {
                    clipPath(mPath0!!)
                    clipPath(mPath1!!, Region.Op.INTERSECT)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (bitmap != null) {
                drawBitmap(bitmap, 0f, 0f, null)
            }
            rotate(mDegrees, mBezierStart1.x, mBezierStart1.y)
            //左上及右下角的xy坐标值,构成一个矩形
            mBackShadowDrawable.setBounds(
                leftX,
                mBezierStart1.y.toInt(),
                rightX,
                (mMaxLength + mBezierStart1.y).toInt()
            )
            mBackShadowDrawable.draw(canvas)
            restore()
        }

    }

    /**
     * 绘制翻起页的阴影
     */
    private fun drawCurrentPageShadow(canvas: Canvas?) {
        val degree: Double = if (mIsRTandLB) {
            (Math.PI / 4 - atan2(
                (mBezierControl1.y - mTouchY).toDouble(), (mTouchX
                        - mBezierControl1.x).toDouble()
            ))
        } else {
            (Math.PI / 4 - atan2(
                (mTouchY - mBezierControl1.y).toDouble(), (mTouchX
                        - mBezierControl1.x).toDouble()
            ))
        }
        // 翻起页阴影顶点与touch点的距离
        val d1 = 25f * 1.414 * cos(degree)
        val d2 = 25f * 1.414 * sin(degree)
        val x = (mTouchX + d1).toFloat()
        val y: Float = if (mIsRTandLB) {
            (mTouchY + d2).toFloat()
        } else {
            (mTouchY - d2).toFloat()
        }
        mPath1?.run {
            reset()
            moveTo(x, y)
            lineTo(mTouchX, mTouchY)
            lineTo(mBezierControl1.x, mBezierControl1.y)
            lineTo(mBezierStart1.x, mBezierStart1.y)
            close()
        }
        canvas?.save()
        drawXORPath(canvas)
        var leftx: Int
        var rightx: Int
        var mCurrentPageShadow: GradientDrawable
        if (mIsRTandLB) {
            leftx = mBezierControl1.x.toInt()
            rightx = mBezierControl1.x.toInt() + 25
            mCurrentPageShadow = mFrontShadowDrawableVLR!!
        } else {
            leftx = (mBezierControl1.x - 25).toInt()
            rightx = mBezierControl1.x.toInt() + 1
            mCurrentPageShadow = mFrontShadowDrawableVRL!!
        }
        var rotateDegrees: Float = Math.toDegrees(
            atan2(
                (mTouchX
                        - mBezierControl1.x).toDouble(), (mBezierControl1.y - mTouchY).toDouble()
            )
        ).toFloat()
        canvas?.rotate(rotateDegrees, mBezierControl1.x, mBezierControl1.y)
        mCurrentPageShadow.setBounds(
            leftx,
            (mBezierControl1.y - mMaxLength).toInt(), rightx,
            (mBezierControl1.y).toInt()
        )
        if (canvas != null) {
            mCurrentPageShadow.draw(canvas)
        }
        canvas?.restore()
        mPath1?.run {
            reset()
            moveTo(x, y)
            lineTo(mTouchX, mTouchY)
            lineTo(mBezierControl2.x, mBezierControl2.y)
            lineTo(mBezierStart2.x, mBezierStart2.y)
            close()
        }
        canvas?.save()
        drawXORPath(canvas)
        if (mIsRTandLB) {
            leftx = (mBezierControl2.y).toInt()
            rightx = (mBezierControl2.y + 25).toInt()
            mCurrentPageShadow = (mFrontShadowDrawableHTB)!!
        } else {
            leftx = (mBezierControl2.y - 25).toInt()
            rightx = (mBezierControl2.y + 1).toInt()
            mCurrentPageShadow = (mFrontShadowDrawableHBT)!!
        }
        rotateDegrees = Math.toDegrees(
            atan2(
                ((mBezierControl2.y
                        - mTouchY).toDouble()), (mBezierControl2.x - mTouchX).toDouble()
            )
        ).toFloat()
        canvas?.rotate(rotateDegrees, mBezierControl2.x, mBezierControl2.y)
        val temp: Float =
            if (mBezierControl2.y < 0) mBezierControl2.y - mScreenHeight else mBezierControl2.y
        val hmg = hypot(mBezierControl2.x.toDouble(), temp.toDouble()).toInt()
        if (hmg > mMaxLength) mCurrentPageShadow
            .setBounds(
                (mBezierControl2.x - 25).toInt() - hmg, leftx,
                (mBezierControl2.x + mMaxLength).toInt() - hmg,
                rightx
            ) else mCurrentPageShadow.setBounds(
            (mBezierControl2.x - mMaxLength).toInt(), leftx,
            (mBezierControl2.x).toInt(), rightx
        )
        if (canvas != null) {
            mCurrentPageShadow.draw(canvas)
            canvas.restore()
        }

    }

    private fun drawXORPath(canvas: Canvas?) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mXORPath?.run {
                    reset()
                    moveTo(0f, 0f)
                    lineTo(canvas?.width?.toFloat() ?: 0f, 0f)
                    lineTo(canvas?.width?.toFloat() ?: 0f, canvas?.height?.toFloat() ?: 0f)
                    lineTo(0f, canvas?.height?.toFloat() ?: 0f)
                    close()

                    // 取 path 的补集，作为 canvas 的交集
                    op(mPath0!!, Path.Op.XOR)
                    mXORPath?.run {
                        canvas?.clipPath(this)
                    }
                }
            } else {
                canvas?.clipPath(mPath0!!, Region.Op.XOR)
            }
            canvas?.clipPath(mPath1!!, Region.Op.INTERSECT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 绘制翻起页背面
     */
    private fun drawCurrentBackArea(canvas: Canvas?, bitmap: Bitmap?) {
        val i = (mBezierStart1.x + mBezierControl1.x).toInt() / 2
        val f1 = abs(i - mBezierControl1.x)
        val i1 = (mBezierStart2.y + mBezierControl2.y).toInt() / 2
        val f2 = abs(i1 - mBezierControl2.y)
        val f3 = f1.coerceAtMost(f2)
        mPath1?.run {
            reset()
            moveTo(mBeziervertex2.x, mBeziervertex2.y)
            lineTo(mBeziervertex1.x, mBeziervertex1.y)
            lineTo(mBezierEnd1.x, mBezierEnd1.y)
            lineTo(mTouchX, mTouchY)
            lineTo(mBezierEnd2.x, mBezierEnd2.y)
            close()
        }
        val mFolderShadowDrawable: GradientDrawable
        val left: Int
        val right: Int
        if (mIsRTandLB) {
            left = (mBezierStart1.x - 1).toInt()
            right = (mBezierStart1.x + f3 + 1).toInt()
            mFolderShadowDrawable = mFolderShadowDrawableLR!!
        } else {
            left = (mBezierStart1.x - f3 - 1).toInt()
            right = (mBezierStart1.x + 1).toInt()
            mFolderShadowDrawable = mFolderShadowDrawableRL!!
        }
        canvas?.save()
        try {
            canvas?.clipPath(mPath0!!)
            canvas?.clipPath(mPath1!!, Region.Op.INTERSECT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mPaint?.colorFilter = mColorMatrixFilter
        //对Bitmap进行取色
        val color = bitmap?.getPixel(1, 1) ?: 0
        //获取对应的三色
        val red = color and 0xff0000 shr 16
        val green = color and 0x00ff00 shr 8
        val blue = color and 0x0000ff
        //转换成含有透明度的颜色
        val tempColor = Color.argb(200, red, green, blue)
        val dis = hypot(
            (mCornerX - mBezierControl1.x).toDouble(),
            (mBezierControl2.y - mCornerY).toDouble()
        ).toFloat()
        val f8 = (mCornerX - mBezierControl1.x) / dis
        val f9 = (mBezierControl2.y - mCornerY) / dis
        mMatrixArray[0] = 1 - 2 * f9 * f9
        mMatrixArray[1] = 2 * f8 * f9
        mMatrixArray[3] = mMatrixArray[1]
        mMatrixArray[4] = 1 - 2 * f8 * f8
        mMatrix?.run {
            reset()
            setValues(mMatrixArray)
            preTranslate(-mBezierControl1.x, -mBezierControl1.y)
            postTranslate(mBezierControl1.x, mBezierControl1.y)
            if (bitmap != null) {
                canvas?.drawBitmap(bitmap, this, mPaint)
            }
        }
        //背景叠加
        canvas?.drawColor(tempColor)
        mPaint?.colorFilter = null
        canvas?.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y)
        mFolderShadowDrawable.setBounds(
            left, mBezierStart1.y.toInt(), right,
            (mBezierStart1.y + mMaxLength).toInt()
        )
        if (canvas != null) {
            mFolderShadowDrawable.draw(canvas)
            canvas.restore()
        }
    }

    override fun startAnim() {
        super.startAnim()
        var dx: Int
        val dy: Int
        // dx 水平方向滑动的距离，负值会使滚动向左滚动
        // dy 垂直方向滑动的距离，负值会使滚动向上滚动
        if (isCancel) {
            dx = if (mCornerX > 0 && mDirection == Direction.NEXT) {
                (mScreenWidth - mTouchX).toInt()
            } else {
                (-mTouchX).toInt()
            }
            if (mDirection != Direction.NEXT) {
                dx = (-(mScreenWidth + mTouchX)).toInt()
            }
            dy = if (mCornerY > 0) {
                (mScreenHeight - mTouchY).toInt()
            } else {
                (-mTouchY).toInt() // 防止mTouchY最终变为0
            }
        } else {
            dx = if (mCornerX > 0 && mDirection == Direction.NEXT) {
                (-(mScreenWidth + mTouchX)).toInt()
            } else {
                (mScreenWidth - mTouchX + mScreenWidth).toInt()
            }
            dy = if (mCornerY > 0) {
                (mScreenHeight - mTouchY).toInt()
            } else {
                (1 - mTouchY).toInt() // 防止mTouchY最终变为0
            }
        }
        mScroller?.startScroll(mTouchX.toInt(), mTouchY.toInt(), dx, dy, 400)
    }

    override fun setDirection(direction: Direction) {
        super.setDirection(direction)
        when (direction) {
            Direction.PRE -> {
                //上一页滑动不出现对角
                if (mStartX > mScreenWidth / 2) {
                    calcCornerXY(mStartX, mScreenHeight.toFloat())
                } else {
                    calcCornerXY(mScreenWidth - mStartX, mScreenHeight.toFloat())
                }
            }
            Direction.NEXT -> {
                if (mScreenWidth / 2 > mStartX) {
                    calcCornerXY(mScreenWidth - mStartX, mStartY)
                }
            }
            else -> {}
        }
    }

    override fun setStartPoint(x: Float, y: Float) {
        super.setStartPoint(x, y)
        calcCornerXY(x, y)
    }

    /**
     * 计算拖拽点对应的拖拽脚
     */
    private fun calcCornerXY(x: Float, y: Float?) {
        mCornerX = if (x <= mScreenWidth / 2) {
            0
        } else {
            mScreenWidth
        }
        mCornerY = if ((y ?: 0f) <= mScreenHeight / 2) {
            0
        } else {
            mScreenHeight
        }
        mIsRTandLB = (mCornerX == 0 && mCornerY == mScreenHeight
                || mCornerX == mScreenWidth && mCornerY == 0)
    }

    override fun setTouchPoint(x: Float, y: Float) {
        super.setTouchPoint(x, y)
        //触摸y中间位置吧y变成屏幕高度
        //触摸y中间位置吧y变成屏幕高度
        if (mStartY > mScreenHeight / 3 && mStartY < mScreenHeight * 2 / 3 || mDirection.equals(
                Direction.PRE
            )
        ) {
            mTouchY = mScreenHeight.toFloat()
        }

        if (mStartY > mScreenHeight / 3 && mStartY < mScreenHeight / 2 && mDirection.equals(
                Direction.NEXT
            )
        ) {
            mTouchY = 1f
        }
    }

    /**
     * 是否能够拖动过去
     */
    fun canDragOver(): Boolean {
        return mTouchToCornerDis > mScreenWidth / 10
    }

    fun right(): Boolean {
        return mCornerX <= -4
    }

}