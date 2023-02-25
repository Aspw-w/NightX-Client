package net.aspw.client.features.module.modules.client;

import net.aspw.client.features.module.Module;
import net.aspw.client.features.module.ModuleCategory;
import net.aspw.client.features.module.ModuleInfo;
import net.aspw.client.value.ListValue;

@ModuleInfo(name = "ClientSpoof", spacedName = "Client Spoof", category = ModuleCategory.CLIENT, forceNoSound = true, onlyEnable = true, array = false)
public final class ClientSpoof extends Module {
    public final ListValue modeValue = new ListValue("Mode", new String[]{
            "Vanilla",
            "Forge",
            "Lunar",
            "LabyMod",
            "CheatBreaker",
            "PvPLounge"
    }, "Vanilla");
}