package dev.binclub.bingait.gui.swing

import dev.binclub.bingait.api.BingaitThreadpool
import dev.binclub.bingait.api.event.events.ActivityState
import dev.binclub.bingait.api.event.EventSystem
import dev.binclub.bingait.api.event.events.ResourcePanelTabsEvent
import dev.binclub.bingait.api.event.events.StatusUpdateEvent
import dev.binclub.bingait.gui.swing.resource.BincodeAstPanel
import dev.binclub.bingait.api.util.maybeCast
import dev.binclub.bingait.api.util.textComponent
import dev.binclub.bingait.storage.BingaitConfig
import hk.quantr.peterswing.advancedswing.jclosabletabbedpane.JClosableTabbedPane
import java.awt.BorderLayout
import java.awt.GridLayout
import java.io.DataInput
import java.util.NavigableSet
import java.util.TreeMap
import java.util.concurrent.Future
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTabbedPane

/**
 * @author cookiedragon234 07/Sep/2020
 */
class ResourceViewerPanel: JPanel() {
	private var tabPane = JClosableTabbedPane()
	
	fun set(resourceView: ResourceView) {
		tabPane.removeAll()
		push(resourceView)
	}
	
	fun push(resourceView: ResourceView) {
		val shortName = resourceView.resourcePath.substringAfterLast('/').substringBeforeLast(".class")
		tabPane.addTab(shortName, resourceView)
		tabPane.selectedIndex = tabPane.tabCount - 1
		resourceView.pushState()
	}
	
	init {
		layout = GridLayout()
		add(tabPane, BorderLayout.NORTH)
		tabPane.preferredSize.width = size.width
		tabPane.addChangeListener {
			val source = it.source
			source.maybeCast<ResourceView>()?.pushState()
		}
	}
}

class ResourceView(
	node: Any,
	parent: ResourceViewerPanel,
	val resourcePath: String,
	byteProvider: () -> DataInput,
	classPathProvider: (String) -> DataInput?,
	expectsClassFile: Boolean = false
): JPanel() {
	private var tabPane = JTabbedPane()
	private val future: Future<*>
	
	init {
		layout = GridLayout()
		
		tabPane.preferredSize.width = size.width
		
		val tabs = TreeMap<String, JComponent>()
		future = BingaitThreadpool.submitTask("Populate Resource Viewer") {
			tabs["BinCode AST"] = BincodeAstPanel(resourcePath, byteProvider, classPathProvider)
			EventSystem.dispatch(
				ResourcePanelTabsEvent(
					node,
					resourcePath,
					byteProvider,
					classPathProvider,
					expectsClassFile,
					tabs
				)
			)
			tabs.forEach(tabPane::addTab)
			
			val preferredIndex = BingaitConfig.preferredResourceView?.let(tabs.keys::indexOf) ?: -1
			if (preferredIndex >= 0) {
				tabPane.selectedIndex = preferredIndex
			}
		}
		
		tabPane.addChangeListener {
			println("Tab pane selected ${tabPane.selectedComponent}")
			tabs.keys.forEachIndexed { index, s ->
				if (index == tabPane.selectedIndex) {
					BingaitConfig.preferredResourceView = s
				}
			}
			pushState()
		}
		
		add(tabPane, BorderLayout.NORTH)
	}
	
	fun pushState() {
		EventSystem.dispatch(
			StatusUpdateEvent(
				getActivityState(),
				"Viewing ${resourcePath.substringAfterLast('/')}"
			), true
		)
	}
	
	private fun getActivityState(): ActivityState? {
		return when (tabPane.selectedIndex) {
			0 -> ActivityState.BINCODE_AST
			1 -> ActivityState.CFRTEXT
			2 -> ActivityState.CFRTREE
			3 -> ActivityState.HEX
			else -> null
		}
	}
	
	override fun removeNotify() {
		super.removeNotify()
		println("Removing resource $resourcePath")
		this.future.cancel(true)
		BingaitThreadpool.currentTask = null
	}
}
