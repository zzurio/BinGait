package dev.binclub.bingait.api

import dev.binclub.bingait.api.event.EventSystem
import dev.binclub.bingait.api.event.events.BackgroundStatusUpdateEvent
import java.util.Optional
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * @author cook 19/Nov/2020
 */
object BingaitThreadpool {
	var currentTask: String? = null
	private val executor = Executors.newFixedThreadPool(1)
	private val bingaitThread = executor.submit(Callable { Thread.currentThread() }).get()
	
	fun <T> submitTask(descriptor: String, task: () -> T): Future<T> {
		if (Thread.currentThread() == bingaitThread) {
			val oldDescriptor = currentTask
			currentTask = descriptor
			val out = CompletableFuture<T>()
			try {
				out.complete(task())
			} catch (t: Throwable) {
				out.completeExceptionally(t)
			}
			currentTask = oldDescriptor
			return out
		}
		
		return executor.submit(Callable {
			currentTask = descriptor
			EventSystem.dispatch(BackgroundStatusUpdateEvent(descriptor))
			val out = task()
			currentTask = null
			EventSystem.dispatch(BackgroundStatusUpdateEvent(null))
			out
		})
	}
}

