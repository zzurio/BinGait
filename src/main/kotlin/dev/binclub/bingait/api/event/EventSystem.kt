package dev.binclub.bingait.api.event

import java.util.LinkedList
import java.util.concurrent.Executors
import java.util.function.Consumer

/**
 * @author cookiedragon234 16/Sep/2020
 */
object EventSystem {
	private val threadPool = Executors.newFixedThreadPool(
		(Runtime.getRuntime().availableProcessors() / 2).coerceIn(1..5)
	)
	private val eventListeners: MutableMap<Class<*>, MutableCollection<Listener<*>>> = hashMapOf()
	
	@JvmStatic
	inline fun <reified T> register(noinline lambda: (T) -> Unit) = register(T::class.java, lambda)
	
	@JvmStatic
	fun <T> register(type: Class<T>, lambda: (T) -> Unit) = register(type, Listener(lambda))
	
	@JvmStatic
	fun <T> register(type: Class<T>, listener: Listener<T>) {
		eventListeners.getOrPut(type, { LinkedList() }).add(listener)
	}
	
	@JvmStatic
	fun dispatch(event: Any, asynchronous: Boolean = false) {
		var type: Class<*>? = event::class.java
		while (type != null) {
			eventListeners[type]?.forEach { listener ->
				if (listener.active) {
					try {
						if (asynchronous) {
							threadPool.execute {
								listener(event)
							}
						} else {
							listener(event)
						}
					} catch (t: Throwable) {
						IllegalStateException("Error invoking event $event", t).printStackTrace()
					}
				}
			}
			type = type.superclass
		}
	}
	
	open class Listener<T>(
		private val lambda: Consumer<T>,
		open var active: Boolean = true
	) {
		operator fun invoke(event: Any) {
			lambda.accept(event as T)
		}
	}
}
