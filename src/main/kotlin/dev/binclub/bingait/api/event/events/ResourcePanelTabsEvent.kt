package dev.binclub.bingait.api.event.events

import java.io.DataInput
import javax.swing.JComponent

/**
 * @author cook 16/Oct/2020
 */
data class ResourcePanelTabsEvent(
	val treeItem: Any,
	val resourcePath: String,
	val byteProvider: () -> DataInput,
	val classPathProvider: (String) -> DataInput?,
	val expectsClassFile: Boolean = false,
	val tabs: MutableMap<String, JComponent>
)
