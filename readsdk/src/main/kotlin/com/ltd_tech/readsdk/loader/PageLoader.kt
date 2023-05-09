package com.ltd_tech.readsdk.loader

import com.ltd_tech.core.entities.TxtChapter
import com.ltd_tech.core.entities.TxtPage
import com.ltd_tech.core.utils.F
import com.ltd_tech.core.utils.StringUtils
import com.ltd_tech.core.widgets.pager.PagerView
import com.ltd_tech.readsdk.entities.BookEntity
import com.ltd_tech.readsdk.utils.DataControls
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException

/**
 * 内容加载器
 */
abstract class PageLoader(private val mPageView: PagerView, private val mBookEntity: BookEntity) :
    PageDrawLoader(mPageView, mBookEntity) {

    // 被遮盖的页，或者认为被取消显示的页
    private var mCancelPage: TxtPage? = null

    /**
     * 设置页面切换监听
     * @param listener
     */
    fun setOnPageChangeListener(listener: OnPageChangeListener) {
        mPageChangeListener = listener

        // 如果目录加载完之后才设置监听器，那么会默认回调
        if (isChapterListPrepare) {
            mPageChangeListener?.onCategoryFinish(mChapterList)
        }
    }

    // 是否打开过章节
    private var isChapterOpen = false
    private var isFirstOpen = true
    private val isClose = false

    private var coroutinesScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun prepareDisplay(width: Int, height: Int) {
        // 获取PageView的宽高
        mDisplayWidth = width
        mDisplayHeight = height

        // 获取内容显示位置的大小
        mVisibleWidth = mDisplayWidth - mMarginWidth * 2
        mVisibleHeight = mDisplayHeight - mMarginHeight * 2

        // 重置 PageMode
        mPageView.setPageMode(mPageMode)

        if (!isChapterOpen) {
            // 展示加载界面
            mPageView.drawCurPage(false)
            // 如果在 display 之前调用过 openChapter 肯定是无法打开的。
            // 所以需要通过 display 再重新调用一次。
            if (!isFirstOpen) {
                // 打开书籍
                openChapter()
            }
        } else {
            // 如果章节已显示，那么就重新计算页面
            if (mStatus == STATUS_FINISH) {
                dealLoadPageList(mCurChapterPos)
                // 重新设置文章指针的位置
                mCurPage = getCurPage(mCurPage?.position ?: 0)
            }
            mPageView.drawCurPage(false)
        }
    }

    /**
     * 打开指定章节
     */
    fun openChapter() {
        isFirstOpen = false
        if (!mPageView.isPrepare()) {
            return
        }

        // 如果章节目录没有准备好
        if (!isChapterListPrepare) {
            mStatus = STATUS_LOADING
            mPageView.drawCurPage(false)
            return
        }

        // 如果获取到的章节目录为空
        if (mChapterList!!.isEmpty()) {
            mStatus = STATUS_CATEGORY_EMPTY
            mPageView.drawCurPage(false)
            return
        }
        if (parseCurChapter()) {
            // 如果章节从未打开
            if (!isChapterOpen) {
                var position: Int = mReadTable?.pagePos ?: 0

                // 防止记录页的页号，大于当前最大页号
                val size = mCurPageList?.size ?: 0
                if (position >= size) {
                    position = size - 1
                }
                mCurPage = getCurPage(position)
                mCancelPage = mCurPage
                // 切换状态
                isChapterOpen = true
            } else {
                mCurPage = getCurPage(0)
            }
        } else {
            mCurPage = TxtPage()
        }
        mPageView.drawCurPage(false)
    }

    /**
     * 翻阅上一页
     */
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
        mCurPage = if (parsePrevChapter()) {
            getPrevLastPage()
        } else {
            TxtPage()
        }
        mPageView.drawNextPage()
        return true
    }

    /**
     * 判断是否有上一章节
     */
    private fun hasPrevChapter(): Boolean {
        //判断是否上一章节为空
        return mCurChapterPos - 1 >= 0
    }

    /**
     * 解析上一章数据
     *
     * @return:数据是否解析成功
     */
    private fun parsePrevChapter(): Boolean {
        // 加载上一章数据
        val prevChapter = mCurChapterPos - 1
        mLastChapterPos = mCurChapterPos
        mCurChapterPos = prevChapter

        // 当前章缓存为下一章
        mNextPageList = mCurPageList

        // 判断是否具有上一章缓存
        if (mPrePageList != null) {
            mCurPageList = mPrePageList
            mPrePageList = null

            // 回调
            chapterChangeCallback()
        } else {
            dealLoadPageList(prevChapter)
        }
        return mCurPageList != null
    }

    /**
     * 解析当前章节
     */
    fun parseCurChapter(): Boolean {
        // 解析数据
        dealLoadPageList(mCurChapterPos)
        // 预加载下一页面
        preLoadNextChapter()
        return mCurPageList != null
    }

    private fun dealLoadPageList(chapterPos: Int) {
        try {
            mCurPageList = loadPageList(chapterPos)
            if (mCurPageList != null) {
                if (mCurPageList?.isEmpty() == true) {
                    mStatus = STATUS_EMPTY

                    // 添加一个空数据
                    val page = TxtPage()
                    page.lines = ArrayList(1)
                    mCurPageList?.add(page)
                } else {
                    mStatus = STATUS_FINISH
                }
            } else {
                mStatus = STATUS_LOADING
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mCurPageList = null
            mStatus = STATUS_ERROR
        }

        // 回调
        chapterChangeCallback()
    }

    /**
     * 加载页面列表
     *
     * @param chapterPos:章节序号
     */
    private fun loadPageList(chapterPos: Int): MutableList<TxtPage>? = try {
        // 获取章节
        val chapter = mChapterList?.get(chapterPos)
        // 判断章节是否存在
        if (hasChapterData(chapter)) {
            // 获取章节的文本流
            loadPages(chapter, getChapterReader(chapter))
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }


    private fun chapterChangeCallback() {
        mPageChangeListener?.onChapterChange(mCurChapterPos)
        mPageChangeListener?.onPageCountChange(mCurPageList?.size ?: 0)
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

    /**
     * 解析下一章数据
     *
     * @return:返回解析成功还是失败
     */
    open fun parseNextChapter(): Boolean {
        val nextChapter = mCurChapterPos + 1
        mLastChapterPos = mCurChapterPos
        mCurChapterPos = nextChapter

        // 将当前章的页面列表，作为上一章缓存
        mPrePageList = mCurPageList

        // 是否下一章数据已经预加载了
        if (mNextPageList != null) {
            mCurPageList = mNextPageList
            mNextPageList = null
            // 回调
            chapterChangeCallback()
        } else {
            // 处理页面解析
            dealLoadPageList(nextChapter)
        }
        // 预加载下一页面
        preLoadNextChapter()
        return mCurPageList != null
    }

    /**
     * 取消翻页
     */
    fun pageCancel() {
        if (mCurPage?.position == 0 && mCurChapterPos > mLastChapterPos) { // 加载到下一章取消了
            if (mPrePageList != null) {
                cancelNextChapter()
            } else {
                mCurPage = if (parsePrevChapter()) {
                    getPrevLastPage()
                } else {
                    TxtPage()
                }
            }
        } else if (mCurPageList == null || (mCurPage?.position == ((mCurPageList?.size ?: 0) - 1)
                    && mCurChapterPos < mLastChapterPos)
        ) {  // 加载上一章取消了
            if (mNextPageList != null) {
                cancelPreChapter()
            } else {
                mCurPage = if (parseNextChapter()) {
                    mCurPageList!![0]
                } else {
                    TxtPage()
                }
            }
        } else {
            // 假设加载到下一页，又取消了。那么需要重新装载。
            mCurPage = mCancelPage
        }
    }

    /**
     * 取消上一章
     */
    private fun cancelPreChapter() {
        // 重置位置点
        val temp = mLastChapterPos
        mLastChapterPos = mCurChapterPos
        mCurChapterPos = temp
        // 重置页面列表
        mPrePageList = mCurPageList
        mCurPageList = mNextPageList
        mNextPageList = null
        chapterChangeCallback()
        mCurPage = getCurPage(0)
        mCancelPage = null
    }

    /**
     * 取消下一章
     */
    private fun cancelNextChapter() {
        val temp = mLastChapterPos
        mLastChapterPos = mCurChapterPos
        mCurChapterPos = temp
        mNextPageList = mCurPageList
        mCurPageList = mPrePageList
        mPrePageList = null
        chapterChangeCallback()
        mCurPage = getPrevLastPage()
        mCancelPage = null
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
    private fun loadPages(chapter: TxtChapter?, br: BufferedReader?): MutableList<TxtPage> {
        //生成的页面
        val pages = mutableListOf<TxtPage>()
        if (chapter == null || br == null) {
            // 数据错误直接返回空列表
            return pages
        }
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
                    while (isNotEmpty()) {
                        //当前空间，是否容得下一行文字
                        rHeight -= if (showTitle) {
                            getTitlePaintTextSize().toInt()
                        } else {
                            getTextPaintTextSize().toInt()
                        }
                        // 一页已经填充满了，创建 TextPage
                        if (rHeight <= 0) {
                            // 创建Page
                            val page = TxtPage()
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
                            breakTextTitlePaint(paragraph)
                        } else {
                            breakTextTextPaint(paragraph)
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
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            F().close(br)
        }
        return pages;
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
        if (!hasNextChapter() || !hasChapterData(mChapterList?.get(nextChapter))
        ) {
            return
        }
        //如果之前正在加载则取消
        coroutinesScope.cancel()

        coroutinesScope.launch {
            //调用异步进行预加载加载
            mNextPageList = loadPageList(nextChapter)
        }

    }

    /**
     * 判断是否存在下一章
     */
    private fun hasNextChapter(): Boolean {
        // 判断是否到达目录最后一章
        return mCurChapterPos + 1 < ((mChapterList?.size) ?: 0)
    }

    /**
     * 判断章节是否打开
     */
    open fun isChapterOpen() = isChapterOpen

    /**
     * 保存阅读记录
     */
    open fun saveRecord() {
        if (mChapterList.isNullOrEmpty()) {
            return
        }
        mReadTable?.bookId = mBookEntity._id
        mReadTable?.chapter = mCurChapterPos
        mReadTable?.pagePos = mCurPage?.position ?: 0

        //存储到数据库
        DataControls.saveBookRecord(mReadTable)
    }


    /**
     * 设置文字相关参数（设置弹窗调用）
     *
     * @param textSize 显示字体大小
     */
    open fun setOrUpdateTextSize(textSize: Int) {
        setTextSize(textSize)
        // 如果当前已经显示数据
        if (isChapterListPrepare && mStatus == STATUS_FINISH) {
            // 重新计算当前页面
            dealLoadPageList(mCurChapterPos)

            // 防止在最后一页，通过修改字体，以至于页面数减少导致崩溃的问题

            mCurPage?.run {
                val pageListSize = mCurPageList?.size ?: 0
                if (position >= pageListSize && pageListSize != 0) {
                    position = pageListSize - 1
                }
                // 重新获取指定页面
                mCurPage = mCurPageList?.get(position)
            }

        }
        mPageView.drawCurPage(false)
    }

    /**
     * 跳转到上一章
     */
    open fun skipPreChapter(): Boolean {
        if (!hasPrevChapter()) {
            return false
        }
        // 载入上一章。
        mCurPage = if (parsePrevChapter()) {
            getCurPage(0)
        } else {
            TxtPage()
        }
        mPageView.drawCurPage(false)
        return true
    }

    /**
     * 跳转到下一章
     */
    open fun skipNextChapter(): Boolean {
        if (!hasNextChapter()) {
            return false
        }
        //判断是否达到章节的终止点
        mCurPage = if (parseNextChapter()) {
            getCurPage(0)
        } else {
            TxtPage()
        }
        mPageView.drawCurPage(false)
        return true
    }

    /****************************** public method */
    /**
     * 跳转到指定章节
     *
     * @param pos:从 0 开始。
     */
    open fun skipToChapter(pos: Int) {
        // 设置参数
        mCurChapterPos = pos

        // 将上一章的缓存设置为null
        mPrePageList = null
        // 如果当前下一章缓存正在执行，则取消
        coroutinesScope.cancel()
        // 将下一章缓存设置为null
        mNextPageList = null

        // 打开指定章节
        openChapter()
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