package dev.binclub.bingait.api.util

import kotlin.concurrent.thread

/**
 * @author cook 17/Oct/2020
 */
object BingaitShutdownHook {
	private var hooks: MutableList<Runnable> = ArrayList()
	
	init {
		Runtime.getRuntime().addShutdownHook(thread(start = false, isDaemon = false, name = "Bingait Shutdown Hook") {
			hooks.forEach(Runnable::run)
		})
	}
	
	fun addHook(hook: Runnable) = hooks.add(hook)
	fun addHook(hook: () -> Unit) = hooks.add(hook)
}
