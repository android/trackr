package com.example.android.trackr

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val NUMBER_OF_THREADS = 4

interface Scheduler {

    fun execute(work: () -> Unit)

    fun postToMainThread(work: () -> Unit)

    fun postDelayedToMainThread(delay: Long, work: () -> Unit)
}

/**
 * A [Scheduler] which defaults to doing work asynchronously but can be configured with
 * any custom [Scheduler] delegate.
 */
object DefaultScheduler : Scheduler {

    private var delegate: Scheduler = AsyncScheduler

    fun setDelegate(newDelegate: Scheduler?) {
        delegate = newDelegate ?: AsyncScheduler
    }

    override fun execute(work: () -> Unit) {
        delegate.execute(work)
    }

    override fun postToMainThread(work: () -> Unit) {
        delegate.postToMainThread(work)
    }

    override fun postDelayedToMainThread(delay: Long, work: () -> Unit) {
        delegate.postToMainThread(work)
    }
}

/**
 * Does work in an [ExecutorService] with a fixed count thread pool.
 */
internal object AsyncScheduler : Scheduler {

    private val executorService: ExecutorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS)

    private val isMainThread: Boolean
        get() = Looper.getMainLooper().thread == Thread.currentThread()

    override fun execute(work: () -> Unit) {
        executorService.execute(work)
    }

    override fun postToMainThread(work: () -> Unit) {
        if (isMainThread) {
            work()
        } else {
            val mainThreadHandler = Handler(Looper.getMainLooper())
            mainThreadHandler.post(work)
        }
    }

    override fun postDelayedToMainThread(delay: Long, work: () -> Unit) {
        val mainThreadHandler = Handler(Looper.getMainLooper())
        mainThreadHandler.postDelayed(work, delay)
    }
}