package net.aspw.client.features.command.commands

import net.aspw.client.Client
import net.aspw.client.features.command.Command
import net.aspw.client.features.command.CommandManager
import net.aspw.client.features.module.modules.visual.Hud
import net.aspw.client.utils.misc.sound.TipSoundManager
import net.aspw.client.visual.client.clickgui.NewUi
import net.aspw.client.visual.font.Fonts

class ReloadCommand : Command("reload", arrayOf("configreload")) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        Client.commandManager = CommandManager()
        Client.commandManager.registerCommands()
        for (module in Client.moduleManager.modules)
            Client.moduleManager.generateCommand(module)
        Fonts.loadFonts()
        Client.tipSoundManager = TipSoundManager()
        Client.fileManager.loadConfig(Client.fileManager.modulesConfig)
        Client.fileManager.loadConfig(Client.fileManager.valuesConfig)
        Client.fileManager.loadConfig(Client.fileManager.accountsConfig)
        Client.fileManager.loadConfig(Client.fileManager.friendsConfig)
        Client.fileManager.loadConfig(Client.fileManager.xrayConfig)
        Client.fileManager.loadConfig(Client.fileManager.hudConfig)
        NewUi.resetInstance()
        if (Client.moduleManager.getModule(Hud::class.java)?.flagSoundValue!!.get()) {
            Client.tipSoundManager.popSound.asyncPlay(90f)
        }
        chat("Reloaded!")
    }
}
