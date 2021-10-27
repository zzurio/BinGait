package dev.binclub.bingait.api

/**
 * @author cook 17/Dec/2020
 */
enum class ResourceType {
	TEXT,
	CLASS,
	UNKNOWN;
	
	companion object {
		fun fromFileExtension(extension: String) {
			when (extension.toLowerCase()) {
				"class" -> CLASS
				"txt", "mf", "json", "xml", "properties", "cfg" -> TEXT
				else -> UNKNOWN
			}
		}
	}
}
