package dev.binclub.bingait.api

import dev.binclub.bingait.api.event.EventSystem
import dev.binclub.bingait.api.settings.BooleanSetting
import dev.binclub.bingait.api.settings.Setting
import dev.binclub.bingait.api.util.cast
import dev.binclub.bingait.plugin.PluginManager
import java.io.File
import java.lang.reflect.Modifier
import java.util.Properties
import java.util.function.Consumer
import java.util.regex.Pattern

/**
 * @author cookiedragon234 16/Sep/2020
 */
abstract class BingaitPlugin {
	var enabled: Boolean by BooleanSetting("enabled", false) { old, new ->
		if (new) onEnabled() else onDisabled()
	}
	
	/**
	 * ID must be unique to this plugin and must only be alphanumeric
	 */
	abstract val id: String
	abstract val name: String
	abstract val description: String
	abstract val version: String
	
	open fun onLoad() { readFromConfig() }
	open fun onUnload() { writeToConfig() }
	
	protected open fun onEnabled() {}
	protected open fun onDisabled() {}
	
	open fun readFromConfig(properties: Properties) {
		settings.forEach {
			it.read(properties)
		}
	}
	
	open fun writeToConfig(properties: Properties) {
		settings.forEach {
			it.write(properties)
		}
	}
	
	val settings: List<Setting<*>> by lazy {
		ArrayList<Setting<*>>().also { settings ->
			val clazzes = arrayListOf<Class<*>>()
			var clazz: Class<*> = this::class.java
			
			while (BingaitPlugin::class.java.isAssignableFrom(clazz)) {
				clazzes.add(clazz)
				clazz = clazz.superclass
			}
			
			clazzes.asReversed().forEach { clazz ->
				clazz.declaredFields.forEach { f ->
					try {
						f.isAccessible = true
						if (Setting::class.java.isAssignableFrom(f.type)) {
							if (Modifier.isStatic(f.modifiers)) {
								settings.add(f.get(null).cast())
							} else {
								settings.add(f.get(this).cast())
							}
						}
					} catch (t: Throwable) {
						Exception("Could not parse possible setting ${f.name}:${f.type}", t).printStackTrace()
					}
				}
			}
			
			println(settings)
		}
	}
	
	
	//
	
	
	inline fun <reified T> register(noinline lambda: (T) -> Unit) =
		EventSystem.register(T::class.java, PluginEventListener(this, lambda))
	
	fun <T> register(event: Class<T>, lambda: Consumer<T>) =
		EventSystem.register(event, PluginEventListener(this, lambda))
	
	class PluginEventListener<T>(
		private val plugin: BingaitPlugin,
		lambda: Consumer<T>
	): EventSystem.Listener<T>(lambda) {
		override var active: Boolean
			get() = plugin.enabled
			set(value) {}
	}
	
	
	//
	
	
	private val idPattern: Pattern = Pattern.compile("[a-zA-Z0-9._\\-:]+")
	private val pluginFile by lazy { // lazy to allow time for id field to initialize
		val id = this.id
		if (!idPattern.matcher(id).matches()) {
			error("Invalid non alphanumeric plugin id [$id]")
		}
		File(PluginManager.pluginsDir, "$id.properties").also(File::createNewFile)
	}
	
	private fun readFromConfig() {
		val props = Properties()
		if (pluginFile.exists()) {
			props.load(pluginFile.bufferedReader())
		}
		readFromConfig(props)
	}
	
	private fun writeToConfig() {
		val props = Properties()
		if (pluginFile.exists()) {
			props.load(pluginFile.bufferedReader())
		}
		writeToConfig(props)
		props.forEach { key, value ->
			props[key] = value.toString()
		}
		if (!pluginFile.exists()) pluginFile.createNewFile()
		props.store(pluginFile.bufferedWriter(), "Bingait Plugin $name")
	}
}
