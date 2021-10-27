package dev.binclub.bingait.api.settings

import java.util.Properties
import javax.swing.JComponent
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author cook 17/Nov/2020
 */
abstract class Setting<T>(
	open val name: String,
	open val callback: (old: T, new: T) -> Unit
): ReadWriteProperty<Any?, T> {
	override fun getValue(thisRef: Any?, property: KProperty<*>): T = getValue()
	override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = setValue(value)
	
	fun getValue(): T = getValue0()
	fun setValue(new: T) {
		val old = getValue0()
		setValue0(new)
		callback(old, new)
	}
	
	abstract fun getValue0(): T
	abstract fun setValue0(new: T)
	
	abstract fun write(properties: Properties)
	abstract fun read(properties: Properties)
	
	abstract fun component(): JComponent
	
	override fun toString(): String = "$name{${getValue()}}"
}
