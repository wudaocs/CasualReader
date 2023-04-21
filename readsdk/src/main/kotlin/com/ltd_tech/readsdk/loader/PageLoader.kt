package com.ltd_tech.readsdk.loader

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import androidx.core.content.ContextCompat
import com.ltd_tech.core.entities.TxtChapter
import com.ltd_tech.core.entities.TxtPage
import com.ltd_tech.core.utils.F
import com.ltd_tech.core.utils.ScreenUtil
import com.ltd_tech.core.utils.StringUtils
import com.ltd_tech.core.widgets.pager.PageMode
import com.ltd_tech.core.widgets.pager.PageStyle
import com.ltd_tech.core.widgets.pager.PagerView
import com.ltd_tech.core.widgets.pager.ReadConfigManager
import com.ltd_tech.readsdk.entities.BookEntity
import com.ltd_tech.readsdk.entities.ReadTable
import com.ltd_tech.readsdk.utils.DataControls
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException

abstract class PageLoader(private val mPageView: PagerView, private val mBookEntity: BookEntity) {

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
        private const val DEFAULT_MARGIN_HEIGHT = 28
        private const val DEFAULT_MARGIN_WIDTH = 15
        private const val DEFAULT_TIP_SIZE = 12
        private const val EXTRA_TITLE_SIZE = 4
    }

    // 当前章节列表
    protected var mChapterList: List<TxtChapter>? = null

    // 书本对象
//    protected var mBookEntity: BookEntity? = null

    // 页面显示类
//    private var mPageView: PagerView? = null

    // 当前显示的页
    private var mCurPage: TxtPage? = null

    // 上一章的页面列表缓存
    private var mPrePageList: List<TxtPage>? = null

    // 当前章节的页面列表
    private var mCurPageList: List<TxtPage>? = null

    // 下一章的页面列表缓存
    private var mNextPageList: List<TxtPage>? = null

    // 绘制电池的画笔
    private var mBatteryPaint: Paint? = null

    // 绘制提示的画笔
    private var mTipPaint: Paint? = null

    // 绘制标题的画笔
    private var mTitlePaint: Paint? = null

    // 绘制背景颜色的画笔(用来擦除需要重绘的部分)
    private var mBgPaint: Paint? = null

    // 绘制小说内容的画笔
    private var mTextPaint: TextPaint? = null

    // 被遮盖的页，或者认为被取消显示的页
    private var mCancelPage: TxtPage? = null

    // 监听器
    protected var mPageChangeListener: OnPageChangeListener? = null

    // 当前的状态
    protected var mStatus = STATUS_LOADING

    // 判断章节列表是否加载完成
    protected var isChapterListPrepare = false

    // 是否打开过章节
    private val isChapterOpen = false
    private val isFirstOpen = true
    private val isClose = false

    // 页面的翻页效果模式
    private var mPageMode: PageMode? = null

    // 加载器的颜色主题
    private var mPageStyle: PageStyle? = null


    //当前是否是夜间模式
    private var isNightMode = false

    //书籍绘制区域的宽高
    private val mVisibleWidth = 0
    private val mVisibleHeight = 0

    //应用的宽高
    private val mDisplayWidth = 0
    private val mDisplayHeight = 0

    //间距
    private var mMarginWidth = 0
    private var mMarginHeight = 0

    //字体的颜色
    private var mTextColor = 0

    //标题的大小
    private var mTitleSize = 0

    //字体的大小
    private var mTextSize = 0

    //行间距
    private var mTextInterval = 0

    //标题的行间距
    private var mTitleInterval = 0

    //段落距离(基于行间距的额外距离)
    private var mTextPara = 0
    private var mTitlePara = 0

    //电池的百分比
    private val mBatteryLevel = 0

    //当前页面的背景
    private var mBgColor = 0

    // 当前章
    protected var mCurChapterPos = 0

    //上一章的记录
    private var mLastChapterPos = 0

    private var mReadTable: ReadTable? = null

    private var coroutinesScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        mChapterList = mutableListOf()

        // 初始化数据
        initData()
        // 初始化画笔
        initPaint()
        // 初始化PageView
        initPageView()
        // 初始化书籍
        prepareBook()
    }

    private fun initData() {
        // 获取配置管理器
        // 获取配置参数
        mPageMode = ReadConfigManager.getPageMode()
        mPageStyle = ReadConfigManager.getPageStyle()
        // 初始化参数
        mMarginWidth = ScreenUtil.dpToPx(DEFAULT_MARGIN_WIDTH)
        mMarginHeight = ScreenUtil.dpToPx(DEFAULT_MARGIN_HEIGHT)
        // 配置文字有关的参数
        setUpTextParams(ReadConfigManager.getTextSize())
    }

    /**
     * 作用：设置与文字相关的参数
     */
    private fun setUpTextParams(textSize: Int) {
        // 文字大小
        mTextSize = textSize
        mTitleSize = mTextSize + ScreenUtil.spToPx(EXTRA_TITLE_SIZE)
        // 行间距(大小为字体的一半)
        mTextInterval = mTextSize / 2
        mTitleInterval = mTitleSize / 2
        // 段落间距(大小为字体的高度)
        mTextPara = mTextSize
        mTitlePara = mTitleSize
    }

    private fun initPaint() {
        // 绘制提示的画笔
        mTipPaint = Paint().apply {
            color = mTextColor
            textAlign = Paint.Align.LEFT // 绘制的起始点
            textSize = ScreenUtil.spToPx(DEFAULT_TIP_SIZE).toFloat()// Tip默认的字体大小
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
        setNightMode(ReadConfigManager.isNightMode())
    }

    /**
     * 设置夜间模式
     */
    open fun setNightMode(nightMode: Boolean) {
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
    fun setPageStyle(pageStyle: PageStyle?) {
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

    private fun initPageView() {
        //配置参数
        mPageView.setPageMode(mPageMode)
        mPageView.setBgColor(mBgColor)
    }

    /**
     * 初始化书籍
     */
    private fun prepareBook() {
        mReadTable = DataControls.getBookRecord(mBookEntity._id)
        mCurChapterPos = mReadTable?.chapter ?: 0
        mLastChapterPos = mCurChapterPos
    }

    fun prepareDisplay(width: Int, height: Int) {

    }


    fun prev(): Boolean {
        // 以下情况禁止翻页
        if (!canTurnPage()) {
            return false
        }
        if (mStatus == STATUS_FINISH) {
            // 先查看是否存在上一页
            val prevPage: TxtPage? = getPrevPage()
            if (prevPage != null) {
                mCancelPage = mCurPage
                mCurPage = prevPage
                mPageView.drawNextPage()
                return true
            }
        }

        if (!hasPrevChapter()) {
            return false
        }

        mCancelPage = mCurPage
        if (parsePrevChapter()) {
            mCurPage = getPrevLastPage()
        } else {
            mCurPage = TxtPage()
        }
        mPageView.drawNextPage()
        return true
    }

    /**
     * 翻到下一页
     *
     * @return:是否允许翻页
     */
    fun next(): Boolean {
        // 以下情况禁止翻页
        if (!canTurnPage()) {
            return false
        }

        if (mStatus == STATUS_FINISH) {
            // 先查看是否存在下一页
            val nextPage = getNextPage()
            if (nextPage != null) {
                mCancelPage = mCurPage
                mCurPage = nextPage
                mPageView.drawNextPage()
                return true
            }
        }

        if (!hasNextChapter()) {
            return false
        }

        mCancelPage = mCurPage
        // 解析下一章数据
        // 解析下一章数据
        mCurPage = if (parseNextChapter()) {
            mCurPageList!![0]
        } else {
            TxtPage()
        }
        mPageView.drawNextPage()
        return true
    }

    fun pageCancel() {

    }

    fun drawPage(bitmap: Bitmap?, isUpdate: Boolean) {

    }


    /**
     * 获取当前章节的章节位置
     *
     * @return
     */
    open fun getChapterPos(): Int {
        return mCurChapterPos
    }

    /**
     * 获取距离屏幕的高度
     *
     * @return
     */
    open fun getMarginHeight(): Int {
        return mMarginHeight
    }

    /**
     * 根据当前状态，决定是否能够翻页
     */
    private fun canTurnPage(): Boolean {
        if (!isChapterListPrepare || (mStatus == STATUS_PARSE_ERROR
                    || mStatus == STATUS_PARING)
        ) {
            return false
        } else if (mStatus == STATUS_ERROR) {
            mStatus = STATUS_LOADING
        }
        return true
    }

    /**************************************private method********************************************/
    /**
     * 将章节数据，解析成页面列表
     *
     * @param chapter：章节信息
     * @param br：章节的文本流
     * @return
     */
    private fun loadPages(chapter :TxtChapter, br: BufferedReader): List<TxtPage> {
        //生成的页面
        val pages = mutableListOf<TxtPage>()
        //使用流的方式加载
        val lines = mutableListOf<String>()
        var rHeight = mVisibleHeight
        var titleLinesCount = 0
        var showTitle = true // 是否展示标题
        //默认展示标题
        var paragraph = chapter.title
        try {
            while (showTitle || (paragraph?.apply { paragraph = br.readLine() }) != null) {
                paragraph = StringUtils.convertCC(paragraph)
                // 重置段落
                if (!showTitle) {
                    paragraph = paragraph?.replace("\\s", "")
                    // 如果只有换行符，那么就不执行
                    if (paragraph.equals("")) {
                        continue
                    }
                    paragraph = StringUtils.halfToFull("  $paragraph\n");
                } else {
                    //设置 title 的顶部间距
                    rHeight -= mTitlePara;
                }
                var wordCount: Int
                var subStr: String
                paragraph?.run {
                    while (length > 0) {
                        //当前空间，是否容得下一行文字
                        rHeight -= if (showTitle) {
                            mTitlePaint?.textSize?.toInt() ?: 0
                        } else {
                            mTextPaint?.textSize?.toInt() ?: 0
                        }
                        // 一页已经填充满了，创建 TextPage
                        if (rHeight <= 0) {
                            // 创建Page
                            val page: TxtPage = TxtPage()
                            page.position = pages.size
                            page.title = StringUtils.convertCC(chapter.title)
                            page.lines = lines
                            page.titleLines = titleLinesCount
                            pages.add(page)
                            // 重置Lines
                            lines.clear()
                            rHeight = mVisibleHeight
                            titleLinesCount = 0

                            continue
                        }

                        //测量一行占用的字节数
                        wordCount = if (showTitle) {
                            mTitlePaint?.breakText(
                                paragraph,
                                true, mVisibleWidth.toFloat(), null
                            ) ?: 0
                        } else {
                            mTextPaint?.breakText(
                                paragraph,
                                true, mVisibleWidth.toFloat(), null
                            ) ?: 0
                        }

                        subStr = paragraph?.substring(0, wordCount) ?: ""
                        if (subStr != "\n") {
                            //将一行字节，存储到lines中
                            lines.add(subStr)

                            //设置段落间距
                            if (showTitle) {
                                titleLinesCount += 1;
                                rHeight -= mTitleInterval;
                            } else {
                                rHeight -= mTextInterval;
                            }
                        }
                    //裁剪
                    paragraph = paragraph?.substring(wordCount);
                    }
                }


                //增加段落的间距
                if (!showTitle && lines.isNotEmpty()) {
                    rHeight = rHeight - mTextPara + mTextInterval;
                }

                if (showTitle) {
                    rHeight = rHeight - mTitlePara + mTitleInterval;
                    showTitle = false
                }
            }

            if (lines.isNotEmpty()) {
                //创建Page
                val page = TxtPage()
                page.position = pages.size
                page.title = StringUtils.convertCC(chapter.title)
                page.lines = lines
                page.titleLines = titleLinesCount
                pages.add(page)
                //重置Lines
                lines.clear()
            }
        } catch (e : FileNotFoundException) {
            e.printStackTrace()
        } catch (e : IOException) {
            e.printStackTrace()
        } finally {
            F().close(br)
        }
        return pages;
    }

    /**
     * @return:获取初始显示的页面
     */
    private fun getCurPage(pos: Int): TxtPage? {
        mPageChangeListener?.onPageChange(pos)
        return mCurPageList?.get(pos)
    }


    /**
     * @return:获取上一个页面
     */
    private fun getPrevPage(): TxtPage? {
        val pos = (mCurPage?.position ?: 0) - 1
        if (pos < 0) {
            return null
        }
        mPageChangeListener?.onPageChange(pos)
        return mCurPageList?.get(pos)
    }

    /**
     * @return:获取下一的页面
     */
    private fun getNextPage(): TxtPage? {
        val pos = (mCurPage?.position ?: 0) - 1
        if (pos >= (mCurPageList?.size ?: 0)) {
            return null
        }
        mPageChangeListener?.onPageChange(pos)
        return mCurPageList?.get(pos)
    }

    /**
     * @return:获取上一个章节的最后一页
     */
    private fun getPrevLastPage(): TxtPage? {
        val pos: Int = (mCurPageList?.size ?: 0) - 1
        mPageChangeListener?.onPageChange(pos)
        return mCurPageList?.get(pos)
    }

    /**
     * 预加载下一章
     */
    private fun preLoadNextChapter() {
        val nextChapter = mCurChapterPos + 1

        // 如果不存在下一章，且下一章没有数据，则不进行加载。
        if (!hasNextChapter()
            || !hasChapterData(mChapterList!![nextChapter])
        ) {
            return
        }

        //如果之前正在加载则取消
        coroutinesScope.cancel()

        coroutinesScope.launch {

        }

        //调用异步进行预加载加载
//        Single.create(object : SingleOnSubscribe<List<TxtPage?>?>() {
//            @Throws(Exception::class)
//            fun subscribe(e: SingleEmitter<List<TxtPage?>?>) {
//                e.onSuccess(loadPageList(nextChapter))
//            }
//        }).compose(RxUtils::toSimpleSingle)
//            .subscribe(object : SingleObserver<List<TxtPage?>?>() {
//                fun onSubscribe(d: Disposable) {
//                    mPreLoadDisp = d
//                }
//
//                fun onSuccess(pages: List<TxtPage?>) {
//                    mNextPageList = pages
//                }
//
//                fun onError(e: Throwable?) {
//                    //无视错误
//                }
//            })
    }

    private fun hasNextChapter(): Boolean {
        // 判断是否到达目录最后一章
        return mCurChapterPos + 1 < ((mChapterList?.size) ?: 0)
    }

    /*******************************abstract method */
    /**
     * 刷新章节列表
     */
    abstract fun refreshChapterList()

    /**
     * 获取章节的文本流
     *
     * @param chapter
     */
    @Throws(java.lang.Exception::class)
    protected abstract fun getChapterReader(chapter: TxtChapter?): BufferedReader?

    /**
     * 章节数据是否存在
     *
     * @return
     */
    protected abstract fun hasChapterData(chapter: TxtChapter?): Boolean

}