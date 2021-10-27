package dev.binclub.bingait.gui.swing

import com.github.weisj.darklaf.ui.filechooser.DarkFileChooserUI
import dev.binclub.bingait.plugin.PluginsPanel
import dev.binclub.bingait.api.util.item
import dev.binclub.bingait.api.util.lazyMenu
import dev.binclub.bingait.api.util.menu
import dev.binclub.bingait.plugin.PluginManager
import java.awt.Desktop
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JOptionPane
import javax.swing.filechooser.FileFilter
import kotlin.system.exitProcess

/**
 * @author cookiedragon234 29/Sep/2020
 */
object TopMenuBar: JMenuBar() {
	init {
		menu("File") {
			item("Open") {
				val jChooser = JFileChooser()
				jChooser.isMultiSelectionEnabled = true
				jChooser.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
				jChooser.fileFilter = object: FileFilter() {
					override fun accept(f: File?): Boolean =
						f?.let { f ->
							f.isDirectory || f.extension.let { it == "zip" || it == "jar" || it == "class" }
						} ?: false
					
					override fun getDescription(): String = "Java Applications"
				}
				val out = jChooser.showOpenDialog(this)
				if (out == JFileChooser.APPROVE_OPTION) {
					jChooser.selectedFiles?.let(MainWindow.classTreePanel::add)
				}
			}
			item("Preferences") {
				PreferencesPanel.popup()
			}
			item("Exit") {
				exitProcess(0)
			}
		}
		menu("Plugins") {
			item("Open Directory") {
				Desktop.getDesktop().open(PluginManager.pluginsDir)
			}
			lazyMenu("Manage") {
				this.removeAll()
				
				PluginManager.plugins.forEach { p ->
					menu(p.name) {
						p.settings.forEach {
							add(it.component())
						}
					}
				}
			}
		}
		menu("About") {
			item("Info") {
				JOptionPane.showMessageDialog(MainWindow, "Eggs are not supposed to be green.")
			}
		}
	}
}
