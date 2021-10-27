package dev.binclub.bingait.gui.swing.resource

import java.io.DataInput
import javax.swing.JPanel

/**
 * @author cook 17/Dec/2020
 */
class RawTextPanel(
	val classFileName: String,
	val byteProvider: () -> DataInput,
	val classPathProvider: (String) -> DataInput?
): JPanel() {

}
