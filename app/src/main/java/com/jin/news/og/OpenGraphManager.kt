package com.jin.news.og

import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class OpenGraphManager {
    private val nThread = 30
    private val keepAliveTime = 10L
    private val workQueue = LinkedBlockingDeque<Runnable>() as BlockingDeque<Runnable>
    private val threadPool =
        ThreadPoolExecutor(nThread, nThread, keepAliveTime, TimeUnit.SECONDS, workQueue)

    fun start(ogRunnable: Runnable) = threadPool.execute(ogRunnable)

    fun resume(): MutableList<Runnable> = threadPool.shutdownNow()

    fun close() = threadPool.shutdown()
}