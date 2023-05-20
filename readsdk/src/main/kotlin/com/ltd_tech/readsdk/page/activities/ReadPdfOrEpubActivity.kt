package com.ltd_tech.readsdk.page.activities

import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.artifex.mupdf.viewer.MuPDFCore
import com.artifex.mupdf.viewer.OutlineItem
import com.artifex.mupdf.viewer.PageAdapter
import com.artifex.mupdf.viewer.PageView
import com.artifex.mupdf.viewer.ReaderView
import com.artifex.mupdf.viewer.ReaderView.ViewMapper
import com.artifex.mupdf.viewer.SearchTask
import com.artifex.mupdf.viewer.SearchTaskResult
import com.ltd_tech.core.BaseViewModel
import com.ltd_tech.core.MBaseActivity
import com.ltd_tech.core.utils.KeyBoardUtils
import com.ltd_tech.core.utils.ScreenUtils
import com.ltd_tech.core.utils.storage.SdkKV
import com.ltd_tech.readsdk.R
import com.ltd_tech.readsdk.databinding.ActivityReadPdfOrEpubBinding
import com.artifex.mupdf.DocumentJavaCreator
import java.util.Locale

/**
 * 阅读pdf或者epub文件格式页面
 * author: Kaos
 * created on 2023/5/19
 */
class ReadPdfOrEpubActivity : MBaseActivity<ActivityReadPdfOrEpubBinding, BaseViewModel>() {

    override fun layout(): Any = R.layout.activity_read_pdf_or_epub

    internal enum class TopBarMode {
        Main, Search, More
    }

    private var mTopBarMode = TopBarMode.Main

    private var mDocTitle: String? = null
    private var mDocKey: String? = null

    private var mReturnToLibraryActivity = false

    private var mSearchTask: SearchTask? = null

    // 章节列表返回码
    private val OUTLINE_REQUEST = 1666

    private var mPageSliderRes = 0

    private var mFlatOutline: ArrayList<OutlineItem>? = null

    private var mLayoutEM = 10
    private var mLayoutW = 312
    private var mLayoutH = 504

    private var mLinkHighlight = false

    private lateinit var mDocumentParser : DocumentJavaCreator

    /** ---------↑↑↑------------------------↑↑↑--------- **/

    private var mMuPDFCore: MuPDFCore? = null

    private var mDocView: ReaderView? = null

    private var mPasswordView: EditText? = null

    private var mLayoutPopupMenu: PopupMenu? = null

    private var mButtonsVisible = false

    private val mAlertBuilder: AlertDialog.Builder by lazy {
        AlertDialog.Builder(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (mMuPDFCore == null) {
            if (savedInstanceState != null && savedInstanceState.containsKey("DocTitle")) {
                mDocTitle = savedInstanceState.getString("DocTitle")
            }
            mReturnToLibraryActivity =
                intent.getIntExtra(componentName.packageName + ".ReturnToLibraryActivity", 0) != 0

        }
        mDocumentParser = DocumentJavaCreator()
        super.onCreate(savedInstanceState)

    }

    override fun initArgus() {
        super.initArgus()
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun initData() {
        super.initData()
        checkFileExist()
        if (mMuPDFCore == null) {
            return
        } else {
            if (mMuPDFCore?.needsPassword() == true) {
                requestPassword()
                return
            }
        }

        if (mMuPDFCore?.countPages() == 0) {
            mMuPDFCore = null
        }
        if (mMuPDFCore == null) {
            showCannotOpenDoc()
        }
    }

    private fun checkFileExist() {
        if (Intent.ACTION_VIEW == intent.action) {
            val uri = intent.data
            var mimetype = intent.type
            if (uri == null) {
                showCannotOpenDocByReason("No document uri to open")
                return
            }
            mDocKey = uri.toString()

            mDocTitle = null
            var size: Long = -1
            var cursor: Cursor? = null
            try {
                cursor = contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    var idx: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0 && cursor.getType(idx) == Cursor.FIELD_TYPE_STRING) {
                        mDocTitle = cursor.getString(idx)
                    }
                    idx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (idx >= 0 && cursor.getType(idx) == Cursor.FIELD_TYPE_INTEGER) {
                        size = cursor.getLong(idx)
                    }
                    if (size == 0L) {
                        size = -1L
                    }
                }
            } catch (x: Exception) {
                x.printStackTrace()
            } finally {
                cursor?.close()
            }

            if (mimetype == null || mimetype == "application/octet-stream") {
                mimetype = contentResolver.getType(uri)
            }
            if (mimetype == null || mimetype == "application/octet-stream") {
                mimetype = mDocTitle
            }
            try {
                mMuPDFCore =
                    mDocumentParser.openCore(this@ReadPdfOrEpubActivity, uri, size, mimetype ?: "")
                SearchTaskResult.set(null)
            } catch (x: java.lang.Exception) {
                showCannotOpenDocByReason(x.toString())
                return
            }
        }
    }

    /**
     * 如果需要输入密码则弹出密码弹窗
     */
    private fun requestPassword() {
        mPasswordView = EditText(this).apply {
            inputType = EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
            transformationMethod = PasswordTransformationMethod()
        }

        val alert = mAlertBuilder.create()
        alert.setTitle(R.string.enter_password)
        alert.setView(mPasswordView)
        alert.setButton(
            AlertDialog.BUTTON_POSITIVE, getString(R.string.okay)
        ) { _, _ ->
            if (mMuPDFCore?.authenticatePassword(mPasswordView?.text?.toString() ?: "") == true) {
                createUI()
            } else {
                requestPassword()
            }
        }
        alert.setButton(
            AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel)
        ) { _, _ -> finish() }
        alert.show()
    }


    private fun getCountPages() = mMuPDFCore?.countPages() ?: 0

    override fun bindWidget() {
        createUI()
    }

    /**
     * 创建UI
     */
    private fun createUI() {
        if (mMuPDFCore == null) {
            return
        }

        // Set up the page slider
        val sMax = (getCountPages() - 1).coerceAtLeast(1)
        mPageSliderRes = (10 + sMax - 1) / sMax * 2

        createDocView()
        bind.switcher.visibility = View.INVISIBLE
        bind.pageNumber.visibility = View.INVISIBLE
        bind.pageSlider.visibility = View.INVISIBLE

        createSearch()
        // Activate the seekbar
        bind.pageSlider.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mDocView?.pushHistory()
                mDocView?.displayedViewIndex =
                    (seekBar.progress + mPageSliderRes / 2) / mPageSliderRes
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int, fromUser: Boolean
            ) {
                updatePageNumView((progress + mPageSliderRes / 2) / mPageSliderRes)
            }
        })
        createLink()
        if (mMuPDFCore?.hasOutline() == true) {
            // TODO 如果有章节列表 则显示 如果没有则隐藏
            bind.outlineButton.setOnClickListener {
                if (mFlatOutline == null) {
                    mFlatOutline = mMuPDFCore?.outline
                }
            }
        } else {
            bind.outlineButton.visibility = View.GONE
        }

        mDocView?.displayedViewIndex = SdkKV.getInt("page$mDocKey", 0)

        if (intent.getBooleanExtra("ButtonsHidden", false)) {
            showButtons()
        }
        if (intent.getBooleanExtra("SearchMode", false)) {
            searchModeOn()
        }
        // Set the file-name text
        mMuPDFCore?.title?.run { bind.docNameText.text = this } ?: kotlin.run {
            bind.docNameText.text = (mDocTitle ?: "")
        }

        bind.rlActivityReadPdfOrEpubRoot.run {
            addView(mDocView)
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mDocKey != null && mDocView != null) {
            if (mDocTitle != null) outState.putString("DocTitle", mDocTitle)
            SdkKV.setInt("page$mDocKey", mDocView?.displayedViewIndex ?: 0)
        }
        if (!mButtonsVisible) outState.putBoolean("ButtonsHidden", true)
        if (mTopBarMode == TopBarMode.Search) outState.putBoolean("SearchMode", true)
    }

    override fun onPause() {
        super.onPause()
        mSearchTask?.stop()
        if (mDocKey != null && mDocView != null) {
            SdkKV.setInt("page$mDocKey", mDocView?.displayedViewIndex ?: 0)
        }
    }

    override fun onBackPressed() {
        if (mDocView == null || mDocView?.popHistory() == false) {
            super.onBackPressed()
            if (mReturnToLibraryActivity) {
                val intent = packageManager.getLaunchIntentForPackage(componentName.packageName)
                startActivity(intent)
            }
        }
    }

    override fun onDestroy() {
        mDocView?.applyToChildren(object : ViewMapper() {
            fun applyToView(view: View) {
                (view as PageView).releaseBitmaps()
            }
        })
        mMuPDFCore?.onDestroy()
        mMuPDFCore = null
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            OUTLINE_REQUEST -> if (resultCode >= RESULT_FIRST_USER) {
                mDocView?.pushHistory()
                mDocView?.displayedViewIndex = resultCode - RESULT_FIRST_USER
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /** ---------↓↓↓---------  UI  ---------↓↓↓--------- **/
    private fun createDocView() {
        // First create the document view
        mDocView = object : ReaderView(this) {
            override fun onMoveToChild(i: Int) {
                if (mMuPDFCore == null) return
                bind.pageNumber.text = String.format(
                    Locale.ROOT, "%d / %d", i + 1, mMuPDFCore?.countPages()
                )
                bind.pageSlider.max = (getCountPages() - 1) * mPageSliderRes
                bind.pageSlider.progress = i * mPageSliderRes
                super.onMoveToChild(i)
            }

            override fun onTapMainDocArea() {
                if (!mButtonsVisible) {
                    showButtons()
                } else {
                    if (mTopBarMode === TopBarMode.Main) {
                        hideButtons()
                    }
                }
            }

            override fun onDocMotion() {
                hideButtons()
            }

            override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
                if (mMuPDFCore?.isReflowable == true) {
                    mLayoutW = w * 72 / ScreenUtils.densityDpi
                    mLayoutH = h * 72 / ScreenUtils.densityDpi
                    relayoutDocument()
                } else {
                    refresh()
                }
            }
        }
        mDocView?.adapter = PageAdapter(this, mMuPDFCore)
    }

    private fun createSearch() {
        mSearchTask = object : SearchTask(this, mMuPDFCore) {
            override fun onTextFound(result: SearchTaskResult) {
                SearchTaskResult.set(result)
                // Ask the ReaderView to move to the resulting page
                mDocView?.displayedViewIndex = result.pageNumber
                // Make the ReaderView act on the change to SearchTaskResult
                // via overridden onChildSetup method.
                mDocView?.resetupChildren()
            }
        }
        // Activate the search-preparing button
        bind.searchButton.setOnClickListener { searchModeOn() }
        bind.searchClose.setOnClickListener { searchModeOff() }

        // Search invoking buttons are disabled while there is no text specified
        setButtonEnabled(bind.searchBack, false)
        setButtonEnabled(bind.searchForward, false)

        // React to interaction with the text widget
        bind.searchText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                val haveText = s.toString().isNotEmpty()
                setButtonEnabled(bind.searchBack, haveText)
                setButtonEnabled(bind.searchForward, haveText)

                // Remove any previous search results
                if (SearchTaskResult.get() != null && bind.searchText.text.toString() != SearchTaskResult.get().txt) {
                    SearchTaskResult.set(null)
                    mDocView?.resetupChildren()
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int
            ) {
            }
        })

        //React to Done button on keyboard
        bind.searchText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) search(1)
            false
        }

        bind.searchText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) search(1)
            false
        }

        // Activate search invoking buttons
        bind.searchBack.setOnClickListener { search(-1) }
        bind.searchForward.setOnClickListener { search(1) }
    }

    private fun createLink() {
        bind.linkButton.setOnClickListener { setLinkHighlight(!mLinkHighlight) }
        if (mMuPDFCore?.isReflowable == true) {
            bind.layoutButton.visibility = View.VISIBLE
            mLayoutPopupMenu = PopupMenu(this, bind.layoutButton)
            mLayoutPopupMenu?.menuInflater?.inflate(R.menu.layout_menu, mLayoutPopupMenu?.menu)
            mLayoutPopupMenu?.setOnMenuItemClickListener { item ->
                val oldLayoutEM = mLayoutEM
                when (item.itemId) {
                    R.id.action_layout_6pt -> mLayoutEM = 6
                    R.id.action_layout_7pt -> mLayoutEM = 7
                    R.id.action_layout_8pt -> mLayoutEM = 8
                    R.id.action_layout_9pt -> mLayoutEM = 9
                    R.id.action_layout_10pt -> mLayoutEM = 10
                    R.id.action_layout_11pt -> mLayoutEM = 11
                    R.id.action_layout_12pt -> mLayoutEM = 12
                    R.id.action_layout_13pt -> mLayoutEM = 13
                    R.id.action_layout_14pt -> mLayoutEM = 14
                    R.id.action_layout_15pt -> mLayoutEM = 15
                    R.id.action_layout_16pt -> mLayoutEM = 16
                }
                if (oldLayoutEM != mLayoutEM) relayoutDocument()
                true
            }
            bind.layoutButton.setOnClickListener { mLayoutPopupMenu?.show() }
        }
    }

    private fun setButtonEnabled(button: ImageButton, enabled: Boolean) {
        button.isEnabled = enabled
        button.setColorFilter(
            if (enabled) Color.argb(255, 255, 255, 255) else Color.argb(
                255, 128, 128, 128
            )
        )
    }

    private fun showButtons() {
        if (mMuPDFCore == null) return
        if (!mButtonsVisible) {
            mButtonsVisible = true
            // Update page number text and slider
            val index = mDocView?.displayedViewIndex ?: 0
            updatePageNumView(index)
            bind.pageSlider.max = (getCountPages() - 1) * mPageSliderRes
            bind.pageSlider.progress = index * mPageSliderRes
            if (mTopBarMode === TopBarMode.Search) {
                bind.searchText.requestFocus()
                KeyBoardUtils.openKeyboard(bind.searchText)
            }
            var anim: Animation = TranslateAnimation(0f, 0f, -bind.switcher.height.toFloat(), 0f)
            anim.duration = 200
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    bind.switcher.visibility = View.VISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {}
            })
            bind.switcher.startAnimation(anim)
            anim = TranslateAnimation(0f, 0f, bind.pageSlider.height.toFloat(), 0f)
            anim.setDuration(200)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    bind.pageSlider.visibility = View.VISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    bind.pageNumber.visibility = View.VISIBLE
                }
            })
            bind.pageSlider.startAnimation(anim)
        }
    }

    private fun updatePageNumView(index: Int) {
        if (mMuPDFCore == null) return
        bind.pageNumber.text = java.lang.String.format(
            Locale.ROOT, "%d / %d", index + 1, getCountPages()
        )
    }

    private fun hideButtons() {
        if (mButtonsVisible) {
            mButtonsVisible = false
            KeyBoardUtils.closeKeyboard(bind.searchText)
            var anim: Animation = TranslateAnimation(0f, 0f, 0f, -bind.switcher.height.toFloat())
            anim.duration = 200
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    bind.switcher.visibility = View.INVISIBLE
                }
            })
            bind.switcher.startAnimation(anim)
            anim = TranslateAnimation(0f, 0f, 0f, bind.pageSlider.height.toFloat())
            anim.setDuration(200)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    bind.pageNumber.visibility = View.INVISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    bind.pageSlider.visibility = View.INVISIBLE
                }
            })
            bind.pageSlider.startAnimation(anim)
        }
    }

    private fun relayoutDocument() {
        mDocView?.run {
            val loc: Int = mMuPDFCore?.layout(mCurrent, mLayoutW, mLayoutH, mLayoutEM) ?: 0
            mFlatOutline = null
            mHistory.clear()
            refresh()
            displayedViewIndex = loc
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (mButtonsVisible && mTopBarMode !== TopBarMode.Search) {
            hideButtons()
        } else {
            showButtons()
            searchModeOff()
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onSearchRequested(): Boolean {
        if (mButtonsVisible && mTopBarMode === TopBarMode.Search) {
            hideButtons()
        } else {
            showButtons()
            searchModeOn()
        }
        return super.onSearchRequested()
    }

    /**
     * 打开搜索模式
     */
    private fun searchModeOn() {
        if (mTopBarMode != TopBarMode.Search) {
            mTopBarMode = TopBarMode.Search
            //Focus on EditTextWidget
            bind.searchText.requestFocus()
            KeyBoardUtils.openKeyboard(bind.searchText)
            bind.switcher.displayedChild = mTopBarMode.ordinal
        }
    }

    /**
     * 关闭搜索模式
     */
    private fun searchModeOff() {
        if (mTopBarMode == TopBarMode.Search) {
            mTopBarMode = TopBarMode.Main
            KeyBoardUtils.closeKeyboard(bind.searchText)
            bind.switcher.displayedChild = mTopBarMode.ordinal
            SearchTaskResult.set(null)
            // Make the ReaderView act on the change to mSearchTaskResult
            // via overridden onChildSetup method.
            mDocView?.resetupChildren()
        }
    }

    /**
     * 搜索
     */
    private fun search(direction: Int) {
        KeyBoardUtils.closeKeyboard(bind.searchText)
        mSearchTask?.go(
            bind.searchText.text.toString(),
            direction,
            mDocView?.displayedViewIndex ?: 0,
            SearchTaskResult.get()?.pageNumber ?: -1
        )
    }

    /**
     * 设置链接高亮
     */
    private fun setLinkHighlight(highlight: Boolean) {
        mLinkHighlight = highlight
        // LINK_COLOR tint
        bind.linkButton.setColorFilter(
            if (highlight) Color.argb(
                0xFF, 0x00, 0x66, 0xCC
            ) else Color.argb(0xFF, 255, 255, 255)
        )
        // Inform pages of the change.
        mDocView?.setLinksEnabled(highlight)
    }


    /**
     * 显示不能打开文件弹窗并提示原因
     */
    private fun showCannotOpenDocByReason(reason: String) {
        val res = resources
        val alert = mAlertBuilder.create()
        title =
            String.format(Locale.ROOT, res.getString(R.string.cannot_open_document_Reason), reason)
        alert.setButton(
            AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss)
        ) { _, _ -> finish() }
        alert.show()
    }

    /**
     * 显示不能打开文件弹窗
     */
    private fun showCannotOpenDoc() {
        val alert = mAlertBuilder.create()
        alert.setTitle(R.string.cannot_open_document)
        alert.setButton(
            AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss)
        ) { _, _ -> finish() }
        alert.setOnCancelListener { finish() }
        alert.show()
    }
}