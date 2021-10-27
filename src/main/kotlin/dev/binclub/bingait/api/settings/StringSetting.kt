package dev.binclub.bingait.api.settings

import java.awt.GridLayout
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.util.Properties
import javax.swing.*

/**
 * @author cook 17/Nov/2020
 */
class StringSetting(
	override val name: String,
	private var value: String,
	override val callback: (old: String, new: String) -> Unit
): Setting<String>(name, callback) {
	override fun getValue0(): String = value
	
	override fun setValue0(new: String) {
		value = new
	}
	
	override fun write(properties: Properties) {
		properties[name] = value.toString()
	}
	
	override fun read(properties: Properties) {
		properties[name]?.let {
			value = it.toString()
		}
	}
	
	override fun component(): JComponent {
		val box = JTextField(value).also {
			it.addFocusListener(object: FocusListener {
				override fun focusGained(e: FocusEvent?) {}
				
				override fun focusLost(e: FocusEvent?) {
					println("Updated ${it.text}")
					value = it.text
				}
			})
		}
		
		val label = JLabel(name)
		
		return JPanel().also {
			it.layout = GridLayout(1, 2)
			it.add(label)
			it.add(box)
		}
	}
}
