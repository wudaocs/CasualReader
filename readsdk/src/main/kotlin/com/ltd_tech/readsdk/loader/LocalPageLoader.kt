package com.ltd_tech.readsdk.loader

import com.ltd_tech.core.entities.TxtChapter
import com.ltd_tech.core.utils.CharsetText
import com.ltd_tech.core.utils.DateUtils
import com.ltd_tech.core.utils.MD5
import com.ltd_tech.core.utils.CRScope
import com.ltd_tech.core.utils.close
import com.ltd_tech.core.utils.getCharset
import com.ltd_tech.readsdk.views.PagerView
import com.ltd_tech.readsdk.entities.BookChapterTable
import com.ltd_tech.readsdk.entities.BookEntity
import com.ltd_tech.readsdk.utils.DataControls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.io.RandomAccessFile
import java.nio.charset.Charset
import java.util.regex.Pattern


class LocalPageLoader(pagerView: PagerView, private val bookEntity: BookEntity) :
    BookLoader(pagerView, bookEntity) {

    private val BUFFER_SIZE = 512 * 1024

    //没有标题的时候，每个章节的最大长度
    private val MAX_LENGTH_WITH_NO_CHAPTER = 10 * 1024

    //正则表达式章节匹配模式
    // "(第)([0-9零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟]{1,10})([章节回集卷])(.*)"
    private val CHAPTER_PATTERNS = arrayOf(
        "^(.{0,8})(\u7b2c)([0-9\u96f6\u4e00\u4e8c\u4e24\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u58f9\u8d30\u53c1\u8086\u4f0d\u9646\u67d2\u634c\u7396\u62fe\u4f70\u4edf]{1,10})([\u7ae0\u8282\u56de\u96c6\u5377])(.{0,30})$",
        "^(\\s{0,4})([\\(\u3010\u300a]?(\u5377)?)([0-9\u96f6\u4e00\u4e8c\u4e24\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u58f9\u8d30\u53c1\u8086\u4f0d\u9646\u67d2\u634c\u7396\u62fe\u4f70\u4edf]{1,10})([\\.:\uff1a\u0020\u000c\t])(.{0,30})$",
        "^(\\s{0,4})([\\(\uff08\u3010\u300a])(.{0,30})([\\)\uff09\u3011\u300b])(\\s{0,2})$",
        "^(\\s{0,4})(\u6b63\u6587)(.{0,20})$",
        "^(.{0,4})(Chapter|chapter)(\\s{0,4})([0-9]{1,4})(.{0,30})$"
    )

    //章节解析模式
    private var mChapterPattern: Pattern? = null

    //获取书本的文件
    private var mBookFile: File? = null

    //编码类型
    private var mCharset: CharsetText? = null

    private val scope: CRScope = CRScope()

    init {
        mStatus = STATUS_PARING
    }

    override fun refreshChapterList() {
        // 对于文件是否存在，或者为空的判断，不作处理。 ==> 在文件打开前处理过了。
        mBookFile = File(bookEntity.cover)

        mCharset = getCharset(mBookFile?.absolutePath)

        val lastModified = DateUtils.dateConvertByBookDateFormat(mBookFile?.lastModified() ?: 0)

        // 判断文件是否已经加载过，并具有缓存
        if (!bookEntity.isUpdate() && bookEntity.updated != null && bookEntity.updated.equals(
                lastModified
            ) && bookEntity.bookChapterList.isNotEmpty()
        ) {
            mChapterList = convertTxtChapter(bookEntity.bookChapterList)
            isChapterListPrepare = true
            mPageChangeListener?.onCategoryFinish(mChapterList)

            // 加载并显示当前章节
            openChapter()
            return
        }

        scope.execute {
            withContext(Dispatchers.IO) {
                try {
                    loadChapters()

                    isChapterListPrepare = true
                    withContext(Dispatchers.Main) {
                        // 提示目录加载完成
                        mPageChangeListener?.onCategoryFinish(mChapterList)

                        // 存储章节到数据库
                        val bookChapterBeanList: MutableList<BookChapterTable> = mutableListOf()
                        for (i in 0 until (mChapterList?.size ?: 0)) {
                            val chapter = mChapterList?.get(i)
                            chapter?.run {
                                val bean = BookChapterTable()
                                bean.id = MD5.strToMd5By16(
                                    mBookFile?.absolutePath + File.separator + chapter.title
                                ) // 将路径+i 作为唯一值
                                bean.title = chapter.title
                                bean.start = chapter.start
                                bean.isUnreadble = false
                                bean.end = chapter.end
                                bookChapterBeanList.add(bean)
                            }

                        }
                        bookEntity.bookChapters = bookChapterBeanList
                        bookEntity.updated = lastModified

                        DataControls.saveBookChaptersWithAsync(bookChapterBeanList)
                        DataControls.saveBookEntity(bookEntity)

                        // 加载并显示当前章节
                        openChapter()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    chapterError()
                }
            }
        }


    }

    private fun convertTxtChapter(bookChapters: MutableList<BookChapterTable>): MutableList<TxtChapter> {
        val txtChapters: MutableList<TxtChapter> = ArrayList(bookChapters.size)
        for (bean in bookChapters) {
            val chapter = TxtChapter()
            chapter.title = bean.title
            chapter.start = bean.start
            chapter.end = bean.end
            txtChapters.add(chapter)
        }
        return txtChapters
    }

    /**
     * 1. 检查文件中是否存在章节名
     * 2. 判断文件中使用的章节名类型的正则表达式
     *
     * @return 是否存在章节名
     */
    @Throws(IOException::class)
    private fun checkChapterType(bookStream: RandomAccessFile): Boolean {
        //首先获取128k的数据
        val buffer = ByteArray(BUFFER_SIZE / 4)
        val length = bookStream.read(buffer, 0, buffer.size)
        //进行章节匹配
        for (str in CHAPTER_PATTERNS) {
            val pattern = Pattern.compile(str, Pattern.MULTILINE)
            val matcher =
                pattern.matcher(String(buffer, 0, length, Charset.forName(mCharset?.value)))
            //如果匹配存在，那么就表示当前章节使用这种匹配方式
            if (matcher.find()) {
                mChapterPattern = pattern
                //重置指针位置
                bookStream.seek(0)
                return true
            }
        }

        //重置指针位置
        bookStream.seek(0)
        return false
    }

    /**
     * 未完成的部分:
     * 1. 序章的添加
     * 2. 章节存在的书本的虚拟分章效果
     *
     * @throws IOException
     */
    private fun loadChapters() {
        val chapters: MutableList<TxtChapter> = mutableListOf()
        //获取文件流
        val bookStream = RandomAccessFile(mBookFile, "r")
        //寻找匹配文章标题的正则表达式，判断是否存在章节名
        val hasChapter: Boolean = checkChapterType(bookStream)
        //加载章节
        val buffer = ByteArray(BUFFER_SIZE)
        //获取到的块起始点，在文件中的位置
        var curOffset: Long = 0
        //block的个数
        var blockPos = 0
        //读取的长度
        var length: Int

        //获取文件中的数据到buffer，直到没有数据为止
        while (bookStream.read(buffer, 0, buffer.size).also { length = it } > 0) {
            ++blockPos
            //如果存在Chapter
            if (hasChapter) {
                //将数据转换成String
                val blockContent = String(buffer, 0, length, Charset.forName(mCharset?.value))
                //当前Block下使过的String的指针
                var seekPos = 0
                //进行正则匹配
                val matcher = mChapterPattern?.matcher(blockContent)
                matcher?.run {
                    while (find()) {
                        //获取匹配到的字符在字符串中的起始位置
                        val chapterStart = start()
                        //如果 seekPos == 0 && nextChapterPos != 0 表示当前block处前面有一段内容
                        //第一种情况一定是序章 第二种情况可能是上一个章节的内容
                        if (seekPos == 0 && chapterStart != 0) {
                            //获取当前章节的内容
                            val chapterContent = blockContent.substring(seekPos, chapterStart)
                            //设置指针偏移
                            seekPos += chapterContent.length
                            //如果当前对整个文件的偏移位置为0的话，那么就是序章
                            if (curOffset == 0L) {
                                //创建序章
                                val preChapter = TxtChapter()
                                preChapter.title = "序章"
                                preChapter.start = 0
                                preChapter.end =
                                    chapterContent.toByteArray(Charset.forName(mCharset?.value)).size.toLong()

                                //如果序章大小大于30才添加进去
                                if (preChapter.end - preChapter.start > 30) {
                                    chapters.add(preChapter)
                                }

                                //创建当前章节
                                val curChapter = TxtChapter()
                                curChapter.title = matcher.group()
                                curChapter.start = preChapter.end
                                chapters.add(curChapter)

                            } else {
                                //否则就block分割之后，上一个章节的剩余内容
                                //获取上一章节
                                val lastChapter = chapters[chapters.size - 1]
                                //将当前段落添加上一章去
                                lastChapter.end += chapterContent.toByteArray(
                                    Charset.forName(
                                        mCharset?.value
                                    )
                                ).size.toLong()

                                //如果章节内容太小，则移除
                                if (lastChapter.end - lastChapter.start < 30) {
                                    chapters.remove(lastChapter)
                                }

                                //创建当前章节
                                val curChapter = TxtChapter()
                                curChapter.title = matcher.group()
                                curChapter.start = lastChapter.end
                                chapters.add(curChapter)
                            }

                        } else {
                            //是否存在章节
                            if (chapters.size != 0) {
                                //获取章节内容
                                val chapterContent =
                                    blockContent.substring(seekPos, matcher.start())
                                seekPos += chapterContent.length

                                //获取上一章节
                                val lastChapter = chapters[chapters.size - 1]
                                lastChapter.end = lastChapter.start + chapterContent.toByteArray(
                                    Charset.forName(mCharset?.value)
                                ).size.toLong()

                                //如果章节内容太小，则移除
                                if (lastChapter.end - lastChapter.start < 30) {
                                    chapters.remove(lastChapter)
                                }

                                //创建当前章节
                                val curChapter = TxtChapter()
                                curChapter.title = matcher.group()
                                curChapter.start = lastChapter.end
                                chapters.add(curChapter)
                            } else {
                                val curChapter = TxtChapter()
                                curChapter.title = matcher.group()
                                curChapter.start = 0
                                chapters.add(curChapter)
                            }
                        }
                    }
                }
            } else {
                //进行本地虚拟分章
                //章节在buffer的偏移量
                var chapterOffset = 0
                //当前剩余可分配的长度
                var strLength = length
                //分章的位置
                var chapterPos = 0
                while (strLength > 0) {
                    ++chapterPos
                    //是否长度超过一章
                    if (strLength > MAX_LENGTH_WITH_NO_CHAPTER) {
                        //在buffer中一章的终止点
                        var end = length
                        //寻找换行符作为终止点
                        for (i in chapterOffset + MAX_LENGTH_WITH_NO_CHAPTER until length) {
                            if (buffer[i] == CharsetText.BLANK) {
                                end = i
                                break
                            }
                        }
                        val chapter = TxtChapter()
                        chapter.title = "第" + blockPos + "章" + "(" + chapterPos + ")"
                        chapter.start = curOffset + chapterOffset + 1
                        chapter.end = curOffset + end
                        chapters.add(chapter)
                        //减去已经被分配的长度
                        strLength -= (end - chapterOffset)
                        //设置偏移的位置
                        chapterOffset = end
                    } else {
                        val chapter = TxtChapter()
                        chapter.title = "第" + blockPos + "章" + "(" + chapterPos + ")"
                        chapter.start = curOffset + chapterOffset + 1
                        chapter.end = curOffset + length
                        chapters.add(chapter)
                        strLength = 0
                    }
                }
            }

            //block的偏移点
            curOffset += length.toLong()

            if (hasChapter) {
                //设置上一章的结尾
                val lastChapter = chapters[chapters.size - 1]
                lastChapter.end = curOffset
            }

            //当添加的block太多的时候，执行GC
            if (blockPos % 15 == 0) {
                System.gc()
                System.runFinalization()
            }
        }

        mChapterList = chapters
        close(bookStream)

        System.gc()
        System.runFinalization()
    }


    override fun getChapterReader(chapter: TxtChapter?): BufferedReader? {
        //从文件中获取数据
        val content: ByteArray = getChapterContent(chapter)
        return BufferedReader(InputStreamReader(ByteArrayInputStream(content), mCharset?.value))
    }

    /**
     * 从文件中提取一章的内容
     *
     * @param chapter
     * @return
     */
    private fun getChapterContent(chapter: TxtChapter?): ByteArray {
        if (chapter == null) {
            return ByteArray(0)
        }
        var bookStream: RandomAccessFile? = null
        try {
            bookStream = RandomAccessFile(mBookFile, "r")
            bookStream.seek(chapter.start)
            val extent = (chapter.end - chapter.start).toInt()
            val content = ByteArray(extent)
            bookStream.read(content, 0, extent)
            return content
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            close(bookStream)
        }
        return ByteArray(0)
    }

    override fun hasChapterData(chapter: TxtChapter?): Boolean {
        return true
    }

    override fun saveRecord() {
        super.saveRecord()
        //修改当前bookEntity记录
        if (isChapterListPrepare) {
            //表示当前CollBook已经阅读
            bookEntity.setIsUpdate(false)
            bookEntity.lastChapter = mChapterList?.get(mCurChapterPos)?.title
            bookEntity.lastRead = DateUtils.getCurrentTimeToBook()
            //直接更新
            DataControls.saveBookEntity(bookEntity)
        }
    }
}