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
object DetexturifyBlacklistCommands {
    init {
        command {
            "blacklist".then {
                "executes" {
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Blacklist commands:".parse(true))

                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify blacklist list")
                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify blacklist add")
                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify blacklist remove")
                }

                "add" {
                    val held = client.player?.mainHandItem?.takeIf { !it.isEmpty } ?: return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Not holding anything!".parse(true))
                    val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull() ?: return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true))

                    if (DetexturifyItemDataInstance.BLACKLIST.value.contains(id)) {
                        return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Item already exists in blacklist!".parse(true))
                    }

                    DetexturifyItemDataInstance.BLACKLIST.update { add(id) }
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Successfully added item to blacklist! You may need to change servers to view changes.".parse(true))
                }

                "remove" {
                    val held = client.player?.mainHandItem?.takeIf { !it.isEmpty } ?: return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Not holding anything!".parse(true))
                    val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull() ?: return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true))

                    if (!DetexturifyItemDataInstance.BLACKLIST.value.contains(id)) {
                        return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Item does not exist in blacklist!".parse(true))
                    }

                    DetexturifyItemDataInstance.BLACKLIST.update { remove(id) }
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Successfully removed item from blacklist! You may need to change servers to view changes.".parse(true))
                }

                "list" {
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Blacklisted items:".parse(true))

                    for (value in DetexturifyItemDataInstance.BLACKLIST.value) {
                        MinecraftMessages.display(" <dark_gray>- <green>$value")
                    }
                }
            }
        }
    }
}
