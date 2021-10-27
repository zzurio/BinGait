package dev.binclub.bingait.api.util.tree

import dev.binclub.bingait.api.util.removeDuplicatesOfChar
import java.io.DataInput
import java.io.DataInputStream
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import javax.swing.tree.DefaultMutableTreeNode

/**
 * @author cookiedragon234 07/Sep/2020
 */
fun fileToTreeCell(file: File): AbstractLazyTreeNode =
	when {
		file.isDirectory -> FileTreeCell(file) // maybe different type for directories?
		file.extension == "jar" -> ArchiveTreeCell(file)
		file.extension == "zip" -> ArchiveTreeCell(file)
		else -> FileTreeCell(file)
	}

class FileTreeCell(val file: File, val name: String = file.name): AbstractLazyTreeNode() {
	override fun createChildrenNodes(): List<AbstractLazyTreeNode> =
		if (file.isFile) emptyList() else file.listFiles()?.map(::fileToTreeCell) ?: emptyList()
	
	@Suppress("ConvertLambdaToReference")
	private val hasChildren by lazy {
		Files.newDirectoryStream(file.toPath()).iterator().hasNext()
	}
	
	override fun willBeLeaf(): Boolean? = !file.isDirectory || !hasChildren
	
	override fun toString(): String = name
}

class ArchiveTreeCell(
	private val name: String,
	private val file: File,
	private val zipFile: ZipFile
): AbstractLazyTreeNode() {
	constructor(file: File): this(file.name, file, ZipFile(file))
	
	override fun createChildrenNodes(): List<AbstractLazyTreeNode> {
		for (entry in zipFile.entries()) {
			if (entry.size == 0L) { // directory
			} else {
				val name = entry.name.removeDuplicatesOfChar('/').replace("\u0000", "")
				val dirs = name.split('/')
				
				var parentNode: DefaultMutableTreeNode = this
				dirLoop@ for ((i, dir) in dirs.withIndex()) {
					if (i == dirs.size - 1) {
						// Last element, the zip file name
						parentNode.add(ArchiveEntryTreeCell(entry.name, dir, file, zipFile, entry))
					} else {
						for (child in parentNode.children()) {
							if (child is ArchiveDirectoryTreeCell && child.name == dir) {
								parentNode = child
								continue@dirLoop
							}
						}
						
						parentNode = ArchiveDirectoryTreeCell(dir).also(parentNode::add)
					}
				}
			}
		}
		return listOf()
	}
	
	override fun willBeLeaf(): Boolean? = zipFile.size() < 1
	override fun toString(): String = name
}

class ArchiveDirectoryTreeCell(val name: String): DefaultMutableTreeNode() {
	override fun toString(): String = name
}

class ArchiveEntryTreeCell(
	val name: String, // Actual name of the entry
	private val displayName: String, // Display name of the entry, probably with escaped characters etc
	val owningFile: File,
	private val owningArchive: ZipFile,
	private val entry: ZipEntry
): DefaultMutableTreeNode() {
	override fun toString(): String = displayName
	
	val bytesProvider: () -> DataInput = {
		DataInputStream(owningArchive.getInputStream(entry))
	}
	
	fun classPathProvider(): (String) -> DataInput? = owningArchive.classPathProvider()
}

fun ZipFile.classPathProvider(): (String) -> DataInput? = { resource ->
	getEntry(resource)?.let { DataInputStream(getInputStream(it)) }
}
