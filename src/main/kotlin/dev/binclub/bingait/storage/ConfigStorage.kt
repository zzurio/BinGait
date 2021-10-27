@file:Suppress("MemberVisibilityCanBePrivate")

package dev.binclub.bingait.storage

import dev.binclub.bingait.api.util.createDir
import java.io.File

/**
 * @author cookiedragon234 16/Sep/2020
 */
object ConfigStorage {
	val homeDir = File(System.getProperty("user.home"))
	val genericConfigDir = File(homeDir, ".config").createDir()
	val configDir = File(genericConfigDir, "BinGait").createDir()
}
