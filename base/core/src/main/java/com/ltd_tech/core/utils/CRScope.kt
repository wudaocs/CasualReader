package com.ltd_tech.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 协程控制器
 * author: Kaos
 * created on 2023/5/16
 */
class CRScope {

    private var coroutinesScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val scope = CoroutineScope(Dispatchers.Main)
    private var job: Job? = null

    fun execute(doWork: suspend () -> Unit) {
        stop()
        if (job == null) {
            job = scope.launch(Dispatchers.Main) {
                doWork()
            }
        }
    }

    fun async(doWork: suspend () -> Unit) {
        stop()
        if (job == null) {
            job = scope.launch(Dispatchers.IO) {
                doWork()
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}