package dev.binclub.bingait.api.event.events

/**
 * @author cookiedragon234 16/Sep/2020
 */
data class StatusUpdateEvent(
	val activity: ActivityState?,
	val message: String?
)

enum class ActivityState {
	CLASSTREE,
	BINCODE_AST,
	CFRTEXT,
	CFRTREE,
	HEX
}
