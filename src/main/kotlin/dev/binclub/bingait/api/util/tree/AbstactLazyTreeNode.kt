package dev.binclub.bingait.api.util.tree

import dev.binclub.bingait.api.util.EventTreeNode
import java.util.Enumeration
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode

/**
 * @author cookiedragon234 06/Sep/2020
 */
abstract class AbstractLazyTreeNode: DefaultMutableTreeNode(), EventTreeNode {
	private var loaded = false
	protected abstract fun createChildrenNodes(): List<AbstractLazyTreeNode>
	protected open fun willBeLeaf(): Boolean? = null
	
	@Synchronized
	private fun load() {
		if (!loaded) {
			loaded = true
			super.removeAllChildren()
			createChildrenNodes().withIndex().forEach { (i, node) ->
				super.insert(node, i)
			}
		}
	}
	
	@Synchronized
	private fun unload() {
		if (loaded) {
			super.removeAllChildren()
			loaded = false
		}
	}
	
	final override fun isLeaf(): Boolean {
		if (loaded) {
			return super.isLeaf()
		}
		return willBeLeaf() ?: loadAndReturn { super.isLeaf() }
	}
	
	private inline fun <T> loadAndReturn(op: () -> T): T {
		load()
		return op()
	}
	
	override fun onExpansion() = load()
	override fun onCollapse() = unload()
	
	
	override fun getIndex(aChild: TreeNode?): Int = loadAndReturn {
		super.getIndex(aChild)
	}
	
	override fun getChildAt(index: Int): TreeNode = loadAndReturn {
		super.getChildAt(index)
	}
	
	override fun getChildCount(): Int {
		return if (true) {
			loadAndReturn { super.getChildCount() }
		} else {
			if (loaded) super.getChildCount() else {
				if (willBeLeaf() == true) 0 else 1
			}
		}
	}
	
	override fun insert(newChild: MutableTreeNode?, childIndex: Int) = loadAndReturn {
		super.insert(newChild, childIndex)
	}
	
	override fun remove(childIndex: Int) = super.remove(childIndex)
	
	override fun children(): Enumeration<*> = loadAndReturn {
		return super.children()
	}
	
	override fun remove(aChild: MutableTreeNode?) = super.remove(aChild)
	
	override fun add(newChild: MutableTreeNode?) = super.add(newChild)
	
	override fun isNodeDescendant(anotherNode: DefaultMutableTreeNode?): Boolean = loadAndReturn {
		super.isNodeDescendant(anotherNode)
	}
	
	override fun isNodeRelated(aNode: DefaultMutableTreeNode?): Boolean = loadAndReturn {
		super.isNodeRelated(aNode)
	}
	
	override fun getDepth(): Int = loadAndReturn {
		super.getDepth()
	}
	
	override fun getNextNode(): DefaultMutableTreeNode = loadAndReturn {
		super.getNextNode()
	}
	
	override fun getPreviousNode(): DefaultMutableTreeNode = loadAndReturn {
		super.getPreviousNode()
	}
	
	override fun preorderEnumeration(): Enumeration<*> = loadAndReturn {
		super.preorderEnumeration()
	}
	
	override fun postorderEnumeration(): Enumeration<*> = loadAndReturn {
		super.postorderEnumeration()
	}
	
	override fun breadthFirstEnumeration(): Enumeration<*> = loadAndReturn {
		super.breadthFirstEnumeration()
	}
	
	override fun depthFirstEnumeration(): Enumeration<*> = loadAndReturn {
		super.depthFirstEnumeration()
	}
	
	override fun isNodeChild(aNode: TreeNode?): Boolean = loadAndReturn {
		super.isNodeChild(aNode)
	}
	
	override fun getFirstChild(): TreeNode = loadAndReturn {
		super.getFirstChild()
	}
	
	override fun getLastChild(): TreeNode = loadAndReturn {
		super.getLastChild()
	}
	
	override fun getChildAfter(aChild: TreeNode?): TreeNode = loadAndReturn {
		super.getChildAfter(aChild)
	}
	
	override fun getChildBefore(aChild: TreeNode?): TreeNode = loadAndReturn {
		super.getChildBefore(aChild)
	}
	
	override fun getFirstLeaf(): DefaultMutableTreeNode = loadAndReturn {
		super.getFirstLeaf()
	}
	
	override fun getLastLeaf(): DefaultMutableTreeNode = loadAndReturn {
		super.getLastLeaf()
	}
	
	override fun getNextLeaf(): DefaultMutableTreeNode = loadAndReturn {
		super.getNextLeaf()
	}
	
	override fun getPreviousLeaf(): DefaultMutableTreeNode = loadAndReturn {
		super.getPreviousLeaf()
	}
	
	override fun getLeafCount(): Int = loadAndReturn {
		super.getLeafCount()
	}
	
	override fun toString(): String = loadAndReturn {
		super.toString()
	}
	
	override fun equals(other: Any?): Boolean = loadAndReturn {
		super.equals(other)
	}
}
