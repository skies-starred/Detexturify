package foo.starred.detexturify.impl.command.impl.filter

import foo.starred.detexturify.annotation.impl.LoadableDetexturifyModule
import foo.starred.detexturify.impl.command.util.command
import foo.starred.detexturify.impl.data.loader.DetexturifyItemDataInstance
import foo.starred.kontext.abstraction.chat.message.MinecraftMessages
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.api.text.parser.impl.parse
import net.minecraft.core.component.DataComponents
import kotlin.jvm.optionals.getOrNull

@LoadableDetexturifyModule
object DetexturifyWhitelistCommands {
    init {
        command {
            "whitelist".then {
                "executes" {
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Whitelist commands:".parse(true))

                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify whitelist list")
                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify whitelist add")
                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify whitelist remove")
                }

                "add" {
                    val held = client.player?.mainHandItem?.takeIf { !it.isEmpty } ?: return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Not holding anything!".parse(true))
                    val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull() ?: return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true))

                    if (DetexturifyItemDataInstance.WHITELIST.value.contains(id)) {
                        return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Item already exists in whitelist!".parse(true))
                    }

                    DetexturifyItemDataInstance.WHITELIST.update { add(id) }
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Successfully added item to whitelist! You may need to change servers to view changes.".parse(true))
                }

                "remove" {
                    val held = client.player?.mainHandItem?.takeIf { !it.isEmpty } ?: return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Not holding anything!".parse(true))
                    val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull() ?: return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true))

                    if (!DetexturifyItemDataInstance.WHITELIST.value.contains(id)) {
                        return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Item does not exist in whitelist!".parse(true))
                    }

                    DetexturifyItemDataInstance.WHITELIST.update { remove(id) }
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Successfully removed item from whitelist! You may need to change servers to view changes.".parse(true))
                }

                "list" {
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Whitelisted items:".parse(true))

                    for (value in DetexturifyItemDataInstance.WHITELIST.value) {
                        MinecraftMessages.display(" <dark_gray>- <green>$value")
                    }
                }
            }
        }
    }
}
