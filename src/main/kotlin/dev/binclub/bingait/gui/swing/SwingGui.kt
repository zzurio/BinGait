package dev.binclub.bingait.gui.swing

import com.github.weisj.darklaf.LafManager
import dev.binclub.bingait.plugin.PluginManager
import dev.binclub.bingait.storage.BingaitConfig

/**
 * @author cookiedragon234 06/Sep/2020
 */
object SwingGui {
	@JvmStatic
	fun main(args: Array<String>) {
		System.setProperty("awt.useSystemAAFontSettings", "lcd_hrgb")
		LafManager.install()
		LafManager.installTheme(BingaitConfig.preferredTheme)
		
		PluginManager
		
		MainWindow.isVisible = true
		
		//Thread.currentThread().join()
	}
}
