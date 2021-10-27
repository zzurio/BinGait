package dev.binclub.bingait.api.settings

import java.awt.GridLayout
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.util.Properties
import javax.swing.*

/**
 * @author cook 17/Nov/2020
 */
class BooleanSetting(
	override val name: String,
	private var value: Boolean = false,
	override val callback: (old: Boolean, new: Boolean) -> Unit
): Setting<Boolean>(name, callback) {
	override fun getValue0(): Boolean = value
	
	override fun setValue0(new: Boolean) {
		value = new
	}
	
	override fun write(properties: Properties) {
		properties[name] = value.toString()
	}
	
	override fun read(properties: Properties) {
		properties[name]?.let {
			value = it.toString().toBoolean()
		}
	}
	
	override fun component(): JComponent {
		val box = JCheckBox().also {
			it.isSelected = value
			it.addItemListener { e ->
				setValue(e.stateChange == 1)
			}
		}
		
		val label = JLabel(name)
		
		return JPanel().also {
			it.layout = GridLayout(1, 2)
			it.add(label)
			it.add(box)
		}
	}
}
