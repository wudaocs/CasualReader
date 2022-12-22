package com.ltd_tech.build

import com.ltd_tech.build.Versions.lifecycle

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


}

object DependsUIConfigs {
    const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.1.3"
    const val recyclerview = "androidx.recyclerview:recyclerview:1.1.0"
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

    const val SVGWireRuntime = "com.squareup.wire:wire-runtime:3.5.0"
}