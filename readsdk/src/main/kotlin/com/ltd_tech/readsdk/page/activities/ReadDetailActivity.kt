package com.ltd_tech.readsdk.page.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ltd_tech.core.MBaseActivity
import com.ltd_tech.core.broadcasts.BatteryAndTimeTickReceiver
import com.ltd_tech.core.entities.TxtChapter
import com.ltd_tech.core.observe.BrightObserver
import com.ltd_tech.core.utils.BrightnessUtils
import com.ltd_tech.core.utils.DateUtils
import com.ltd_tech.core.utils.L
import com.ltd_tech.core.utils.ResourceUtils
import com.ltd_tech.core.utils.ScreenUtils
import com.ltd_tech.core.utils.StringUtils
import com.ltd_tech.core.utils.SysUtils
import com.ltd_tech.core.utils.gson.JsonUtil
import com.ltd_tech.readsdk.R
import com.ltd_tech.readsdk.consts.KEY_EXTRA_BOOK
import com.ltd_tech.readsdk.consts.KEY_EXTRA_BOOK_IS_LOCAL
import com.ltd_tech.readsdk.consts.REQUEST_MORE_SETTING
import com.ltd_tech.readsdk.consts.RESULT_IS_COLLECTED
import com.ltd_tech.readsdk.databinding.ActivityReadDetailBinding
import com.ltd_tech.readsdk.entities.BookChapterTable
import com.ltd_tech.readsdk.entities.BookEntity
import com.ltd_tech.readsdk.loader.BookLoader
import com.ltd_tech.readsdk.loader.OnBookChangeListener
import com.ltd_tech.readsdk.loader.PageDrawLoader.Companion.STATUS_ERROR
import com.ltd_tech.readsdk.loader.PageDrawLoader.Companion.STATUS_LOADING
import com.ltd_tech.readsdk.page.adapter.BookCategoryAdapter
import com.ltd_tech.readsdk.utils.DataControls
import com.ltd_tech.readsdk.utils.ReadSettingManager
import com.ltd_tech.readsdk.views.ReadSettingDialog
import com.ltd_tech.readsdk.views.TouchListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 阅读详情页面
 * author: Kaos
 * created on 2023/5/12
 */
class ReadDetailActivity : MBaseActivity<ActivityReadDetailBinding, ReadDetailViewModel>() {

    private var mPageLoader: BookLoader? = null

    private var mBrightObserver: BrightObserver? = null

    private var mBookId: String? = null

    // 是否收藏为本地书籍
    private var isCollected = false

    private var mBookEntity: BookEntity? = null

    private var mBookCategoryAdapter: BookCategoryAdapter? = null

    //控制屏幕常亮
    private var mWakeLock: WakeLock? = null

    private lateinit var mSettingDialog: ReadSettingDialog

    /** ---------↓↓↓---------  动画效果  ---------↓↓↓--------- **/
    private var mTopInAnim: Animation? = null
    private var mTopOutAnim: Animation? = null
    private var mBottomInAnim: Animation? = null
    private var mBottomOutAnim: Animation? = null
    private val durationTime = 200L

    /** ---------↑↑↑----------------------------↑↑↑--------- **/


    override fun initArgus() {
        super.initArgus()
        // 接收电池信息和时间更新的广播
        BatteryAndTimeTickReceiver.register(this, { battery ->
            mPageLoader?.updateBattery(battery)
        }, {
            mPageLoader?.updateTime()
        })

    }

    override fun layout() = R.layout.activity_read_detail

    override fun initData() {
        super.initData()

        mBookEntity = intent.getSerializableExtra(KEY_EXTRA_BOOK) as BookEntity?
        isCollected = intent.getBooleanExtra(KEY_EXTRA_BOOK_IS_LOCAL, false)

        mBookId = mBookEntity?._id
    }

    private fun supportActionBar(toolbar: Toolbar?): ActionBar? {
        setSupportActionBar(toolbar)
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
        }
        toolbar?.setNavigationOnClickListener { v -> finish() }
        return actionBar
    }

    override fun bindWidget() {
        // 显示返回按钮
        bind.toolbar.run {
            title = mBookEntity?.title

            supportActionBar(this)
        }

        //半透明化StatusBar
        SysUtils.transparentStatusBar(this)

        //初始化屏幕常亮类
        mWakeLock = (getSystemService(POWER_SERVICE) as PowerManager?)?.newWakeLock(
            PowerManager.SCREEN_DIM_WAKE_LOCK,
            "readSdk:keep_bright"
        )
        bind.pvActivityReadDetail.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        mPageLoader = bind.pvActivityReadDetail.getPageLoader(mBookEntity)

        mSettingDialog = ReadSettingDialog(this, mPageLoader)

        bind.dlActivityReadDetail.run {
            //禁止滑动展示DrawerLayout
            setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            //侧边打开后，返回键能够起作用
            isFocusableInTouchMode = false
        }
        initMenuAnim()
        setListener()
        bind.ablActivityReadDetailMenu.setPadding(0, ScreenUtils.getStatusHeight(this), 0, 0)
        setCategory()
        toggleNightMode()

        //设置当前Activity的Brightness
        if (ReadSettingManager.isBrightnessAuto()) {
            BrightnessUtils.setDefaultBrightness(this)
        } else {
            BrightnessUtils.setBrightness(this, ReadSettingManager.getBrightness())
        }

        //隐藏StatusBar
        bind.pvActivityReadDetail.post {
            hideSystemBar()
        }

        //初始化BottomMenu
        initBottomMenu()

    }

    /**
     * 设置设置功能相关的UI
     */
    private fun setSettingUI() {

    }

    /**
     * 设置目录数据
     */
    private fun setCategory() {
        mBookCategoryAdapter = BookCategoryAdapter(this, mutableListOf()) { pos, item ->
            // item点击事件
            L.il("setCategory", "章节目录 $pos -> ${JsonUtil.toJson(item)}")
            bind.dlActivityReadDetail.closeDrawer(GravityCompat.START)
            mPageLoader?.skipToChapter(pos)
        }
        bind.rvActivityReadDetailCategory.adapter = mBookCategoryAdapter
        bind.rvActivityReadDetailCategory.layoutManager = LinearLayoutManager(this)
    }

    /**
     * 设置各种listener
     */
    private fun setListener() {

        mPageLoader?.setOnPageChangeListener(object : OnBookChangeListener {
            override fun onChapterChange(pos: Int) {
                mBookCategoryAdapter?.setCurrentChapter(pos)
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun requestChapters(requestChapters: MutableList<TxtChapter>) {
                mViewModel?.loadChapter(mBookId, requestChapters) {
                    if (it) {
                        // 成功
                        if (mPageLoader?.getPageStatus() == STATUS_LOADING) {
                            mPageLoader?.openChapter()
                        }
                        // 当完成章节的时候，刷新列表
                        mBookCategoryAdapter?.notifyDataSetChanged()
                    } else {
                        // 失败
                        if (mPageLoader?.getPageStatus() == STATUS_LOADING) {
                            mPageLoader?.chapterError()
                        }
                    }
                }
                smoothScrollToPosition()
            }

            override fun onCategoryFinish(chapters: List<TxtChapter>?) {
                chapters?.forEach {
                    it.title = StringUtils.convertCC(it.title)
                }
                mBookCategoryAdapter?.refreshItems(chapters)
            }

            override fun onPageCountChange(count: Int) {
                bind.sbActivityReadDetailChapterProgress.run {
                    max = 0.coerceAtLeast(count - 1)
                    progress = 0
                    // 如果处于错误状态，那么就冻结使用
                    isEnabled =
                        !(mPageLoader?.getPageStatus() == STATUS_LOADING || mPageLoader?.getPageStatus() == STATUS_ERROR)
                }
            }

            override fun onPageChange(pos: Int) {
                bind.sbActivityReadDetailChapterProgress.post {
                    bind.sbActivityReadDetailChapterProgress.progress = pos
                }
            }
        })

        bind.pvActivityReadDetail.setTouchListener(object : TouchListener {
            override fun onTouch(): Boolean {
                return !hideReadMenu()
            }

            override fun center() {
                toggleMenu(true)
            }

            override fun prePage() {
            }

            override fun nextPage() {
            }

            override fun cancel() {
            }
        })

        bind.tvActivityReadDetailCategory.setOnClickListener {
            // 目录按钮点击
            //移动到指定位置
            if ((mBookCategoryAdapter?.itemCount ?: 0) > 0) {
                smoothScrollToPosition()
            }
            //切换菜单
            toggleMenu(true)
            //打开侧滑动栏
            bind.dlActivityReadDetail.openDrawer(GravityCompat.START)
        }

        bind.tvActivityReadDetailSetting.setOnClickListener {
            // 设置按钮点击
            toggleMenu(false)
            mSettingDialog.show()
        }

        bind.tvActivityReadDetailPreChapter.setOnClickListener {
            // 上一章按钮点击
            if (mPageLoader?.skipPreChapter() == true) {
                mBookCategoryAdapter?.setCurrentChapter(mPageLoader?.getChapterPos())
            }
        }

        bind.tvActivityReadDetailNextChapter.setOnClickListener {
            if (mPageLoader?.skipNextChapter() == true) {
                mBookCategoryAdapter?.setCurrentChapter(mPageLoader?.getChapterPos())
            }
        }

        bind.sbActivityReadDetailChapterProgress.setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //进行切换
                val pagePos: Int = bind.sbActivityReadDetailChapterProgress.progress
                if (pagePos != mPageLoader?.getPagePos()) {
                    mPageLoader?.skipToPage(pagePos)
                }
            }

        })

        bind.tvActivityReadDetailNightMode.setOnClickListener {
            mPageLoader?.updateNightMode(!ReadSettingManager.isNightMode())
            toggleNightMode()
        }

        mSettingDialog.setOnDismissListener {
            hideSystemBar()
        }

    }

    /**
     * 滚动到指定位置
     */
    private fun smoothScrollToPosition() {
        lifecycleScope.launch(Dispatchers.Main) {
            bind.rvActivityReadDetailCategory.smoothScrollToPosition(
                mPageLoader?.getChapterPos() ?: 0
            )
        }
    }

    /**
     * 切换亮度设置按钮
     */
    private fun toggleNightMode() {
        bind.run {
            if (ReadSettingManager.isNightMode()) {
                tvActivityReadDetailNightMode.text =
                    ResourceUtils.getString(R.string.nb_mode_morning)
                val drawable = ContextCompat.getDrawable(
                    this@ReadDetailActivity,
                    R.drawable.ic_read_menu_morning
                )
                tvActivityReadDetailNightMode.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    drawable,
                    null,
                    null
                )
            } else {
                tvActivityReadDetailNightMode.text = ResourceUtils.getString(R.string.nb_mode_night)
                val drawable = ContextCompat.getDrawable(
                    this@ReadDetailActivity,
                    R.drawable.ic_read_menu_night
                )
                tvActivityReadDetailNightMode.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    drawable,
                    null,
                    null
                )
            }
        }

    }

    private fun initBottomMenu() {
        bind.run {
            //判断是否全屏
            if (ReadSettingManager.isFullScreen()) {
                //还需要设置mBottomMenu的底部高度
                val params = llActivityReadDetailBottomMenu.layoutParams as MarginLayoutParams
                params.bottomMargin = ScreenUtils.getNavigationBarHeight()
                llActivityReadDetailBottomMenu.layoutParams = params
            } else {
                //设置mBottomMenu的底部距离
                val params = llActivityReadDetailBottomMenu.layoutParams as MarginLayoutParams
                params.bottomMargin = 0
                llActivityReadDetailBottomMenu.layoutParams = params
            }
        }

    }


    override fun loadData() {
        super.loadData()
        // 如果是已经收藏的，那么就从数据库中获取目录
        if (isCollected) {
            // 根据书籍id获取章节信息
            DataControls.getBookChaptersInRx(mBookId) {
                it?.run {
                    // 刷新章节列表
                    mPageLoader?.refreshBookChapters(this)
                    // 如果是网络小说并被标记更新的，则从网络下载目录
                    if (mBookEntity?.isUpdate() == true && mBookEntity?.isLocal() == false) {
                        loadCategory()
                    }
                }
            }
        } else {
            loadCategory()
        }
    }

    private fun loadCategory() {
        // 本地不存在则请求网络获取
        mViewModel?.loadCategory(mBookId) {
            showCategory(it)
        }
    }

    /**
     * 显示章节信息
     */
    private fun showCategory(bookChapters: MutableList<BookChapterTable>?) {
        if (bookChapters == null) {
            return
        }
        mPageLoader?.refreshBookChapters(bookChapters)
        // 如果是目录更新的情况，那么就需要存储更新数据
        if (mBookEntity?.isUpdate() == true && isCollected) {
            DataControls.saveBookChaptersWithAsync(bookChapters)
        }
    }

    override fun onStart() {
        super.onStart()
        if (mBrightObserver == null) {
            mBrightObserver = BrightObserver(Handler(Looper.getMainLooper()))
        }
        // 亮度调节监听
        mBrightObserver?.registerBrightObserver(this)
    }

    override fun onResume() {
        super.onResume()
        mWakeLock?.acquire(3 * 60 * 60 * 1000L /*180 minutes*/)
    }

    override fun onPause() {
        super.onPause()
        mWakeLock?.release()
        if (isCollected) {
            mPageLoader?.saveRecord()
        }
    }

    override fun onStop() {
        super.onStop()
        mBrightObserver?.unregisterBrightObserver(this)
    }

    /**
     * 退出并返回当前书籍状态
     */
    private fun exit() {
        // 返回给上一个页面
        val result = Intent()
        result.putExtra(RESULT_IS_COLLECTED, isCollected)
        setResult(RESULT_OK, result)
        // 退出
        // 退出
        super.onBackPressed()
    }


    override fun onDestroy() {
        super.onDestroy()
        BatteryAndTimeTickReceiver.unregister(this)
        mPageLoader?.closeBook()
        mPageLoader = null
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val isVolumeTurnPage: Boolean = ReadSettingManager.isVolumeTurnPage()
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> if (isVolumeTurnPage) {
                return mPageLoader?.skipToPrePage() ?: super.onKeyDown(keyCode, event)
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> if (isVolumeTurnPage) {
                return mPageLoader?.skipToNextPage() ?: super.onKeyDown(keyCode, event)
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        if (bind.ablActivityReadDetailMenu.visibility == View.VISIBLE) {
            // 非全屏下才收缩，全屏下直接退出
            if (!ReadSettingManager.isFullScreen()) {
                toggleMenu(true)
            }
            // 退出
            super.onBackPressed()
        } else if (mSettingDialog.isShowing) {
            mSettingDialog.dismiss()
        } else if (bind.dlActivityReadDetail.isDrawerOpen(GravityCompat.START)) {
            bind.dlActivityReadDetail.closeDrawer(GravityCompat.START)
        } else {
            if ((mBookEntity?.isLocal() == false) &&
                !isCollected && mBookEntity?.bookChapterList?.isNotEmpty() == true
            ) {
                val alertDialog: AlertDialog = AlertDialog.Builder(this)
                    .setTitle("加入书架")
                    .setMessage("喜欢本书就加入书架吧")
                    .setPositiveButton("确定") { _, _ ->
                        //设置为已收藏
                        isCollected = true
                        //设置阅读时间
                        mBookEntity?.lastRead = DateUtils.getCurrentTimeToBook()
                        DataControls.saveBookWithAsync(mBookEntity)
                        exit()
                    }
                    .setNegativeButton("取消") { _, _ -> exit() }.create()
                alertDialog.show()
            } else {
                exit()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        SysUtils.hideStableStatusBar(this)
        if (requestCode == REQUEST_MORE_SETTING) {
            // 设置显示状态
            if (ReadSettingManager.isFullScreen()) {
                SysUtils.hideStableNavBar(this)
            } else {
                SysUtils.showStableNavBar(this)
            }
        }
    }

    /** ---------↓↓↓---------  UI效果相关  ---------↓↓↓--------- **/

    /**
     * 隐藏阅读界面的菜单显示
     *
     * @return 是否隐藏成功
     */
    private fun hideReadMenu(): Boolean {
        hideSystemBar()
        if (bind.ablActivityReadDetailMenu.visibility == View.VISIBLE) {
            toggleMenu(true)
            return true
        } else if (mSettingDialog.isShowing) {
            mSettingDialog.dismiss()
            return true
        }
        return false
    }

    private fun showSystemBar() {
        //显示
        SysUtils.showUnStableStatusBar(this)
        if (ReadSettingManager.isFullScreen()) {
            SysUtils.showUnStableNavBar(this)
        }
    }

    private fun hideSystemBar() {
        //隐藏
        SysUtils.hideStableStatusBar(this)
        if (ReadSettingManager.isFullScreen()) {
            SysUtils.hideStableNavBar(this)
        }
    }

    /**
     * 切换菜单栏的可视状态
     * 默认是隐藏的
     */
    private fun toggleMenu(hideStatusBar: Boolean) {
        bind.run {
            if (ablActivityReadDetailMenu.visibility == View.VISIBLE) {
                //关闭
                ablActivityReadDetailMenu.startAnimation(mTopOutAnim)
                llActivityReadDetailBottomMenu.startAnimation(mBottomOutAnim)
                ablActivityReadDetailMenu.visibility = View.GONE
                llActivityReadDetailBottomMenu.visibility = View.GONE
                if (hideStatusBar) {
                    hideSystemBar()
                }
            } else {
                bind.ablActivityReadDetailMenu.visibility = View.VISIBLE
                llActivityReadDetailBottomMenu.visibility = View.VISIBLE
                ablActivityReadDetailMenu.startAnimation(mTopInAnim)
                llActivityReadDetailBottomMenu.startAnimation(mBottomInAnim)
                showSystemBar()
            }
        }

    }

    /**
     * 初始化菜单动画
     */
    private fun initMenuAnim() {
        if (mTopInAnim != null) return
        mTopInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_in)
        mTopOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_out)
        mBottomInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_in)
        mBottomOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_out)
        //退出的速度要快
        mTopOutAnim?.duration = durationTime
        mBottomOutAnim?.duration = durationTime
    }
}