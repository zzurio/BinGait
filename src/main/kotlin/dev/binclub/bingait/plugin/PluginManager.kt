package dev.binclub.bingait.plugin

import dev.binclub.bincode.parsing.ClassFileParser
import dev.binclub.bingait.api.BingaitPlugin
import dev.binclub.bingait.api.event.EventSystem
import dev.binclub.bingait.api.event.events.PluginRegisterEvent
import dev.binclub.bingait.api.util.BingaitShutdownHook
import dev.binclub.bingait.api.util.cast
import dev.binclub.bingait.storage.ConfigStorage
import dev.binclub.bingait.api.util.createDir
import java.io.DataInputStream
import java.io.File
import java.io.RandomAccessFile
import java.lang.Exception
import java.lang.reflect.Modifier
import java.net.URL
import java.net.URLClassLoader
import java.util.TreeSet
import java.util.jar.JarFile
import kotlin.concurrent.thread

/**
 * @author cookiedragon234 16/Sep/2020
 */
object PluginManager {
	val pluginsDir = File(ConfigStorage.configDir, "plugins").createDir()
	val plugins: MutableSet<BingaitPlugin> = TreeSet<BingaitPlugin>(Comparator.comparing(BingaitPlugin::name))
	
	init {
		scan(pluginsDir)
		BingaitShutdownHook.addHook {
			plugins.forEach(BingaitPlugin::onUnload)
			plugins.clear()
		}
	}
	
	class PluginClassLoader(urls: Array<URL>): URLClassLoader(urls, PluginManager::class.java.classLoader) {
		constructor(url: URL): this(arrayOf(url.toURI().toURL()))
		constructor(file: File): this(file.toURI().toURL())
		constructor(): this(arrayOf())
		
		fun <T: BingaitPlugin> definePlugin(bytes: ByteArray): Class<T> {
			return defineClass(null, bytes, 0, bytes.size).cast()
		}
	}
	
	fun register(vararg clazz: Class<out BingaitPlugin>) {
		clazz.cast<Array<Class<BingaitPlugin>>>().forEach(::register)
	}
	
	fun <T: BingaitPlugin> register(clazz: Class<T>): T {
		val plugin = clazz.declaredFields
			.firstOrNull { field -> field.type == clazz && Modifier.isStatic(field.modifiers) }
			?.let { field ->
				val instance = field.get(null)
				instance as T
			} ?: clazz.newInstance()
		EventSystem.dispatch(PluginRegisterEvent(plugin))
		return register(plugin)
	}
	
	fun register(vararg plugin: BingaitPlugin) {
		plugin.forEach(::register)
	}
	
	fun <T: BingaitPlugin> register(plugin: T): T {
		if (plugins.any { it.id == plugin.id }) {
			error("Duplicate plugin id ${plugin.id}")
		}
		plugins += plugin
		plugin.onLoad()
		return plugin
	}
	
	
	private const val pluginClass = "dev/binclub/bingait/api/BingaitPlugin"
	private fun scan(dir: File) {
		dir.listFiles()!!.forEach { file ->
			when (file.extension) {
				"jar" -> {
					var loader: PluginClassLoader? = null
					JarFile(file).use { jar ->
						jar.entries().asSequence().forEach { entry ->
							if (entry.name.endsWith(".class")) {
								try {
									val header =
										ClassFileParser.parseClassHeader(DataInputStream(jar.getInputStream(entry)))
									
									if (header.superClass[header.constantPool].nameRef[header.constantPool].value == pluginClass) {
										loader = (loader ?: PluginClassLoader(file)).also { loader ->
											val clazz: Class<BingaitPlugin> = loader.definePlugin(
												jar.getInputStream(entry).readBytes()
											)
											register(clazz)
										}
									}
								} catch (t: Throwable) {
									Exception(
										"Failed to parse possible plugin in class ${entry.name} of ${file.name}",
										t
									).printStackTrace()
								}
							}
						}
					}
				}
				"class" -> {
					try {
						val header = ClassFileParser.parseClassHeader(RandomAccessFile(file, "r"))
						
						if (header.superClass[header.constantPool].nameRef[header.constantPool].value == pluginClass) {
							val clazz: Class<BingaitPlugin> = PluginClassLoader().definePlugin(file.readBytes())
							register(clazz)
						}
					} catch (t: Throwable) {
						Exception("Failed to parse possible plugin", t).printStackTrace()
					}
				}
			}
		}
	}
}
