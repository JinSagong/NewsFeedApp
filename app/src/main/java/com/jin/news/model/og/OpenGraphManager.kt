package com.jin.news.model.og

import com.jin.news.util.LOADING_TIME
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class OpenGraphManager {
    private val nThread = 30
    private val workQueue = LinkedBlockingDeque<Runnable>() as BlockingDeque<Runnable>
    private val threadPool = ThreadPoolExecutor(nThread, nThread, LOADING_TIME, TimeUnit.MILLISECONDS, workQueue)

    fun start(ogRunnable: Runnable) = threadPool.execute(ogRunnable)

    // workQueue 에 남은 작업들을 모두 버리고 진행중인 작업들도 즉시 모두 중단함
    fun resume(): MutableList<Runnable> = threadPool.shutdownNow()

    // workQueue 에 남은 작업들을 모두 처리한 후 threadPool 을 종료함
    fun close() = threadPool.shutdown()
}