package dev.binclub.bingait.storage

import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.theme.Theme
import dev.binclub.bingait.api.util.BingaitShutdownHook
import java.io.File
import java.util.Properties

/**
 * @author cook 17/Oct/2020
 */
object BingaitConfig {
	private val configFile = File(ConfigStorage.configDir, "config.properties").also(File::createNewFile)
	
	private var properties = Properties()
	
	init {
		load()
		save()
		
		BingaitShutdownHook.addHook(this::save)
	}
	
	var openFsRoot: Boolean
		get() {
			val value = when (val x = properties["openFsRoot"]) {
				is Boolean -> x
				is String -> x.toBoolean()
				else -> true
			}
			properties["openFsRoot"] = value
			return value
		}
		set(value) {
			properties["openFsRoot"] = value
		}
	
	var preferredTheme: Theme
		get() {
			val theme: Theme = when (val name = properties["theme"]) {
				is Theme -> name
				is String -> {
					LafManager.getRegisteredThemes().firstOrNull {
						it.toString() == name
					} ?: LafManager.getInstalledTheme()
				}
				else -> LafManager.getInstalledTheme()
			}
			properties["theme"] = theme
			return theme
		}
		set(value) {
			properties["theme"] = value
		}
	
	var preferredResourceView: String?
		get() = properties["preferredResourceView"]?.toString()
		set(value) {
			properties["preferredResourceView"] = value
		}
	
	fun load() {
		properties = Properties().also {
			it.load(configFile.bufferedReader())
		}
	}
	
	fun save() {
		properties.forEach { key, value ->
			properties[key] = value.toString()
		}
		properties.store(configFile.bufferedWriter(), "Generic configuration")
	}
}
