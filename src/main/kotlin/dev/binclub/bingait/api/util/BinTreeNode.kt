package dev.binclub.bingait.api.util

import java.awt.Color
import javax.swing.Icon
import javax.swing.tree.DefaultMutableTreeNode

/**
 * @author cookiedragon234 08/Sep/2020
 */
class BinTreeNode(
	text: String? = null,
	val texts: MutableList<BinTreeText> = ArrayList()
): DefaultMutableTreeNode(text), Iterable<BinTreeText> {
	constructor(texts: MutableList<BinTreeText>): this(null, texts)
	
	var closedIcon: Icon? = null
	var openedIcon: Icon? = null
	
	fun setIcon(icon: Icon?) {
		closedIcon = icon
		openedIcon = icon
	}
	
	fun push(text: String, colour: Color? = null) = texts.add(BinTreeText(text, colour))
	
	override fun iterator(): Iterator<BinTreeText> = texts.iterator()
}

data class BinTreeText(
	val text: String,
	val colour: Color? = null
)
