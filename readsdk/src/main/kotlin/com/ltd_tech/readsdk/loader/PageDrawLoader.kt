package com.ltd_tech.readsdk.loader

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import androidx.core.content.ContextCompat
import com.ltd_tech.core.entities.TxtChapter
import com.ltd_tech.core.entities.TxtPage
import com.ltd_tech.core.utils.DateUtils
import com.ltd_tech.core.utils.ResourceUtils
import com.ltd_tech.core.utils.ScreenUtils
import com.ltd_tech.core.widgets.pager.PageMode
import com.ltd_tech.core.widgets.pager.PageStyle
import com.ltd_tech.core.widgets.pager.PagerView
import com.ltd_tech.core.widgets.pager.ReadConfigManager
import com.ltd_tech.readsdk.R
import com.ltd_tech.readsdk.entities.BookEntity
import com.ltd_tech.readsdk.entities.ReadTable
import com.ltd_tech.readsdk.utils.DataControls

/**
 * 页面加载绘制loader
 *  @param mPageView 页面显示类
 *  @param mBookEntity 书本对象
 */
open class PageDrawLoader(private val mPageView: PagerView, private val mBookEntity: BookEntity) {

    companion object {
        // 当前页面的状态
        const val STATUS_LOADING = 1 // 正在加载
        const val STATUS_FINISH = 2 // 加载完成
        const val STATUS_ERROR = 3 // 加载错误 (一般是网络加载情况)
        const val STATUS_EMPTY = 4 // 空数据
        const val STATUS_PARING = 5 // 正在解析 (装载本地数据)
        const val STATUS_PARSE_ERROR = 6 // 本地文件解析错误(暂未被使用)
        const val STATUS_CATEGORY_EMPTY = 7 // 获取到的目录为空

        // 默认的显示参数配置
        const val DEFAULT_MARGIN_HEIGHT = 28f
        const val DEFAULT_MARGIN_WIDTH = 15f
        const val DEFAULT_TIP_SIZE = 12
        const val EXTRA_TITLE_SIZE = 4
    }

    // 当前的状态
    protected var mStatus = STATUS_LOADING

    /** ---------------------------  书籍数据  ----------------------------  */

    // 当前章节列表
    protected var mChapterList: MutableList<TxtChapter>? = null

    // 当前显示的页
    protected var mCurPage: TxtPage? = null

    // 上一章的页面列表缓存
    protected var mPrePageList: MutableList<TxtPage>? = null

    // 当前章节的页面列表
    protected var mCurPageList: MutableList<TxtPage>? = null

    // 下一章的页面列表缓存
    protected var mNextPageList: MutableList<TxtPage>? = null

    // 存储阅读记录类
    protected var mReadTable: ReadTable? = null

    // 当前章
    protected var mCurChapterPos = 0

    //上一章的记录
    protected var mLastChapterPos = 0

    // 判断章节列表是否加载完成
    protected var isChapterListPrepare = false

    // 监听器
    protected var mPageChangeListener: OnPageChangeListener? = null


    /** ---------------------  绘制部分  --------------------------- */
    // 绘制电池的画笔
    protected var mBatteryPaint: Paint? = null

    // 绘制提示的画笔
    protected var mTipPaint: Paint? = null

    // 绘制标题的画笔
    protected var mTitlePaint: Paint? = null

    // 绘制小说内容的画笔
    private var mTextPaint: TextPaint? = null

    // 绘制背景颜色的画笔(用来擦除需要重绘的部分)
    protected var mBgPaint: Paint? = null

    // 页面的翻页效果模式
    protected var mPageMode: PageMode? = null

    // 加载器的颜色主题
    protected var mPageStyle: PageStyle? = null

    //当前是否是夜间模式
    protected var isNightMode = false

    //书籍绘制区域的宽高
    protected var mVisibleWidth = 0
    protected var mVisibleHeight = 0

    //应用的宽高
    protected var mDisplayWidth = 0
    protected var mDisplayHeight = 0

    //间距
    protected var mMarginWidth = 0
    protected var mMarginHeight = 0

    //字体的颜色
    private var mTextColor = 0

    //标题的大小
    private var mTitleSize = 0

    //字体的大小
    private var mTextSize = 0

    //行间距
    protected var mTextInterval = 0

    //标题的行间距
    protected var mTitleInterval = 0

    //段落距离(基于行间距的额外距离)
    protected var mTextPara = 0
    protected var mTitlePara = 0

    //电池的百分比
    private var mBatteryLevel = 0

    //当前页面的背景
    private var mBgColor = 0

    init {
        mChapterList = mutableListOf()
        initConfigs()
        initPaint()
        // 设置视图
        mPageView.setPageMode(mPageMode)
        mPageView.setBgColor(mBgColor)
        // 初始化书籍
        prepareBook()
    }

    /**
     * 初始化配置信息
     */
    private fun initConfigs() {
        // 获取配置管理器
        // 获取配置参数
        mPageMode = ReadConfigManager.getPageMode()
        mPageStyle = ReadConfigManager.getPageStyle()
        // 初始化参数
        mMarginWidth = ScreenUtils.dp2px(DEFAULT_MARGIN_WIDTH)
        mMarginHeight = ScreenUtils.dp2px(DEFAULT_MARGIN_HEIGHT)
        setUpTextParams(ReadConfigManager.getTextSize())
    }

    private fun initPaint() {
        // 绘制提示的画笔
        mTipPaint = Paint().apply {
            color = mTextColor
            textAlign = Paint.Align.LEFT // 绘制的起始点
            textSize = ScreenUtils.sp2px(DEFAULT_TIP_SIZE).toFloat()// Tip默认的字体大小
            isAntiAlias = true
            isSubpixelText = true
        }

        // 绘制页面内容的画笔
        mTextPaint = TextPaint().apply {
            color = mTextColor
            textSize = mTextSize.toFloat()
            isAntiAlias = true
        }

        // 绘制标题的画笔
        mTitlePaint = TextPaint().apply {
            color = mTextColor
            textSize = mTitleSize.toFloat()
            style = Paint.Style.FILL_AND_STROKE
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        // 绘制背景的画笔
        mBgPaint = Paint().apply {
            color = mBgColor
        }

        // 绘制电池的画笔
        mBatteryPaint = Paint().apply {
            isAntiAlias = true
            isDither = true
        }

        // 初始化页面样式
        updateNightMode(ReadConfigManager.isNightMode())
    }

    /**
     * 设置夜间模式
     */
    open fun updateNightMode(nightMode: Boolean) {
        ReadConfigManager.setNightMode(nightMode)
        isNightMode = nightMode
        if (isNightMode) {
            mBatteryPaint?.color = Color.WHITE
            setPageStyle(PageStyle.NIGHT)
        } else {
            mBatteryPaint?.color = Color.BLACK
            setPageStyle(mPageStyle)
        }
    }

    /**
     * 设置页面样式
     */
    private fun setPageStyle(pageStyle: PageStyle?) {
        if (pageStyle != PageStyle.NIGHT) {
            mPageStyle = pageStyle
            ReadConfigManager.setPageStyle(pageStyle)
        }
        if (isNightMode && pageStyle != PageStyle.NIGHT) {
            return
        }

        // 设置当前颜色样式
        pageStyle?.run {
            mTextColor = ContextCompat.getColor(mPageView.context, fontColor)
            mBgColor = ContextCompat.getColor(mPageView.context, bgColor)
        }

        mTipPaint?.color = mTextColor
        mTitlePaint?.color = mTextColor
        mTextPaint?.color = mTextColor
        mBgPaint?.color = mBgColor
        mPageView.drawCurPage(false)
    }

    /**
     * 设置页面调用设置字体方法刷新字体样式
     */
    open fun setUpTextParams(textSize: Int) {
        // 配置文字有关的参数
        mTextSize = textSize
        mTitleSize = mTextSize + ScreenUtils.sp2px(EXTRA_TITLE_SIZE)
        // 行间距(大小为字体的一半)
        mTextInterval = mTextSize / 2
        mTitleInterval = mTitleSize / 2
        // 段落间距(大小为字体的高度)
        mTextPara = mTextSize
        mTitlePara = mTitleSize
    }

    /**
     * 初始化书籍
     */
    private fun prepareBook() {
        mReadTable = DataControls.getBookRecord(mBookEntity._id)
        mCurChapterPos = mReadTable?.chapter ?: 0
        mLastChapterPos = mCurChapterPos
    }

    /**
     * 获取标题画笔文字大小
     */
    fun getTitlePaintTextSize() = mTitlePaint?.textSize ?: 0f

    /**
     * 获取内容画笔字体大小
     */
    fun getTextPaintTextSize() = mTextPaint?.textSize ?: 0f

    fun breakTextTitlePaint(paragraph: String?) = mTitlePaint?.breakText(
        paragraph,
        true, mVisibleWidth.toFloat(), null
    ) ?: 0

    fun breakTextTextPaint(paragraph: String?) = mTextPaint?.breakText(
        paragraph,
        true, mVisibleWidth.toFloat(), null
    ) ?: 0


    fun drawPage(bitmap: Bitmap?, isUpdate: Boolean) {
        drawBackground(mPageView.getBgBitmap(), isUpdate)
        if (!isUpdate) {
            drawContent(bitmap)
        }
        //更新绘制
        //更新绘制
        mPageView.invalidate()
    }

    /**
     * 显示背景内容
     */
    private fun drawBackground(bitmap: Bitmap?, isUpdate: Boolean) {
        if (bitmap == null) {
            return
        }
        val canvas = Canvas(bitmap)
        val tipMarginHeight = ScreenUtils.dp2px(3f)
        if (!isUpdate) {
            /****绘制背景****/
            canvas.drawColor(mBgColor)
            if (!mChapterList.isNullOrEmpty()) {
                /*****初始化标题的参数********/
                //需要注意的是:绘制text的y的起始点是text的基准线的位置，而不是从text的头部的位置
                val tipTop =
                    (tipMarginHeight - (mTipPaint?.fontMetrics?.top?.toInt() ?: 0)).toFloat()
                //根据状态不一样，数据不一样
                if (mStatus != STATUS_FINISH) {
                    if (isChapterListPrepare) {
                        val title = mChapterList?.get(mCurChapterPos)?.title
                        if (title != null && mTipPaint != null) {
                            canvas.drawText(title, mMarginWidth.toFloat(), tipTop, mTipPaint!!)
                        }
                    }
                } else {
                    val title = mCurPage?.title
                    if (title != null && mTipPaint != null) {
                        canvas.drawText(title, mMarginWidth.toFloat(), tipTop, mTipPaint!!)
                    }
                }

                /******绘制页码********/
                // 底部的字显示的位置Y
                val y = mDisplayHeight - (mTipPaint?.fontMetrics?.bottom?.toInt()
                    ?: 0) - tipMarginHeight
                // 只有finish的时候采用页码
                if (mStatus == STATUS_FINISH) {
                    val percent = "${((mCurPage?.position ?: 0) + 1)} / ${mCurPageList?.size ?: 0}"
                    if (mTipPaint != null) {
                        canvas.drawText(percent, mMarginWidth.toFloat(), y.toFloat(), mTipPaint!!)
                    }
                }
            }
        } else {
            //擦除区域
            mBgPaint?.run {
                color = mBgColor;
                canvas.drawRect(
                    (mDisplayWidth / 2).toFloat(),
                    (mDisplayHeight - mMarginHeight + ScreenUtils.dp2px(2f)).toFloat(),
                    mDisplayWidth.toFloat(),
                    mDisplayHeight.toFloat(),
                    this
                )
            }

        }
        /******绘制电池********/
        val visibleRight = mDisplayWidth - mMarginWidth
        val visibleBottom = mDisplayHeight - tipMarginHeight

        val outFrameWidth = mTipPaint?.measureText("xxx") ?: 0f
        val outFrameHeight = mTipPaint?.textSize ?: 0f

        val polarHeight = ScreenUtils.dp2px(6f)
        val polarWidth = ScreenUtils.dp2px(2f)
        val border = 1f
        val innerMargin = 1
        //电极的制作
        val polarLeft = visibleRight - polarWidth
        val polarTop = (visibleBottom - (outFrameHeight + polarHeight) / 2).toInt()
        val polar =
            Rect(polarLeft, polarTop, visibleRight, polarTop + polarHeight - ScreenUtils.dp2px(2f))

        mBatteryPaint?.style = Paint.Style.FILL
        mBatteryPaint?.run {
            canvas.drawRect(polar, this)
        }

        //外框的制作
        val outFrameLeft = (polarLeft - outFrameWidth).toInt()
        val outFrameTop = (visibleBottom - outFrameHeight).toInt()
        val outFrameBottom = visibleBottom - ScreenUtils.dp2px(2f)
        val outFrame = Rect(outFrameLeft, outFrameTop, polarLeft, outFrameBottom)

        mBatteryPaint?.run {
            style = Paint.Style.STROKE
            strokeWidth = border
            canvas.drawRect(outFrame, this)
        }
        //内框的制作
        val innerWidth = (outFrame.width() - innerMargin * 2 - border) * (mBatteryLevel / 100.0f)
        val innerFrame = RectF(
            outFrameLeft + border + innerMargin, outFrameTop + border + innerMargin,
            outFrameLeft + border + innerMargin + innerWidth, outFrameBottom - border - innerMargin
        )

        mBatteryPaint?.run {
            style = Paint.Style.FILL
            canvas.drawRect(innerFrame, this)
        }

        /******绘制当前时间********/
        //底部的字显示的位置Y
        val y = mDisplayHeight - (mTipPaint?.fontMetrics?.bottom?.toInt() ?: 0) - tipMarginHeight
        val time = DateUtils.dateConvert(System.currentTimeMillis(), DateUtils.FORMAT_HH_MM) ?: ""
        val x = outFrameLeft - (mTipPaint?.measureText(time) ?: 0f) - ScreenUtils.dp2px(4f)
        canvas.drawText(time, x, y.toFloat(), mTipPaint!!)

    }

    /**
     * 显示文章内容
     */
    private fun drawContent(bitmap: Bitmap?) {
        if (bitmap == null) {
            return
        }
        val canvas = Canvas(bitmap)
        if (mPageMode == PageMode.SCROLL) {
            canvas.drawColor(mBgColor);
        }

        /******绘制内容****/
        if (mStatus != STATUS_FINISH) {
            //绘制字体
            val tip = when (mStatus) {
                STATUS_LOADING -> {
                    ResourceUtils.getString(R.string.read_book_tip_loading)
                }

                STATUS_ERROR -> {
                    ResourceUtils.getString(R.string.read_book_tip_error)
                }

                STATUS_EMPTY -> {
                    ResourceUtils.getString(R.string.read_book_tip_empty)
                }

                STATUS_PARING -> {
                    ResourceUtils.getString(R.string.read_book_tip_paring)
                }

                STATUS_PARSE_ERROR -> {
                    ResourceUtils.getString(R.string.read_book_tip_parse_error)
                }

                STATUS_CATEGORY_EMPTY -> {
                    ResourceUtils.getString(R.string.read_book_tip_category_empty)
                }

                else -> ""
            }

            //将提示语句放到正中间
            val textHeight = mTextPaint?.fontMetrics?.let {
                it.top - it.bottom
            } ?: 0f
            val textWidth = mTextPaint?.measureText(tip) ?: 0f

            val pivotX = (mDisplayWidth - textWidth) / 2
            val pivotY = (mDisplayHeight - textHeight) / 2

            canvas.drawText(tip, pivotX, pivotY, mTextPaint!!)

        } else {

            var top = if (mPageMode == PageMode.SCROLL) {
                -(mTextPaint?.fontMetrics?.top ?: 0f)
            } else {
                mMarginHeight - (mTextPaint?.fontMetrics?.top ?: 0f)
            }
            //设置总距离
            val interval = mTextInterval + (mTextPaint?.textSize?.toInt() ?: 0)
            val paragraphInterval = mTextPara + (mTextPaint?.textSize?.toInt() ?: 0)
            val titleInterval = mTitleInterval + (mTitlePaint?.textSize ?: 0f)
            val titleParagraphInterval = mTitlePara + (mTitlePaint?.textSize ?: 0f)
            var str: String = ""
            mCurPage?.run {
                //对标题进行绘制
                for (i in 0..titleLines) {
                    str = lines?.get(i) ?: ""
                    //设置顶部间距
                    if (i == 0) {
                        top += mTitlePara
                    }
                    //计算文字显示的起始点
                    val start = (mDisplayWidth - (mTitlePaint?.measureText(str) ?: 0f)) / 2
                    //进行绘制
                    if (mTitlePaint != null) {
                        canvas.drawText(str, start, top, mTitlePaint!!)
                    }
                    //设置尾部间距
                    top += if (i == titleLines - 1) {
                        titleParagraphInterval
                    } else {
                        //行间距
                        titleInterval
                    }

                    //对内容进行绘制
                    for (j in titleLines..(lines?.size ?: 0)) {
                        str = lines?.get(i) ?: ""
                        if (mTextPaint != null) {
                            canvas.drawText(str, mMarginWidth.toFloat(), top, mTextPaint!!)
                        }
                        top += if (str.endsWith("\n")) {
                            paragraphInterval
                        } else {
                            interval
                        }
                    }
                }

            }
        }
    }

    /**
     * 章节错误 加载错误页面
     */
    open fun chapterError() {
        //加载错误
        mStatus = STATUS_ERROR
        mPageView.drawCurPage(false)
    }

    /**
     * 设置或更新显示字体
     */
    fun setTextSize(textSize: Int) {
        // 设置文字相关参数
        setUpTextParams(textSize)
        // 设置画笔的字体大小
        mTextPaint?.textSize = mTextSize.toFloat()
        // 设置标题的字体大小
        mTitlePaint?.textSize = mTitleSize.toFloat()
        // 存储文字大小
        ReadConfigManager.setTextSize(mTextSize)
        // 取消缓存
        mPrePageList = null
        mNextPageList = null
    }

    /**
     * 更新电量
     *
     * @param level
     */
    open fun updateBattery(level: Int) {
        mBatteryLevel = level
        if (!mPageView.isRunning()) {
            mPageView.drawCurPage(true)
        }
    }

    /**
     * 设置提示的文字大小
     *
     * @param textSize:单位为 px。
     */
    open fun setTipTextSize(textSize: Int) {
        mTipPaint?.textSize = textSize.toFloat()
        // 如果屏幕大小加载完成
        mPageView.drawCurPage(false)
    }

    /**
     * 翻页动画
     *
     * @param pageMode:翻页模式
     * @see PageMode
     */
    open fun setPageMode(pageMode: PageMode) {
        mPageMode = pageMode
        mPageView.setPageMode(mPageMode)
        ReadConfigManager.setPageMode(mPageMode)

        // 重新绘制当前页
        mPageView.drawCurPage(false)
    }

    /**
     * 更新时间
     */
    open fun updateTime() {
        if (!mPageView.isRunning()) {
            mPageView.drawCurPage(true)
        }
    }

    /**
     * 翻到上一页
     */
    open fun skipToPrePage(): Boolean {
        return mPageView.autoPrevPage()
    }

    /**
     * 翻到下一页
     */
    open fun skipToNextPage(): Boolean {
        return mPageView.autoNextPage()
    }

    /**
     * @return:获取初始显示的页面
     */
    fun getCurPage(pos: Int): TxtPage? {
        mPageChangeListener?.onPageChange(pos)
        return mCurPageList?.get(pos)
    }

    /**
     * 跳转到指定的页
     *
     * @param pos
     */
    open fun skipToPage(pos: Int): Boolean {
        if (!isChapterListPrepare) {
            return false
        }
        mCurPage = getCurPage(pos)
        mPageView.drawCurPage(false)
        return true
    }

    /**
     * 设置内容与屏幕的间距
     *
     * @param marginWidth  :单位为 px
     * @param marginHeight :单位为 px
     */
    fun setMargin(marginWidth: Int, marginHeight: Int) {
        mMarginWidth = marginWidth
        mMarginHeight = marginHeight

        // 如果是滑动动画，则需要重新创建了
        if (mPageMode === PageMode.SCROLL) {
            mPageView.setPageMode(PageMode.SCROLL)
        }
        mPageView.drawCurPage(false)
    }

    /**
     * 获取当前页的状态
     *
     * @return
     */
    open fun getPageStatus(): Int = mStatus

    /**
     * 获取章节目录。
     */
    open fun getChapterCategory(): List<TxtChapter?>? = mChapterList

    /**
     * 获取当前页的页码
     */
    open fun getPagePos(): Int = mCurPage?.position ?: 0

    /**
     * 获取当前章节的章节位置
     */
    open fun getChapterPos(): Int = mCurChapterPos

    /**
     * 获取距离屏幕的高度
     *
     * @return
     */
    open fun getMarginHeight(): Int = mMarginHeight

    /**
     * 关闭书本
     */
    open fun closeBook() {
        isChapterListPrepare = false
        clearList(mChapterList)
        clearList(mCurPageList)
        clearList(mNextPageList)
        mChapterList = null
        mCurPageList = null
        mNextPageList = null
        mCurPage = null
    }

    private fun clearList(list: MutableList<*>?) {
        list?.clear()
    }

}