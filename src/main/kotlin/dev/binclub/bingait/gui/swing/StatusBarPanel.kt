package dev.binclub.bingait.gui.swing

import dev.binclub.bingait.api.event.EventSystem
import dev.binclub.bingait.api.event.events.BackgroundStatusUpdateEvent
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.lang.management.ManagementFactory
import java.text.DecimalFormat
import javax.swing.*


/**
 * @author cookiedragon234 11/Sep/2020
 */
class StatusBarPanel: JPanel() {
	init {
		layout = GridLayout(1, 3)
		
		val text = JLabel()
		text.horizontalAlignment = SwingConstants.LEFT
		add(text)
		EventSystem.register { event: BackgroundStatusUpdateEvent ->
			text.text = event.newStatus
		}
		
		val pbPanel = JPanel()
		pbPanel.add(object: JProgressBar(), ActionListener {
			var timer = Timer(5000, this)
			private val df = DecimalFormat("#.##")
			
			init {
				timer.start()
				isStringPainted = true
			}
			
			override fun paint(g: Graphics?) {
				val heap = ManagementFactory.getMemoryMXBean().heapMemoryUsage
				
				value = ((heap.used / heap.max) * 100).toInt()
				string = "${df.format(heap.used * 1e-6)}MB / ${df.format(heap.max * 1e-6)}MB"
				
				super.paint(g)
			}
			
			override fun actionPerformed(e: ActionEvent) {
				if (e.source == timer) {
					repaint()
				}
			}
		})
		add(pbPanel)
		
		add(JPanel())
	}
}
