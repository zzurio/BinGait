package dev.binclub.bingait.gui.swing.resource

import java.io.DataInput
import javax.swing.JPanel

/**
 * @author cookiedragon234 08/Sep/2020
 */
class HexEditorPanel(
	val classFileName: String,
	val byteProvider: () -> DataInput,
	val classPathProvider: (String) -> DataInput?
): JPanel() {
}


