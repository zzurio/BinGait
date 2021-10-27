package dev.binclub.bingait.api.util

import java.io.*
import javax.swing.JComponent


/**
 * @author cookiedragon234 28/May/2020
 */
inline fun <reified T: Any> Any?.cast(): T = this as T
inline fun <reified T: Any> Any?.maybeCast(): T? = this as? T

inline fun <T> Array<T>.mapArr(op: (T) -> T) = this.apply {
	for (i in this.indices) {
		this[i] = op(this[i])
	}
}

inline fun CharArray.mapArr(op: (Char) -> Char) = this.apply {
	for (i in this.indices) {
		this[i] = op(this[i])
	}
}

inline fun ByteArray.mapArr(op: (Byte) -> Byte) = this.apply {
	for (i in this.indices) {
		this[i] = op(this[i])
	}
}

fun DataInput.readBytes(): ByteArray {
	if (this is InputStream) {
		val buffer = ByteArrayOutputStream(maxOf(DEFAULT_BUFFER_SIZE, this.available()))
		copyTo(buffer)
		return buffer.toByteArray()
	}
	
	if (this is RandomAccessFile) {
		val bytes = ByteArray(this.length().toInt())
		this.readFully(bytes)
		return bytes
	}
	
	val out = ByteArrayOutputStream()
	while (true) {
		try {
			out.write(this.readByte().toInt())
		} catch (e: EOFException) {
			break
		}
	}
	return out.toByteArray()
}

fun String.removeDuplicates(): String {
	val chars = toCharArray()
	val n = chars.size
	if (n < 2) return String(chars)
	
	var j = 0
	for (i in 1 until n) {
		if (chars[j] != chars[i]) {
			j++
			chars[j] = chars[i]
		}
	}
	return String(chars, 0, j + 1)
}

fun String.removeDuplicatesOfChar(char: Char): String {
	val chars = toCharArray()
	val n = chars.size
	if (n < 2) return String(chars)
	
	var j = 0
	for (i in 1 until n) {
		val thisChar = chars[i]
		if (thisChar != char || chars[j] != thisChar) {
			j++
			chars[j] = thisChar
		}
	}
	return String(chars, 0, j + 1)
}

fun File.classPathProvider(): (String) -> DataInput? {
	val dir = when {
		this.isDirectory -> this
		this.isFile -> this.parentFile
		else -> error("$this is not a directory or file")
	}
	return { resource ->
		val withoutPath = File(dir, resource.substringAfterLast(File.pathSeparatorChar))
		val withPath = File(dir, resource)
		when {
			withoutPath.exists() -> {
				RandomAccessFile(withoutPath, "r")
			}
			withPath.exists() -> {
				RandomAccessFile(withPath, "r")
			}
			else -> null
		}
	}
}

fun escapeHTML(s: String): String = buildString(s.length) {
	for (c in s) {
		when (c) {
			'<' -> append("&lt;")
			'>' -> append("&gt;")
			'&' -> append("&amp;")
			else -> append(c)
		}
	}
}

fun File.createDir(): File =
	this.apply { if (this.exists() && this.isFile) error("$this already exists as file") else this.mkdir() }

fun Any.wait() = JavaUtils.wait(this)
fun Any.notify() = JavaUtils.notify(this)

fun Throwable.removeBingait() {
	this.stackTraceToString()
	val stacktrace = Throwable::class.java.getDeclaredField("stackTrace").let {
		it.isAccessible = true
		it.get(this) as Array<StackTraceElement>
	}
	var size = 0
	for (el in stacktrace) {
		if (el.className.startsWith("dev.binclub.bingait")) {
			break
		}
		size += 1
	}
	val newStacktrace = stacktrace.copyOfRange(0, size)
	Throwable::class.java.getDeclaredField("stackTrace").let {
		it.isAccessible = true
		it.set(this, newStacktrace)
	}
	
	// No need to check for circular reference, we already print stack trace earlier which will throw that exception
	// for us
	this.suppressed.forEach(Throwable::removeBingait)
	this.cause?.removeBingait()
}

fun JComponent.disableHtml(): JComponent = this.apply {
	putClientProperty("html.disable", true)
}

fun File.recursivelyDelete() {
	if (this.exists()) {
		if (this.isDirectory) {
			this.listFiles()?.forEach(File::recursivelyDelete)
		}
		this.delete()
	}
}

fun File.traverseDeepFiles(op: (File) -> Unit) {
	if (this.isFile) {
		op(this)
		return
	}
	
	val dirs = arrayListOf(this)
	while (dirs.isNotEmpty()) {
		val file = dirs.removeLast()
		if (file.isFile) {
			op(file)
		} else if (file.isDirectory) {
			file.listFiles()?.forEach(dirs::add)
		}
	}
}
