package dev.binclub.bingait.api.util.logging

import java.io.PrintWriter
import java.io.StringWriter
import java.text.MessageFormat
import java.util.Date
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord

/**
 * @author cookiedragon234 05/Aug/2020
 */
object BingaitFormatter: Formatter() {
	private const val format = "{0,time}"
	private val formatter = MessageFormat(format)
	
	// Text length of longest level
	private val maxLevelSize =
		arrayOf(Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL)
			.map { it.localizedName.length }
			.max()!!
	
	private const val ANSI_RESET = "\u001B[0m"
	private const val ANSI_BLACK = "\u001B[30m"
	private const val ANSI_RED = "\u001B[31m"
	private const val ANSI_GREEN = "\u001B[32m"
	private const val ANSI_YELLOW = "\u001B[33m"
	private const val ANSI_BLUE = "\u001B[34m"
	private const val ANSI_PURPLE = "\u001B[35m"
	private const val ANSI_CYAN = "\u001B[36m"
	private const val ANSI_WHITE = "\u001B[37m"
	
	private fun Level.ansiColour(): String =
		when (this) {
			Level.SEVERE -> ANSI_RED
			Level.WARNING -> ANSI_PURPLE
			Level.INFO -> ANSI_CYAN
			else -> ""
		}
	
	override fun format(record: LogRecord): String {
		return buildString {
			val date = Date(record.millis)
			
			append(formatter.format(arrayOf(date)))
			append(' ')
			
			val level = record.level
			append('[')
			append(level.ansiColour())
			append(level.localizedName)
			append(ANSI_RESET)
			append(']')
			append(':')
			
			val levelIndent = maxLevelSize - level.localizedName.length
			append(" ".repeat(levelIndent.coerceAtLeast(0) + 1))
			
			append(record.message)
			
			append(System.lineSeparator())
			
			record.thrown?.let { thrown ->
				val sw = StringWriter()
				PrintWriter(sw).use {
					thrown.printStackTrace(it)
				}
				append(sw)
			}
		}
	}
}
