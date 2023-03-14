package net.aspw.client.features.module.modules.misc

import com.google.gson.JsonParser
import net.aspw.client.Client
import net.aspw.client.event.EventTarget
import net.aspw.client.event.PacketEvent
import net.aspw.client.event.UpdateEvent
import net.aspw.client.features.module.Module
import net.aspw.client.features.module.ModuleCategory
import net.aspw.client.features.module.ModuleInfo
import net.aspw.client.utils.misc.RandomUtils
import net.aspw.client.utils.timer.MSTimer
import net.aspw.client.value.IntegerValue
import net.aspw.client.visual.hud.element.elements.Notification
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemSkull
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemTool
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import org.apache.commons.io.IOUtils
import java.util.*

@ModuleInfo(name = "AuthBypass", spacedName = "Auth Bypass", category = ModuleCategory.MISC)
class AuthBypass : Module() {
    private val delayValue = IntegerValue("Delay", 100, 100, 5000, "ms")

    private var skull: String? = null
    private var type = "none"
    private val packets = ArrayList<Packet<INetHandlerPlayServer>>()
    private val clickedSlot = ArrayList<Int>()
    private val timer = MSTimer()
    private val jsonParser = JsonParser()

    private val brLangMap = HashMap<String, String>()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (packets.isNotEmpty() && timer.hasTimePassed(delayValue.get().toLong())) {
            for (packet in packets) {
                mc.netHandler.addToSendQueue(packet)
            }
            packets.clear()
            Client.hud.addNotification(Notification("Authentication bypassed.", Notification.Type.INFO))
        }
    }

    override fun onEnable() {
        skull = null
        type = "none"
        packets.clear()
        clickedSlot.clear()

        //load locale async
        Thread {
            val localeJson = JsonParser().parse(
                IOUtils.toString(
                    AuthBypass::class.java.classLoader.getResourceAsStream("br_items.json"),
                    "utf-8"
                )
            ).asJsonObject

            brLangMap.clear()
            for ((key, element) in localeJson.entrySet()) {
                brLangMap["item.$key"] = element.asString.lowercase(Locale.getDefault())
            }
        }.start()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S2FPacketSetSlot) {
            val slot = packet.func_149173_d()
            val windowId = packet.func_149175_c()
            val item = packet.func_149174_e()
            if (windowId == 0 || item == null || type == "none" || clickedSlot.contains(slot)) {
                return
            }
            val itemName = item.unlocalizedName

            when (type.lowercase(Locale.getDefault())) {
                "skull" -> {
                    if (itemName.contains("item.skull.char", ignoreCase = true)) {
                        val nbt = item.tagCompound ?: return
                        // val uuid=nbt.get<CompoundTag>("SkullOwner").get<CompoundTag>("Properties").get<ListTag>("textures").get<CompoundTag>(0).get<StringTag>("Value").value
                        val data = process(
                            nbt.getCompoundTag("SkullOwner").getCompoundTag("Properties")
                                .getTagList("textures", NBTTagCompound.NBT_TYPES.indexOf("COMPOUND"))
                                .getCompoundTagAt(0).getString("Value")
                        )
                        if (skull == null) {
                            skull = data
                        } else if (skull != data) {
                            skull = null
                            timer.reset()
                            click(windowId, slot, item)
                        }
                    }
                }

                // special rules lol
                "enchada" -> { // select all
                    click(windowId, slot, item)
                }

                "cabeça" -> { // skulls
                    if (item.item is ItemSkull) {
                        click(windowId, slot, item)
                    }
                }

                "ferramenta" -> { // tools
                    if (item.item is ItemTool) {
                        click(windowId, slot, item)
                    }
                }

                "comida" -> { // foods
                    if (item.item is ItemFood) {
                        click(windowId, slot, item)
                    }
                }

                // the new item check in redesky
                else -> {
                    if (getItemLocalName(item).contains(type)) {
                        click(windowId, slot, item)
                    }
                }
            }
        }
        //silent auth xd
        if (packet is S2DPacketOpenWindow) {
            val windowName = packet.windowTitle.unformattedText
            if (packet.slotCount == 27 && packet.guiId.contains("container", ignoreCase = true)
                && windowName.startsWith("Clique", ignoreCase = true)
            ) {
                type = when {
                    windowName.contains("bloco", ignoreCase = true) -> "skull"
                    else -> {
                        val splited = windowName.split(" ")
                        var str = splited[splited.size - 1].replace(".", "").lowercase(Locale.getDefault())
                        if (str.endsWith("s")) {
                            str = str.substring(0, str.length - 1)
                        }
                        str
                    }
                }
                packets.clear()
                clickedSlot.clear()
                event.cancelEvent()
            } else {
                type = "none"
            }
        }
    }

    private fun click(windowId: Int, slot: Int, item: ItemStack) {
        clickedSlot.add(slot)
        packets.add(C0EPacketClickWindow(windowId, slot, 0, 0, item, RandomUtils.nextInt(114, 514).toShort()))
    }

    private fun getItemLocalName(item: ItemStack): String {
        return brLangMap[item.unlocalizedName] ?: "null"
    }

    private fun process(data: String): String {
        val jsonObject = jsonParser.parse(String(Base64.getDecoder().decode(data))).asJsonObject
        return jsonObject
            .getAsJsonObject("textures")
            .getAsJsonObject("SKIN")
            .get("url").asString
    }
}