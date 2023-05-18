package com.ltd_tech.readsdk.views

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.ltd_tech.core.exts.globalToast
import com.ltd_tech.core.utils.BrightnessUtils
import com.ltd_tech.core.utils.ScreenUtils
import com.ltd_tech.core.widgets.pager.PageMode
import com.ltd_tech.core.widgets.pager.PageStyle
import com.ltd_tech.readsdk.R
import com.ltd_tech.readsdk.databinding.DialogReadSettingBinding
import com.ltd_tech.readsdk.loader.BookLoader
import com.ltd_tech.readsdk.page.adapter.SettingBackgroundAdapter
import com.ltd_tech.readsdk.utils.ReadSettingManager

/**
 * 书籍详情页面设置弹窗
 * author: Kaos
 * created on 2023/5/15
 */
class ReadSettingDialog(
    private val activity: Activity,
    private val bookLoader: BookLoader?,
) :
    Dialog(activity, R.style.ReadSettingDialog) {

    private var binding: DialogReadSettingBinding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.dialog_read_setting, null, false)

    private var mSettingBackgroundAdapter: SettingBackgroundAdapter? = null

    init {
        initWidget()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val wd = window
        val lp = wd?.attributes
        lp?.width = ScreenUtils.width
        lp?.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp?.gravity = Gravity.BOTTOM
        wd?.attributes = lp
    }

    private fun initWidget() {
        binding.run {
            sbDialogReadSettingBrightness.progress = ReadSettingManager.getBrightness()
            tvDialogReadSettingFont.text = ReadSettingManager.getTextSize().toString()
            cbDialogReadSettingBrightnessAuto.isChecked = ReadSettingManager.isBrightnessAuto()
            cbDialogReadSettingFontDefault.isChecked = ReadSettingManager.isDefaultTextSize()

            when (ReadSettingManager.getPageMode()) {
                PageMode.TURN_PAGE -> rbDialogReadSettingSimulation.isChecked = true
                PageMode.COVER -> rbDialogReadSettingCover.isChecked = true
                PageMode.NONE -> rbDialogReadSettingNone.isChecked = true
                PageMode.SCROLL -> rbDialogReadSettingScroll.isChecked = true
            }
        }

        //RecyclerView
        setUpAdapter()

        setListener()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getDrawable(drawRes: Int): Drawable {
        return ContextCompat.getDrawable(context, drawRes) ?: context.resources.getDrawable(drawRes)
    }

    /**
     * 设置背景色
     */
    private fun setUpAdapter() {
        mSettingBackgroundAdapter = SettingBackgroundAdapter(
            context,
            PageStyle.values().toMutableList(),
            onItemClick = { pos, pageStyle ->
                //背景的点击事件
                bookLoader?.setPageStyle(pageStyle)
                mSettingBackgroundAdapter?.setPageStyle(pageStyle)
            })

        binding.rvDialogReadSettingBg.run {
            layoutManager = GridLayoutManager(context, 5)
            adapter = mSettingBackgroundAdapter
        }

        mSettingBackgroundAdapter?.setPageStyle(ReadSettingManager.getPageStyle())
    }

    private fun setListener() {
        binding.run {

            /** ---------↓↓↓---------  亮度调节  ---------↓↓↓--------- **/
            // 亮度调节 -
            ivDialogReadSettingBrightnessMinus.setOnClickListener {
                if (cbDialogReadSettingBrightnessAuto.isChecked) {
                    cbDialogReadSettingBrightnessAuto.isChecked = false
                }
                val progress = sbDialogReadSettingBrightness.progress - 1
                if (progress >= 0) {
                    updateProgress(progress)
                }
            }
            // 亮度调节 +
            ivDialogReadSettingBrightnessPlus.setOnClickListener {
                if (cbDialogReadSettingBrightnessAuto.isChecked) {
                    cbDialogReadSettingBrightnessAuto.isChecked = false
                }
                val progress = sbDialogReadSettingBrightness.progress + 1
                if (progress <= sbDialogReadSettingBrightness.max) {
                    updateProgress(progress)
                }
            }
            // 亮度进度条
            sbDialogReadSettingBrightness.setOnSeekBarChangeListener(object :
                OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    if (cbDialogReadSettingBrightnessAuto.isChecked) {
                        cbDialogReadSettingBrightnessAuto.isChecked = false
                    }
                    seekBar?.run {
                        updateProgress(progress)
                    }
                }
            })

            // 是否随系统
            cbDialogReadSettingBrightnessAuto.setOnCheckedChangeListener { _, isChecked ->
                updateProgress(
                    if (isChecked) {
                        BrightnessUtils.getScreenBrightness()
                    } else {
                        sbDialogReadSettingBrightness.progress
                    }
                )
            }

            /** ---------↑↑↑------------------------↑↑↑--------- **/

            /** ---------↓↓↓---------  字体调节  ---------↓↓↓--------- **/
            tvDialogReadSettingFontMinus.setOnClickListener {
                if (cbDialogReadSettingFontDefault.isChecked) {
                    cbDialogReadSettingFontDefault.isChecked = false
                }
                val fontSize = tvDialogReadSettingFont.text.toString().toInt() - 1
                if (fontSize > 0) {
                    tvDialogReadSettingFont.text = fontSize.toString()
                    bookLoader?.setOrUpdateTextSize(fontSize)
                }
            }

            tvDialogReadSettingFontPlus.setOnClickListener {
                if (cbDialogReadSettingFontDefault.isChecked) {
                    cbDialogReadSettingFontDefault.isChecked = false
                }
                val fontSize = tvDialogReadSettingFont.text.toString().toInt() + 1
                if (fontSize < 100){
                    tvDialogReadSettingFont.text = fontSize.toString()
                    bookLoader?.setOrUpdateTextSize(fontSize)
                } else {
                    globalToast("字体大小超过上限")
                }
            }

            cbDialogReadSettingFontDefault.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    val fontSize: Int = ScreenUtils.dp2px(20f)
                    tvDialogReadSettingFont.text = fontSize.toString()
                    bookLoader?.setOrUpdateTextSize(fontSize)
                }
            }

            /** ---------↑↑↑------------------------↑↑↑--------- **/
            //Page Mode 切换
            rgDialogReadSettingPageMode.setOnCheckedChangeListener { _, checkedId ->
                val pageMode = when(checkedId){
                    R.id.rb_dialog_read_setting_simulation -> {
                        PageMode.TURN_PAGE
                    }
                    R.id.rb_dialog_read_setting_cover -> {
                        PageMode.COVER
                    }
                    R.id.rb_dialog_read_setting_slide -> {
                        PageMode.SCROLL
                    }
                    R.id.rb_dialog_read_setting_scroll -> {
                        PageMode.NONE
                    }
                    else -> PageMode.TURN_PAGE
                }
                bookLoader?.setPageMode(pageMode)
            }

            tvDialogReadSettingMore.setOnClickListener {
                // 更多设置
            }

        }
    }


    /**
     * 更新亮度进度
     */
    private fun updateProgress(progress: Int) {
        binding.run {
            sbDialogReadSettingBrightness.progress = progress
            BrightnessUtils.setBrightness(activity, progress)
            ReadSettingManager.setBrightness(progress)
        }
    }


}