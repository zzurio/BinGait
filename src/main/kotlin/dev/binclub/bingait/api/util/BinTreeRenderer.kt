package dev.binclub.bingait.api.util

import java.awt.Component
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultTreeCellRenderer

/**
 * @author cookiedragon234 08/Sep/2020
 */
class BinTreeRenderer: DefaultTreeCellRenderer() {
	override fun getTreeCellRendererComponent(
		tree: JTree?,
		value: Any?,
		sel: Boolean,
		expanded: Boolean,
		leaf: Boolean,
		row: Int,
		hasFocus: Boolean
	): Component {
		if (value is BinTreeNode) {
			value.openedIcon?.let(this::setOpenIcon)
			value.closedIcon?.let(this::setClosedIcon)
			
			if (value.texts.isNotEmpty()) {
				val texts = value.texts
				val label = JPanel()
				if (hasFocus) {
					label.background = getBackgroundSelectionColor()
				} else {
					label.background = getBackgroundNonSelectionColor()
				}
				label.layout = GridBagLayout()
				//label.layout = GridLayout(0, texts.size)
				texts.forEach {
					val thisLabel = JLabel(it.text)
					thisLabel.disableHtml()
					it.colour?.let {
						thisLabel.foreground = it
					}
					label.add(thisLabel)
				}
				return label
			}
		}
		val out = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)
		out.maybeCast<JComponent>()?.disableHtml()
		return out
	}
}
