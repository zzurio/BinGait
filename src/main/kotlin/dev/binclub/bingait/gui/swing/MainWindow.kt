package dev.binclub.bingait.gui.swing

import dev.binclub.bingait.api.event.events.ClassTreeItemSelectedEvent
import dev.binclub.bingait.api.event.EventSystem
import dev.binclub.bingait.api.util.tree.ArchiveEntryTreeCell
import dev.binclub.bingait.api.util.tree.FileTreeCell
import dev.binclub.bingait.api.util.classPathProvider
import dev.binclub.bingait.api.util.findActiveScreen
import dev.binclub.bingait.storage.BingaitConfig
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.io.RandomAccessFile
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.border.BevelBorder


/**
 * @author cookiedragon234 06/Sep/2020
 */
object MainWindow: JFrame() {
	private val splitPane: JSplitPane
	val classTreePanel: ClassTreePanel
	val resourceViewer: ResourceViewerPanel
	private val statusBar: StatusBarPanel
	
	init {
		title = "BinGait"
		iconImage = ImageIcon(Toolkit.getDefaultToolkit().getImage(javaClass.getResource("/images/icon.png"))).image
		jMenuBar = TopMenuBar
		defaultCloseOperation = EXIT_ON_CLOSE
		
		layout = BorderLayout()
		
		val screen = findActiveScreen()
		setBounds(screen.x, screen.y, screen.width, screen.height)
		
		splitPane = JSplitPane()
		splitPane.resizeWeight = 0.1
		splitPane.maximumSize = Dimension(width, height)
		splitPane.preferredSize = splitPane.maximumSize
		
		
		classTreePanel = ClassTreePanel()
		val scrollPane = JScrollPane(classTreePanel)
		scrollPane.border = BevelBorder(BevelBorder.LOWERED)
		splitPane.add(scrollPane, JSplitPane.LEFT)
		
		if (BingaitConfig.openFsRoot) {
			classTreePanel.add(arrayOf(File(".")))
		}
		
		resourceViewer = ResourceViewerPanel()
		resourceViewer.border = BevelBorder(BevelBorder.LOWERED)
		splitPane.add(resourceViewer, JSplitPane.RIGHT)
		
		
		this.add(splitPane)
		
		statusBar = StatusBarPanel()
		this.add(statusBar, BorderLayout.SOUTH)
		
		addWindowListener(object: WindowAdapter() {
			override fun windowClosed(e: WindowEvent?) {
			}
		})
		
		EventSystem.register { e: ClassTreeItemSelectedEvent ->
			when (val selected = e.item) {
				is FileTreeCell -> {
					val resource = ResourceView(
						selected,
						resourceViewer,
						selected.file.name,
						{ RandomAccessFile(selected.file, "r") },
						selected.file.classPathProvider()
					)
					if (selected.file.isFile) {
						if (e.middleClick) {
							resourceViewer.push(resource)
						} else {
							resourceViewer.set(resource)
						}
					}
				}
				is ArchiveEntryTreeCell -> {
					val resource = ResourceView(
						selected,
						resourceViewer,
						selected.name,
						selected.bytesProvider,
						selected.classPathProvider()
					)
					if (e.middleClick) {
						resourceViewer.push(resource)
					} else {
						resourceViewer.set(resource)
					}
				}
			}
		}
		
		isVisible = true
	}
}
