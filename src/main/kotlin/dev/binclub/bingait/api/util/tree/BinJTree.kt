package dev.binclub.bingait.api.util.tree

import dev.binclub.bingait.api.util.BinTreeRenderer
import javax.swing.JTree

/**
 * @author cookiedragon234 12/Sep/2020
 */
open class BinJTree: JTree() {
	init {
		@Suppress("LeakingThis")
		setCellRenderer(BinTreeRenderer())
	}
}
