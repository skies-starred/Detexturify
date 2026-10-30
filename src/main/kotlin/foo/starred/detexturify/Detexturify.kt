@file:Suppress("ConstPropertyName", "Unused")

package foo.starred.detexturify

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import foo.starred.detexturify.config.DetexturifyConfig
import foo.starred.detexturify.data.SkyBlockItem
import foo.starred.detexturify.pack.HypixelPackCache
import foo.starred.detexturify.utils.NetworkUtils.request
import foo.starred.kommand.IKommand
import foo.starred.kommand.scopes.KommandCommandScope
import foo.starred.kontext.abstraction.chat.message.MinecraftMessages
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.abstraction.client.extension.nextTick
import foo.starred.kontext.abstraction.gui.MinecraftGui
import foo.starred.kontext.api.storage.impl.generic.extension.mutableMap
import foo.starred.kontext.api.storage.impl.generic.extension.mutableSet
import foo.starred.kontext.api.storage.impl.generic.extension.string
import foo.starred.kontext.api.storage.impl.generic.impl.GenericJsonStore
import foo.starred.kontext.api.storage.impl.generic.property.base.AbstractStorageProperty
import foo.starred.kontext.api.text.parser.impl.parse
import foo.starred.updater.logic.source.impl.ModrinthUpdateSource
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.SharedConstants
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.jvm.optionals.getOrNull

object Detexturify : ClientModInitializer, IKommand<FabricClientCommandSource> {
    override val loader: KommandCommandScope<FabricClientCommandSource> = KommandCommandScope()

    const val modVersion: String = /*$ mod_version*/ "0.1.4"
    const val modId: String = /*$ mod_id*/ "detexturify"
    const val modName: String = /*$ mod_name*/ "Detexturify"
    const val discordUrl: String = "https://discord.gg/starred"

    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger(Detexturify::class.java)

    @JvmField
    val JSON: GenericJsonStore = GenericJsonStore(modName, "detexturify/whitelist")

    @JvmField
    val LAST_VERSION: AbstractStorageProperty<String> = JSON.string("last_minecraft_version")

    @JvmField
    val WHITELIST: AbstractStorageProperty<MutableSet<String>> = JSON.mutableSet("whitelist", Codec.STRING)

    @JvmField
    val BLACKLIST: AbstractStorageProperty<MutableSet<String>> = JSON.mutableSet("blacklist", Codec.STRING)

    @JvmField
    val REPLACEMENTS: AbstractStorageProperty<MutableMap<String, String>> = JSON.mutableMap("replacements", Codec.STRING, Codec.STRING)

    @JvmField
    var MAP: Map<String, SkyBlockItem> = mapOf()

    @JvmField
    var texture: Boolean = false

    override fun onInitializeClient() {
        val current = SharedConstants.getCurrentVersion().name()
        if (current != LAST_VERSION.value) {
            if (LAST_VERSION.value.isNotEmpty()) LOGGER.info("Minecraft version was changed, this should not happen! Redownloading the texture pack...")

            LAST_VERSION.value = current
            HypixelPackCache.remote()
        }

        ModrinthUpdateSource("detexturify").init(modVersion)

        DetexturifyConfig.toString()
        HypixelPackCache.toString()

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            loader.register(dispatcher)
        }

        kommand()

        "https://data.starred.foo/items.json".request {
            success<JsonObject> { json ->
                MAP = json.entrySet().associate { (k, v) ->
                    val a = v.asJsonObject
                    k to SkyBlockItem(a.get("texture")?.asString, a.get("model").asString)
                }
            }
        }
    }

    private fun kommand() {
        command(modId) {
            executes {
                MinecraftMessages.display("<#FAB387>[Detexturify]<r> Available commands:".parse(true))

                MinecraftMessages.display(" <dark_gray>- <green>/detexturify config <dark_gray>- <green>Opens config".parse())
                MinecraftMessages.display(" <dark_gray>- <green>/detexturify whitelist <dark_gray>- <green>Whitelist items".parse())
                MinecraftMessages.display(" <dark_gray>- <green>/detexturify blacklist <dark_gray>- <green>Blacklist items".parse())
                MinecraftMessages.display(" <dark_gray>- <green>/detexturify replace <dark_gray>- <green>Replace item textures".parse())
            }

            "help" {
                MinecraftMessages.display("<#FAB387>[Detexturify]<r> Available commands:".parse(true))

                MinecraftMessages.display(" <dark_gray>- <green>/detexturify config <dark_gray>- <green>Opens config".parse())
                MinecraftMessages.display(" <dark_gray>- <green>/detexturify whitelist <dark_gray>- <green>Whitelist items".parse())
                MinecraftMessages.display(" <dark_gray>- <green>/detexturify blacklist <dark_gray>- <green>Blacklist items".parse())
                MinecraftMessages.display(" <dark_gray>- <green>/detexturify replace <dark_gray>- <green>Replace item textures".parse())
            }

            "config" {
                nextTick {
                    MinecraftGui.screen(ResourcefulConfigScreen.getFactory(modId).apply(null))
                }
            }

            "whitelist".then {
                "executes" {
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Whitelist commands:".parse(true))

                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify whitelist list".parse())
                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify whitelist add".parse())
                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify whitelist remove".parse())
                }

                "add" {
                    val held = client.player?.mainHandItem?.takeIf { !it.isEmpty } ?: return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Not holding anything!".parse(true))
                    val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull() ?: return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true))

                    if (WHITELIST.value.contains(id)) {
                        return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Item already exists in whitelist!".parse(true))
                    }

                    WHITELIST.update { add(id) }
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Successfully added item to whitelist! You may need to change servers to view changes.".parse(true))
                }

                "remove" {
                    val held = client.player?.mainHandItem?.takeIf { !it.isEmpty } ?: return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Not holding anything!".parse(true))
                    val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull() ?: return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true))

                    if (!WHITELIST.value.contains(id)) {
                        return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Item does not exist in whitelist!".parse(true))
                    }

                    WHITELIST.update { remove(id) }
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Successfully removed item from whitelist! You may need to change servers to view changes.".parse(true))
                }

                "list" {
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Whitelisted items:".parse(true))
                    for (v in WHITELIST.value) MinecraftMessages.display(" <dark_gray>- <green>$v".parse())
                }
            }

            "blacklist".then {
                "executes" {
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Blacklist commands:".parse(true))

                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify blacklist list".parse())
                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify blacklist add".parse())
                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify blacklist remove".parse())
                }

                "add" {
                    val held = client.player?.mainHandItem?.takeIf { !it.isEmpty } ?: return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Not holding anything!".parse(true))
                    val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull() ?: return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true))

                    if (BLACKLIST.value.contains(id)) {
                        return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Item already exists in blacklist!".parse(true))
                    }

                    BLACKLIST.update { add(id) }
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Successfully added item to blacklist! You may need to change servers to view changes.".parse(true))
                }

                "remove" {
                    val held = client.player?.mainHandItem?.takeIf { !it.isEmpty } ?: return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Not holding anything!".parse(true))
                    val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull() ?: return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true))

                    if (!BLACKLIST.value.contains(id)) {
                        return@invoke MinecraftMessages.display("<#FAB387>[Detexturify]<r> Item does not exist in blacklist!".parse(true))
                    }

                    BLACKLIST.update { remove(id) }
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Successfully removed item from blacklist! You may need to change servers to view changes.".parse(true))
                }

                "list" {
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Blacklisted items:".parse(true))
                    for (v in BLACKLIST.value) MinecraftMessages.display(" <dark_gray>- <green>$v".parse())
                }
            }

            "replace".then {
                executes {
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Replacement commands:".parse(true))

                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify replace list".parse())
                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify replace <skyblock_id> <item>".parse())
                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify replace remove <skyblock_id>".parse())
                    MinecraftMessages.display(" <dark_gray>- <green>/detexturify replace clear".parse())
                }

                "list" {
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Custom replacements:".parse(true))
                    for ((k, v) in REPLACEMENTS.value) MinecraftMessages.display(" <dark_gray>- <green>$k <dark_gray>-> <yellow>$v".parse())
                }

                "clear" {
                    val count = REPLACEMENTS.value.size
                    REPLACEMENTS.update { clear() }
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Cleared <green>$count<r> replacements! You may need to change servers to view changes.".parse(true))
                }

                "remove" {
                    string("id") {
                        val id = string("id").replace(':', '-')
                        if (!REPLACEMENTS.value.containsKey(id)) return@string MinecraftMessages.display("<#FAB387>[Detexturify]<r> No replacement found for '<red>$id<r>'!".parse(true))

                        REPLACEMENTS.update { remove(id) }
                        MinecraftMessages.display("<#FAB387>[Detexturify]<r> Successfully removed replacement for '<green>$id<r>'! You may need to change servers to view changes.".parse(true))
                    }.suggests { REPLACEMENTS.value.keys }
                }

                "add" / "withId" / string("id").suggests { MAP.keys }.then {
                    string("model") {
                        val id = string("id").replace(':', '-')
                        val model = string("model")

                        REPLACEMENTS.update { put(id, model) }
                        MinecraftMessages.display("<#FAB387>[Detexturify]<r> Successfully replaced '<green>$id<r>' with '<yellow>$model<r>'! You may need to change servers to view changes.".parse(true))
                    }.suggests { BuiltInRegistries.ITEM.keySet().map { it.path } }
                }

                "add" / "held" / string("model") {
                    val held = client.player?.mainHandItem?.takeIf { !it.isEmpty } ?: return@string MinecraftMessages.display("<#FAB387>[Detexturify]<r> Not holding anything!".parse(true))
                    val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull()?.replace(':', '-') ?: return@string MinecraftMessages.display("<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true))
                    val model = string("model")

                    REPLACEMENTS.update { put(id, model) }
                    MinecraftMessages.display("<#FAB387>[Detexturify]<r> Successfully replaced '<green>$id<r>' with '<yellow>$model<r>'! You may need to change servers to view changes.".parse(true))
                }.suggests { BuiltInRegistries.ITEM.keySet().map { it.path } }
            }
        }
    }
}
