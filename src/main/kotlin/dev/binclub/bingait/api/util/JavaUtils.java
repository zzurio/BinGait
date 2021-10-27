package dev.binclub.bingait.api.util;

/**
 * @author cookiedragon234 16/Sep/2020
 */
public class JavaUtils {
	public static void wait(Object obj) throws InterruptedException {
		obj.wait();
	}
	
	public static void notify(Object obj) {
		obj.notify();
	}
}
