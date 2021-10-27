package dev.binclub.bingait.gui.swing.resource

import dev.binclub.bincode.Bincode
import dev.binclub.bincode.types.ClassFile
import dev.binclub.bincode.types.annotation.Annotation
import dev.binclub.bincode.types.attributes.*
import dev.binclub.bincode.types.attributes.types.code.CodeAttribute
import dev.binclub.bincode.types.constantpool.ConstantPoolReference
import dev.binclub.bincode.types.constantpool.PrimitiveConstant
import dev.binclub.bincode.types.constantpool.constants.*
import dev.binclub.bingait.api.util.tree.BinJTree
import dev.binclub.bingait.api.util.cast
import dev.binclub.bingait.api.util.removeBingait
import java.awt.GridLayout
import java.io.DataInput
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import kotlin.properties.Delegates

/**
 * @author cookiedragon234 07/Sep/2020
 */
class BincodeAstPanel(
	val classFileName: String,
	val byteProvider: () -> DataInput,
	val classPathProvider: (String) -> DataInput?
): JPanel() {
	private var classFile: ClassFile by Delegates.notNull()
	
	init {
		layout = GridLayout()
		
		try {
			classFile = Bincode.parse(byteProvider())
			
			val tree = BinJTree()
			tree.model = DefaultTreeModel(DefaultMutableTreeNode())
			val root = tree.model.cast<DefaultTreeModel>().root.cast<DefaultMutableTreeNode>()
			
			tree.isRootVisible = true
			tree.showsRootHandles = true
			
			root.add(DefaultMutableTreeNode("Version: ${classFile.version}"))
			
			val cpNode = DefaultMutableTreeNode("Constant Pool")
			root.add(cpNode)
			classFile.constantPool.forEachIndexed { index, constant ->
				cpNode.add(DefaultMutableTreeNode("#$index:   $constant"))
			}
			
			root.add(DefaultMutableTreeNode("Access: ${classFile.access}"))
			root.add(DefaultMutableTreeNode("Name: ${formatCpRef(classFile.thisClass)}"))
			root.add(DefaultMutableTreeNode("Parent: ${formatCpRef(classFile.superClass)}"))
			
			val interfacesNode = DefaultMutableTreeNode("Interfaces")
			root.add(interfacesNode)
			classFile.interfaces.forEach { inter ->
				interfacesNode.add(DefaultMutableTreeNode(formatCpRef(inter)))
			}
			
			val fieldsNode = DefaultMutableTreeNode("Fields")
			root.add(fieldsNode)
			classFile.fields.forEach { field ->
				val fieldNode = DefaultMutableTreeNode(
					"${field.name}.${field.descriptor} " +
					"(${tryGetConstantValue(field.name.index)}.${tryGetConstantValue(field.descriptor.index)})"
				)
				fieldsNode.add(fieldNode)
				
				fieldNode.add(DefaultMutableTreeNode("Access: ${field.accessFlags}"))
				
				val attribsNode = DefaultMutableTreeNode("Attributes")
				fieldNode.add(attribsNode)
				field.attributes.forEach { attrib ->
					attribsNode.add(attributeToTreeNode(attrib))
				}
			}
			
			val methodsNode = DefaultMutableTreeNode("Methods")
			root.add(methodsNode)
			classFile.methods.forEach { method ->
				val methodNode = DefaultMutableTreeNode(
					"${method.name}.${method.descriptor} " +
					"(${tryGetConstantValue(method.name.index)}.${tryGetConstantValue(method.descriptor.index)})"
				)
				methodsNode.add(methodNode)
				
				methodNode.add(DefaultMutableTreeNode("Access: ${method.accessFlags}"))
				
				val attribsNode = DefaultMutableTreeNode("Attributes")
				methodNode.add(attribsNode)
				method.attributes.forEach { attrib ->
					attribsNode.add(attributeToTreeNode(attrib))
				}
			}
			
			val attribsNode = DefaultMutableTreeNode("Attributes")
			root.add(attribsNode)
			classFile.attributes.forEach { attrib ->
				attribsNode.add(attributeToTreeNode(attrib))
			}
			
			tree.expandRow(0)
			add(JScrollPane(tree))
		} catch (t: Throwable) {
			t.removeBingait()
			
			val sw = StringWriter()
			t.printStackTrace(PrintWriter(sw))
			val error = sw.toString()
			add(JScrollPane(JTextArea(error).apply {
				isEditable = false
			}))
		}
	}
	
	private fun attributeToTreeNode(attrib: Attribute): DefaultMutableTreeNode {
		val out = DefaultMutableTreeNode(formatCpRef(attrib.name))
		when (attrib) {
			is CodeAttribute -> {
				out.add(DefaultMutableTreeNode("MaxStack: ${attrib.maxStack}"))
				out.add(DefaultMutableTreeNode("MaxLocals: ${attrib.maxLocals}"))
				
				val insnsNode = DefaultMutableTreeNode("Instructions")
				out.add(insnsNode)
				attrib.instructions.forEach { insn ->
					insnsNode.add(DefaultMutableTreeNode(insn.toString()))
				}
				
				val trysNode = DefaultMutableTreeNode("TryCatchBlocks")
				out.add(trysNode)
				attrib.tryCatchBlocks.forEachIndexed { index, tcb ->
					val tcbNode = DefaultMutableTreeNode(index.toString())
					trysNode.add(tcbNode)
					tcbNode.add(DefaultMutableTreeNode("Start: ${tcb.start}"))
					tcbNode.add(DefaultMutableTreeNode("End: ${tcb.end}"))
					tcbNode.add(DefaultMutableTreeNode("Handler: ${tcb.handler}"))
					tcbNode.add(DefaultMutableTreeNode("Catch: CP#${tcb.catch} (${tryGetConstantValue(tcb.catch?.index ?: -1)})"))
				}
				
				val attribsNode = DefaultMutableTreeNode("Attributes")
				out.add(attribsNode)
				attrib.attributes.forEach { attrib ->
					attribsNode.add(attributeToTreeNode(attrib))
				}
			}
			is ConstantValueAttribute -> {
				out.add(DefaultMutableTreeNode("Value: ${formatCpRef(attrib.constantIndex)}"))
			}
			is DeprecatedAttribute -> {
			}
			is InnerClassesAttribute -> {
				attrib.classes.forEachIndexed { index, inner ->
					val classesNode = DefaultMutableTreeNode(index.toString())
					out.add(classesNode)
					classesNode.add(DefaultMutableTreeNode("InnerClass: ${tryGetConstantValue(inner.innerClassInfoIndex.index)}"))
					classesNode.add(DefaultMutableTreeNode("OuterClass: ${tryGetConstantValue(inner.outerClassInfoIndex.index)}"))
					classesNode.add(DefaultMutableTreeNode("InnerName: ${tryGetConstantValue(inner.innerNameIndex.index)}"))
					classesNode.add(DefaultMutableTreeNode("InnerAccess: ${inner.innerClassAccessFlags}"))
				}
			}
			is LineNumberTableAttribute -> {
				attrib.lineNumbers.forEachIndexed { index, line ->
					val linesNode = DefaultMutableTreeNode(index.toString())
					out.add(linesNode)
					linesNode.add(DefaultMutableTreeNode("Start: ${line.start}"))
					linesNode.add(DefaultMutableTreeNode("LineNumber: ${line.lineNumber}"))
				}
			}
			is LocalVariableTableAttribute -> {
				attrib.variables.forEachIndexed { index, variable ->
					val varNode = DefaultMutableTreeNode(index.toString())
					out.add(varNode)
					varNode.add(DefaultMutableTreeNode("Start: ${variable.start}"))
					varNode.add(DefaultMutableTreeNode("Length: ${variable.length}"))
					varNode.add(DefaultMutableTreeNode("Name: ${formatCpRef(variable.nameRef)}"))
					varNode.add(DefaultMutableTreeNode("Descriptor: ${formatCpRef(variable.descriptorRef)}"))
					varNode.add(DefaultMutableTreeNode("Index: ${variable.index}"))
				}
			}
			is RuntimeInvisibleAnnotationsAttribute -> {
				annotationsToTreeNode(attrib.annotations, out)
			}
			is RuntimeInvisibleTypeAnnotationsAttribute -> {
				annotationsToTreeNode(attrib.annotations, out)
			}
			is RuntimeVisibleAnnotationsAttribute -> {
				annotationsToTreeNode(attrib.annotations, out)
			}
			is RuntimeVisibleTypeAnnotationsAttribute -> {
				annotationsToTreeNode(attrib.annotations, out)
			}
			is SignatureAttribute -> {
				out.add(DefaultMutableTreeNode(formatCpRef(attrib.signatureIndex)))
			}
			is SyntheticAttribute -> {
			}
			is SourceFileAttribute -> {
				out.add(DefaultMutableTreeNode(formatCpRef(attrib.sourceFileIndex)))
			}
			else -> out.add(DefaultMutableTreeNode("[Unknown Format]"))
		}
		return out
	}
	
	private fun annotationsToTreeNode(annotations: Array<Annotation>, addTo: DefaultMutableTreeNode) {
		annotations.forEach { annotation ->
			val node = DefaultMutableTreeNode(formatCpRef(annotation.typeIndex))
			annotation.pairs.forEach { (ref, value) ->
				val key = DefaultMutableTreeNode(formatCpRef(ref))
				node.add(key)
				key.add(DefaultMutableTreeNode(value.toString()))
			}
		}
	}
	
	private fun formatCpRef(cpRef: ConstantPoolReference<*>): String =
		"CP#${cpRef.index} (${tryGetConstantValue(cpRef.index)})"
	
	private fun tryGetConstantValue(index: Int): String {
		try {
			return when (val constant = classFile.constantPool[index]) {
				is ClassConstant -> tryGetConstantValue(constant.nameRef.index)
				is DoubleConstant -> constant.value.toString()
				is FieldRefConstant ->
					"${tryGetConstantValue(constant.nameAndTypeRef.index)}.${tryGetConstantValue(constant.nameAndTypeRef.index)}"
				is FloatConstant -> constant.value.toString()
				is IntegerConstant -> constant.value.toString()
				is InterfaceMethodRefConstant ->
					"${tryGetConstantValue(constant.classRef.index)}.${tryGetConstantValue(constant.nameAndTypeRef.index)}"
				is InvokeDynamicConstant ->
					"${tryGetConstantValue(constant.bootstrapMethodIndex)}.${tryGetConstantValue(constant.nameAndType.index)}"
				is LongConstant -> constant.value.toString()
				is MethodHandleConstant -> "${constant.referenceKind} ${tryGetConstantValue(constant.referenceRef.index)}"
				is MethodRefConstant ->
					"${tryGetConstantValue(constant.classRef.index)}.${tryGetConstantValue(constant.nameAndTypeRef.index)}"
				is NameAndTypeConstant ->
					"${tryGetConstantValue(constant.nameIndex.index)}${tryGetConstantValue(constant.descriptorIndex.index)}"
				is PrimitiveConstant<*> -> constant.value.toString()
				is StringConstant -> tryGetConstantValue(constant.stringRef.index)
				is Utf8Constant -> constant.value
				else -> constant.toString()
			}
		} catch (t: Throwable) {
			return "?"
		}
	}
}
