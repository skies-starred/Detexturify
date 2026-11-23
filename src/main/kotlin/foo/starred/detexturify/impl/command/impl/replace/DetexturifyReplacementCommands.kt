package foo.starred.detexturify.impl.command.impl.replace

import foo.starred.detexturify.annotation.impl.LoadableDetexturifyModule
import foo.starred.detexturify.impl.command.util.command
import foo.starred.detexturify.impl.data.loader.DetexturifyItemDataInstance
import foo.starred.kontext.abstraction.chat.message.MinecraftMessages
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.api.text.parser.impl.parse
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import kotlin.jvm.optionals.getOrNull

@LoadableDetexturifyModule
object DetexturifyReplacementCommands {
    init {
        command {
            "replace".then {
                executes {
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Replacement commands:".parse(true))

                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify replace list")
                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify replace <skyblock_id> <item>")
                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify replace remove <skyblock_id>")
                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify replace clear")
                }

                "list" {
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Custom replacements:".parse(true))

                    for ((key, value) in DetexturifyItemDataInstance.REPLACEMENTS.value) {
                        MinecraftMessages.display(" <dark_gray>- <green>$key <dark_gray>-> <yellow>$value")
                    }
                }

                "clear" {
                    val count = DetexturifyItemDataInstance.REPLACEMENTS.value.size
                    DetexturifyItemDataInstance.REPLACEMENTS.update { clear() }

                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Cleared <green>$count<r> replacements! You may need to change servers to view changes.".parse(true))
                }

                "remove" {
                    string("id") {
                        val id = string("id").replace(':', '-')
                        if (!DetexturifyItemDataInstance.REPLACEMENTS.value.containsKey(id)) return@string MinecraftMessages.display("<#FAB387>[Detexturify]<r> No replacement found for '<red>$id<r>'!".parse(true))

                        DetexturifyItemDataInstance.REPLACEMENTS.update { remove(id) }
                        MinecraftMessages.display("<#FAB387>[Detexturify]<r> Successfully removed replacement for '<green>$id<r>'! You may need to change servers to view changes.".parse(true))
                    }.suggests { DetexturifyItemDataInstance.REPLACEMENTS.value.keys }
                }

                "add" / "withId" / string("id").suggests { DetexturifyItemDataInstance.MAP.keys }.then {
                    string("model") {
                        val id = string("id").replace(':', '-')
                        val model = string("model")

                        DetexturifyItemDataInstance.REPLACEMENTS.update { put(id, model) }
                        MinecraftMessages.display("<#FAB387>[Detexturify]<r> Successfully replaced '<green>$id<r>' with '<yellow>$model<r>'! You may need to change servers to view changes.".parse(true))
                    }.suggests { BuiltInRegistries.ITEM.keySet().map { it.path } }
                }

                "add" / "held" / string("model") {
                    val held = client.player?.mainHandItem?.takeIf { !it.isEmpty } ?: return@string MinecraftMessages.display("<#FAB387>[Detexturify]<r> Not holding anything!".parse(true))
                    val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull()?.replace(':', '-') ?: return@string MinecraftMessages.display("<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true))
                    val model = string("model")

                    DetexturifyItemDataInstance.REPLACEMENTS.update { put(id, model) }
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Successfully replaced '<green>$id<r>' with '<yellow>$model<r>'! You may need to change servers to view changes.".parse(true))
                }.suggests { BuiltInRegistries.ITEM.keySet().map { it.path } }
            }
        }
    }
}
