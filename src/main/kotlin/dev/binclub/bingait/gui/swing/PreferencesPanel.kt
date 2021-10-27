package dev.binclub.bingait.gui.swing

import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.theme.Theme
import dev.binclub.bingait.api.util.cast
import dev.binclub.bingait.storage.BingaitConfig
import java.awt.*
import javax.swing.*


/**
 * @author cookiedragon234 19/Sep/2020
 */
class PreferencesPanel: JPanel() {
	init {
		layout = BoxLayout(this, BoxLayout.Y_AXIS)
		
		JPanel().also { settingPanel ->
			settingPanel.layout = BorderLayout()
			settingPanel.maximumSize = Dimension(Int.MAX_VALUE, 30)
			add(settingPanel)
			val label = JLabel()
			label.text = "Theme"
			settingPanel.add(label, BorderLayout.WEST)
			val comboBox = JComboBox(LafManager.getThemeComboBoxModel())
			comboBox.selectedItem = LafManager.getInstalledTheme()
			settingPanel.add(comboBox, BorderLayout.EAST)
			comboBox.addActionListener { e ->
				val value = comboBox.selectedItem.cast<Theme>()
				BingaitConfig.preferredTheme = value
				LafManager.installTheme(value)
			}
		}
		
		JPanel().also { settingPanel ->
			settingPanel.layout = BorderLayout()
			settingPanel.maximumSize = Dimension(Int.MAX_VALUE, 30)
			add(settingPanel)
			val label = JLabel()
			label.text = "Open FS Root on startup"
			settingPanel.add(label, BorderLayout.WEST)
			val checkBox = JCheckBox()
			checkBox.isSelected = BingaitConfig.openFsRoot
			settingPanel.add(checkBox, BorderLayout.EAST)
			checkBox.addActionListener { e ->
				val value = checkBox.isSelected
				BingaitConfig.openFsRoot = value
			}
		}
	}
	
	companion object {
		fun popup() {
			val frame = JDialog(MainWindow, "Preferences", true)
			frame.minimumSize = Dimension(600, 300)
			val scrollBox = JScrollPane(PreferencesPanel())
			frame.contentPane.add(scrollBox)
			frame.isVisible = true
		}
	}
}
