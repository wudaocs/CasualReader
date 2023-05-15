package com.ltd_tech.core

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

open class BaseViewModel : ViewModel(), LifecycleObserver {

    private val mCoroutine : CoroutineScope = CoroutineScope(Dispatchers.IO)


    override fun onCleared() {
        super.onCleared()
        mCoroutine.cancel()
    }
}