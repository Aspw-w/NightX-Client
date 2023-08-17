package net.aspw.client.script.api.global

import net.aspw.client.util.ClientUtils

/**
 * Object used by the script API to provide an easier way of calling chat-related methods.
 */
object Chat {

    /**
     * Prints a message to the chat (client-side)
     * @param message Message to be printed
     */
    @Suppress("unused")
    @JvmStatic
    fun print(message: String) {
        ClientUtils.displayChatMessage(message)
    }
}