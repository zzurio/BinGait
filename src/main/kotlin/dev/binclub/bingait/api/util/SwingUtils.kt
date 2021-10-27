@file:Suppress("NOTHING_TO_INLINE")

package dev.binclub.bingait.api.util

import java.awt.GridLayout
import javax.swing.*
import javax.swing.event.MenuEvent
import javax.swing.event.MenuListener
import javax.swing.tree.TreePath


/**
 * @author cookiedragon234 07/Sep/2020
 */
fun textComponent(text: String, horizontalAlignment: Int = JLabel.CENTER): JComponent {
	val panel = JPanel(false)
	val filler = JLabel(text)
	filler.horizontalAlignment = horizontalAlignment
	panel.layout = GridLayout(1, 1)
	panel.add(filler)
	return panel
}

inline fun JMenu.menu(name: String, op: JMenu.() -> Unit): JMenu {
	val menu = JMenu(name)
	op(menu)
	add(menu)
	return menu
}

inline fun JMenu.lazyMenu(name: String, crossinline op: JMenu.() -> Unit): JMenu {
	val menu = JMenu(name)
	menu.addMenuListener(object: MenuListener {
		override fun menuSelected(e: MenuEvent?) {
			op(menu)
		}
		
		override fun menuDeselected(e: MenuEvent?) {
			menu.removeAll()
		}
		
		override fun menuCanceled(e: MenuEvent?) {
			menu.removeAll()
		}
	})
	add(menu)
	return menu
}

inline fun JMenuBar.menu(name: String, op: JMenu.() -> Unit): JMenu {
	val menu = JMenu(name)
	op(menu)
	add(menu)
	return menu
}

inline fun JMenu.item(name: String, crossinline op: JMenuItem.() -> Unit): JMenuItem {
	val menu = JMenuItem(name)
	menu.addActionListener { event -> op() }
	add(menu)
	return menu
}

fun JTree.getHorizontalPathForLocation(x: Int, y: Int): TreePath? {
	val closestPath = getClosestPathForLocation(x, y)
	if (closestPath != null) {
		val pathBounds = getPathBounds(closestPath)
		if (pathBounds != null && y >= pathBounds.y && y < pathBounds.y + pathBounds.height) return closestPath
	}
	return null
}
