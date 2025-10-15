package com.hy.kuikly_easyui.adapter

import com.tencent.kuikly.core.render.android.adapter.IKRThreadAdapter
import java.util.concurrent.Executors

class KRThreadAdapter : IKRThreadAdapter {
    override fun executeOnSubThread(task: () -> Unit) {
        execOnSubThread(task)
    }
}

private val subThreadPoolExecutor by lazy {
    Executors.newFixedThreadPool(2)
}

fun execOnSubThread(runnable: () -> Unit) {
    subThreadPoolExecutor.execute(runnable)
}