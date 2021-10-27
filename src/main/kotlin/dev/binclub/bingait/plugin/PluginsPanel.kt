package dev.binclub.bingait.plugin

import dev.binclub.bingait.api.BingaitPlugin
import dev.binclub.bingait.api.util.textComponent
import dev.binclub.bingait.gui.swing.MainWindow
import java.awt.*
import javax.swing.*


/**
 * @author cook 05/Oct/2020
 */
class PluginsPanel: JPanel() {
	private val pluginsListPanel = JPanel()
	private val pluginsScrollList = JScrollPane(pluginsListPanel)
	
	init {
		val plugins = PluginManager.plugins
		pluginsListPanel.layout = BoxLayout(pluginsListPanel, BoxLayout.Y_AXIS)
		
		pluginsScrollList.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
		plugins.forEach { plugin ->
			pluginsListPanel.add(PluginPanel(plugin))
		}
		this.add(pluginsScrollList)
	}
	
	
	companion object {
		fun popup() {
			val frame = JDialog(MainWindow, "Plugins", true)
			frame.minimumSize = Dimension(600, 300)
			frame.contentPane.add(PluginsPanel())
			frame.isVisible = true
		}
	}
}

class PluginPanel(val plugin: BingaitPlugin): JPanel() {
	init {
		layout = BorderLayout()
		add(JCheckBox().also { checkbox ->
			checkbox.isSelected = plugin.enabled
			checkbox.addItemListener { event ->
				if (event.source == checkbox) {
					plugin.enabled = event.stateChange == 1
				}
			}
		}, BorderLayout.WEST)
		add(textComponent(plugin.name), BorderLayout.CENTER)
	}
}
