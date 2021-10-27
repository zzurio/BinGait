package dev.binclub.bingait.gui.swing

import dev.binclub.bingait.api.BingaitThreadpool
import dev.binclub.bingait.api.event.events.ClassTreeItemSelectedEvent
import dev.binclub.bingait.api.event.EventSystem
import dev.binclub.bingait.api.util.tree.BinJTree
import dev.binclub.bingait.api.util.tree.FileTreeCell
import dev.binclub.bingait.api.util.tree.fileToTreeCell
import dev.binclub.bingait.api.util.EventTreeNode
import dev.binclub.bingait.api.util.cast
import dev.binclub.bingait.api.util.getHorizontalPathForLocation
import dev.binclub.bingait.storage.BingaitConfig
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.io.File
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel


/**
 * @author cookiedragon234 06/Sep/2020
 */
class ClassTreePanel: BinJTree() {
	init {
		val tree = this
		val root = DefaultMutableTreeNode()
		val model = DefaultTreeModel(root)
		this.model = model
		
		isRootVisible = false
		showsRootHandles = true
		
		var middleClick = false
		addTreeSelectionListener { e ->
			val path = e.path
			val row = tree.getRowForPath(path)
			if (row != -1) {
				EventSystem.dispatch(
					ClassTreeItemSelectedEvent(
						e.path.lastPathComponent!!,
						middleClick
					)
				)
				middleClick = false
			}
		}
		
		listenerList.add(TreeExpansionListener::class.java, object: TreeExpansionListener {
			override fun treeExpanded(event: TreeExpansionEvent) {
				val last = event.path.lastPathComponent
				if (last is EventTreeNode) {
					last.onExpansion()
				}
			}
			
			override fun treeCollapsed(event: TreeExpansionEvent) {
				val last = event.path.lastPathComponent
				if (last is EventTreeNode) {
					last.onCollapse()
				}
			}
		})
		dropTarget = object: DropTarget() {
			@Synchronized
			override fun drop(evt: DropTargetDropEvent) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE)
					val droppedFiles = evt.transferable
						.getTransferData(DataFlavor.javaFileListFlavor)
						.cast<List<File>>()
					add(droppedFiles.toTypedArray())
				} catch (t: Throwable) {
					t.printStackTrace()
				}
			}
		}
		
		addMouseListener(object: MouseListener {
			override fun mouseClicked(e: MouseEvent) {
				middleClick = false
				if (SwingUtilities.isRightMouseButton(e)) {
					val row = getClosestRowForLocation(e.x, e.y)
					if (row < 0) return
					
					tree.setSelectionRow(row)
					val popup = JPopupMenu()
					popup.add(JMenuItem("Remove").apply {
						addActionListener {
							val path = tree.getPathForRow(row).path
							val last = path[path.size - 1].cast<DefaultMutableTreeNode>()
							model.removeNodeFromParent(last)
						}
					})
					popup.show(tree, e.x, e.y)
				} else if (SwingUtilities.isMiddleMouseButton(e)) {
					middleClick = true
					val selPath = tree.getHorizontalPathForLocation(e.x, e.y)
					tree.selectionPath = selPath
					val selRow = tree.getRowForPath(selPath)
					if (selRow > -1) {
						tree.setSelectionRow(selRow)
					}
					middleClick = false
				}
			}
			
			override fun mousePressed(e: MouseEvent?) {}
			override fun mouseReleased(e: MouseEvent?) {}
			override fun mouseEntered(e: MouseEvent?) {}
			override fun mouseExited(e: MouseEvent?) {}
		})
	}
	
	fun add(files: Array<File>) {
		val model = this.model.cast<DefaultTreeModel>()
		val root = model.root.cast<DefaultMutableTreeNode>()
		for (file in files) {
			BingaitThreadpool.submitTask("Indexing ${file.name}") {
				val childCount = root.childCount
				val cell = fileToTreeCell(file)
				model.insertNodeInto(cell, root, childCount)
				model.reload(cell)
				if (childCount == 0) {
					model.reload()
				}
				expandRow(0)
			}
		}
	}
}
