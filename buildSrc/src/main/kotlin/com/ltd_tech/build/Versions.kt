package com.ltd_tech.build

import com.ltd_tech.build.Versions.lifecycle
import com.ltd_tech.build.Versions.support_code

/**
 * 版本管理类
 */
object Versions {
    const val appcompat = "1.4.1"
    const val compileSdk = 32
    const val minSdk = 21
    const val targetSdk = 32
    const val lifecycle = "2.2.0"
    const val retrofit = "2.6.0"
    const val okHttp = "4.9.0"
    const val kotlin_version = "1.4.32"
    const val support_code = "1.2.0-beta01"
}

/**
 * android 版本相关
 */
object DependsConfigs {
    const val appcompat = "androidx.appcompat:appcompat:1.4.1"
    const val material = "com.google.android.material:material:1.5.0"
    const val lifecycle_extensions = "androidx.lifecycle:lifecycle-extensions:$lifecycle"
    const val lifecycle_viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle"
    const val livedata = "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle"
    const val lifecycle_runtimektx = "androidx.lifecycle:lifecycle-runtime-ktx:2.3.0-alpha02"
    const val databinding_compiler = "androidx.databinding:databinding-compiler:1.0.0"

    // 网络库相关
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val converter_scalars = "com.squareup.retrofit2:converter-scalars:${Versions.retrofit}"
    const val converter_gson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    const val adapter_rxjava2 = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit}"
    const val okHttp = "com.squareup.okhttp3:okhttp:${Versions.okHttp}"
    const val logging = "com.squareup.okhttp3:logging-interceptor:${Versions.okHttp}"

    const val support_annotations = "androidx.annotation:annotation:$support_code"

    const val gson = "com.google.code.gson:gson:2.8.6"
    //项目方法过多，需要分割
    const val multidex = "androidx.multidex:multidex:2.0.0"
    //二维码扫描
    const val zxing = "com.google.zxing:core:3.4.0"

    // 数据库
    const val ormlite_android = "com.j256.ormlite:ormlite-android:5.0"
    const val ormlite_core = "com.j256.ormlite:ormlite-core:5.0"

    const val rxjava = "io.reactivex.rxjava2:rxjava:2.2.12"
    const val rxandroid = "io.reactivex.rxjava2:rxandroid:2.1.1"

    const val rxlifecycle = "com.trello.rxlifecycle3:rxlifecycle-components:3.1.0"
    const val okio = "com.squareup.okio:okio:2.2.2"

    const val isoparser = "com.googlecode.mp4parser:isoparser:1.1.22"



}

object DependsUIConfigs {
    const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.1.3"
    const val recyclerview = "androidx.recyclerview:recyclerview:1.1.0"

    const val lottie = "com.airbnb.android:lottie:4.2.1"

    const val SVGAPlayer = "com.github.svga:SVGAPlayer-Android:2.5.14"
    const val SVGWireRuntime = "com.squareup.wire:wire-runtime:3.5.0"
    //万能的自定义日历框架
    const val calendarview = "com.haibin:calendarview:3.6.2"
    //圆图框架
    const val roundedimageview = "com.makeramen:roundedimageview:2.3.0"
    //优化 RecyclerViewAdapterHelper
    const val BaseRecyclerViewAdapterHelper = "com.github.CymChad:BaseRecyclerViewAdapterHelper:2.9.30"

    const val smartRefreshLayout = "com.scwang.smartrefresh:SmartRefreshLayout:1.1.0"
    //导航栏
    const val flycoTabLayout = "com.flyco.tablayout:FlycoTabLayout_Lib:2.1.2@aar"

    const val badgeview = "q.rorbin:badgeview:1.1.3"

    const val numberprogressbar = "com.daimajia.numberprogressbar:library:1.4@aar"

}

/**
 * kotlin 版本相关
 */
object DependsKotlinConfigs {
    const val core_ktx = "androidx.core:core-ktx:1.7.0"
    const val activity_ktx = "androidx.activity:activity-ktx:1.1.0"
    const val fragment_ktx = "androidx.fragment:fragment-ktx:1.2.5"
    const val kotlinx_coroutines_core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0"
    const val kotlinx_coroutines_android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.0"
    const val kotlin_coroutines_adapter =
        "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2"
    const val work_runtime_ktx = "androidx.work:work-runtime-ktx:2.3.4"
    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin_version}"
}

/**
 * 测试版本相关
 */
object DependsTestConfigs {
    const val junit = "junit:junit:4.13.2"
    const val junit_ext = "androidx.test.ext:junit:1.1.3"
    const val espresso = "androidx.test.espresso:espresso-core:3.4.0"
}

object DependsThirdsConfigs {
    // 屏幕适配
    const val autosize = "me.jessyan:autosize:1.2.1"
    const val leakcanaryDebug = "com.squareup.leakcanary:leakcanary-android:2.0-alpha-2"
    const val leakcanaryRelease = "com.squareup.leakcanary:leakcanary-android-no-op:1.6.3"

    const val stethoDebug = "com.facebook.stetho:stetho:1.5.1"
    const val stethoOkhttp3Debug = "com.facebook.stetho:stetho-okhttp3:1.5.1"

    // 多啦a梦 开发过程中检查工具
    const val doraemonkit = "com.didichuxing.doraemonkit:doraemonkit:3.2.0"
    const val doraemonkit_no_op = "com.didichuxing.doraemonkit:doraemonkit-no-op:3.2.0"

    const val crashreport = "com.tencent.bugly:crashreport:3.2.422"
    const val nativecrashreport = "com.tencent.bugly:nativecrashreport:3.7.5"

    const val MMKV = "com.tencent:mmkv-static:1.1.2"

    //图片加载框架
    const val glide = "com.github.bumptech.glide:glide:4.10.0"
    const val glide_compiler = "com.github.bumptech.glide:compiler:4.10.0"
    //glide各种样式的图片转换
    const val glide_transformations = "jp.wasabeef:glide-transformations:4.1.0"
    //下载框架
    const val filedownloader = "com.liulishuo.filedownloader:library:1.7.7"
    //时间选择器
    const val pickerView = "com.contrarywind:Android-PickerView:4.1.9"
    //消息总线，基于LiveData
    const val livebus = "com.jeremyliao:live-event-bus-x:1.5.7"

    //阿里路由框架
    const val ARouter_api = "com.alibaba:arouter-api:1.5.0"
    const val ARouter_compiler = "com.alibaba:arouter-compiler:1.2.2"

    const val JIMU = "com.github.jimu:componentlib:1.3.3"

    // 权限
    const val easypermissions = "pub.devrel:easypermissions:3.0.0"

    const val AndroidAudioConverter = "com.github.adrielcafe:AndroidAudioConverter:0.0.8"
    //CacheWebView通过拦截资源实现自定义缓存静态资源。突破WebView缓存空间限制，让缓存更简单。让网站离线也能正常访问
    const val web_cache = "ren.yale.android:cachewebviewlib:2.1.8"
    //友盟
    const val umeng_analytics = "com.umeng.analytics:analytics:latest.integration"
    //日志框架
    const val logger = "com.orhanobut:logger:2.2.0"
    //网页解析工具
    const val jsoup = "org.jsoup:jsoup:1.11.3"
    //  生成充值二维码和扫描二维码的库
    const val zxing_lite = "com.king.zxing:zxing-lite:1.1.9-androidx"

    // Google Mobile Services -> com.google.firebase:firebase-appindexing:10.0.1
    const val gmsAppindexing = "com.google.android.gms:play-services-appindexing:8.1.0"

}