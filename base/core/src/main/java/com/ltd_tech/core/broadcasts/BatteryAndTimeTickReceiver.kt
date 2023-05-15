package com.ltd_tech.core.broadcasts

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import java.util.concurrent.ConcurrentHashMap

/**
 * 增加关于电量和时间变化的广播接收器
 * author: Kaos
 * created on 2023/5/12
 */
class BatteryAndTimeTickReceiver(
    private val battery: (Int) -> Unit,
    private val time: () -> Unit
) :
    BroadcastReceiver() {

    companion object {

        /**
         * 保存创建的对象实例 用于解注册
         */
        private val hashMap: ConcurrentHashMap<Activity, BatteryAndTimeTickReceiver> =
            ConcurrentHashMap()

        /**
         * 注册广播
         * @param battery 电量变化回调
         * @param time 时间变化回调
         */
        fun register(activity: Activity, battery: (Int) -> Unit, time: () -> Unit) {
            //注册广播
            val intentFilter = IntentFilter()
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
            intentFilter.addAction(Intent.ACTION_TIME_TICK)
            activity.registerReceiver(BatteryAndTimeTickReceiver(battery, time).apply {
                hashMap[activity] = this
            }, intentFilter)
        }

        // 解注册广播

        fun unregister(activity: Activity) {
            try {
                hashMap[activity]?.run {
                    activity.unregisterReceiver(this)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.run {
            if (action == Intent.ACTION_BATTERY_CHANGED) {
                battery(getIntExtra("level", 0))
            } else if (intent.action == Intent.ACTION_TIME_TICK) {
                time()
            }
        }
    }

}