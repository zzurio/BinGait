package dev.binclub.bingait.api.util

import javafx.stage.Screen
import java.awt.GraphicsEnvironment
import java.awt.MouseInfo
import java.awt.Rectangle

/**
 * @author cookiedragon234 28/May/2020
 */
fun findActiveScreenJFX(): Screen {
	val (mouseX, mouseY) = MouseInfo.getPointerInfo().location.let {
		it.x.toDouble() to it.y.toDouble()
	}
	
	Screen.getScreens().forEach { screen ->
		val bounds = screen.bounds
		if (bounds.contains(mouseX, mouseY)) {
			return screen
		}
	}
	return Screen.getPrimary()
}

fun findActiveScreen(): Rectangle {
	val mouse = MouseInfo.getPointerInfo().location
	
	val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
	
	ge.screenDevices.forEach { device ->
		val bounds = device.defaultConfiguration.bounds
		if (bounds.contains(mouse)) {
			return bounds
		}
	}
	
	return ge.screenDevices[0].defaultConfiguration.bounds
}
