package dev.binclub.bingait.api.util.logging

import java.util.logging.ConsoleHandler
import java.util.logging.Logger

/**
 * @author cookiedragon234 05/Aug/2020
 */
object BingaitLogger {
	fun getLogger(name: String) = Logger.getLogger(name).also { logger ->
		logger.useParentHandlers = false
		logger.handlers.forEach {
			logger.removeHandler(it)
		}
		logger.addHandler(ConsoleHandler().also {
			it.formatter = BingaitFormatter
		})
	}
}
