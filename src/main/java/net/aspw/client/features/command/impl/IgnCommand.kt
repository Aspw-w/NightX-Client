package net.aspw.client.features.command.impl

import net.aspw.client.Client
import net.aspw.client.features.command.Command
import net.aspw.client.features.module.impl.visual.Interface
import net.aspw.client.visual.hud.element.elements.Notification
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class IgnCommand : Command("ign", emptyArray()) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        val username = mc.thePlayer.name

        if (Client.moduleManager.getModule(Interface::class.java)?.flagSoundValue!!.get()) {
            Client.tipSoundManager.popSound.asyncPlay(Client.moduleManager.popSoundPower)
        }
        Client.hud.addNotification(
            Notification(
                "Copied Username: §a${username}",
                Notification.Type.SUCCESS
            )
        )

        val stringSelection = StringSelection(username)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(stringSelection, stringSelection)
    }
}