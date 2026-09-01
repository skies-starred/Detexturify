@file:Suppress("ConstPropertyName", "Unused")

package foo.starred.detexturify

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import foo.starred.detexturify.config.Config
import foo.starred.detexturify.data.SkyBlockItem
import foo.starred.detexturify.pack.HypixelPackCache
import foo.starred.detexturify.updater.ModUpdater
import foo.starred.detexturify.utils.NetworkUtils.request
import foo.starred.kommand.IKommand
import foo.starred.kommand.scopes.KommandCommandScope
import foo.starred.snowbird.api.client
import foo.starred.snowbird.api.held
import foo.starred.snowbird.api.lie
import foo.starred.snowbird.api.nextTick
import foo.starred.snowbird.api.storage.AbstractJsonStore
import foo.starred.snowbird.api.text.parser.impl.parse
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.jvm.optionals.getOrNull

object Detexturify : ClientModInitializer, IKommand<FabricClientCommandSource> {
    override val loader: KommandCommandScope<FabricClientCommandSource> = KommandCommandScope()

    const val modVersion: String = /*$ mod_version*/ "0.0.9"
    const val modId: String = /*$ mod_id*/ "detexturify"
    const val modName: String = /*$ mod_name*/ "Detexturify"
    const val discordUrl: String = "https://discord.gg/starred"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(Detexturify::class.java)

    @JvmField
    val SCRIBBLE: AbstractJsonStore = AbstractJsonStore(modName, "detexturify/whitelist")

    @JvmField
    val WHITELIST: AbstractJsonStore.Value<MutableSet<String>> = SCRIBBLE.mutableSet("whitelist", Codec.STRING)

    @JvmField
    val BLACKLIST: AbstractJsonStore.Value<MutableSet<String>> = SCRIBBLE.mutableSet("blacklist", Codec.STRING)

    @JvmField
    val REPLACEMENTS: AbstractJsonStore.Value<MutableMap<String, String>> = SCRIBBLE.mutableMap("replacements", Codec.STRING, Codec.STRING)

    @JvmField
    var MAP: Map<String, SkyBlockItem> = mapOf()

    @JvmField
    var texture: Boolean = false

    override fun onInitializeClient() {
        Config.toString()
        ModUpdater.toString()
        HypixelPackCache.toString()

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            loader.register(dispatcher)
        }

        command(modId) {
            executes {
                "<#FAB387>[Detexturify]<r> Available commands:".parse(true).lie()

                " <dark_gray>- <green>/detexturify config <dark_gray>- <green>Opens config".parse().lie()
                " <dark_gray>- <green>/detexturify whitelist <dark_gray>- <green>Whitelist items".parse().lie()
                " <dark_gray>- <green>/detexturify blacklist <dark_gray>- <green>Blacklist items".parse().lie()
                " <dark_gray>- <green>/detexturify replace <dark_gray>- <green>Replace item textures".parse().lie()
            }

            "help" {
                "<#FAB387>[Detexturify]<r> Available commands:".parse(true).lie()

                " <dark_gray>- <green>/detexturify config <dark_gray>- <green>Opens config".parse().lie()
                " <dark_gray>- <green>/detexturify whitelist <dark_gray>- <green>Whitelist items".parse().lie()
                " <dark_gray>- <green>/detexturify blacklist <dark_gray>- <green>Blacklist items".parse().lie()
                " <dark_gray>- <green>/detexturify replace <dark_gray>- <green>Replace item textures".parse().lie()
            }

            "config" {
                nextTick {
                    //~ if >= 26.2 'setScreen(' -> 'gui.setScreen('
                    client.setScreen(ResourcefulConfigScreen.getFactory(modId).apply(null))
                }
            }

            "whitelist".then {
                "executes" {
                    "<#FAB387>[Detexturify]<r> Whitelist commands:".parse(true).lie()

                    " <dark_gray>- <green>/detexturify whitelist list".parse().lie()
                    " <dark_gray>- <green>/detexturify whitelist add".parse().lie()
                    " <dark_gray>- <green>/detexturify whitelist remove".parse().lie()
                }

                "add" {
                    val held = held?.takeIf { !it.isEmpty } ?: return@invoke "<#FAB387>[Detexturify]<r> Not holding anything!".parse(true).lie()
                    val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull() ?: return@invoke "<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true).lie()

                    if (WHITELIST.value.contains(id)) {
                        return@invoke "<#FAB387>[Detexturify]<r> Item already exists in whitelist!".parse(true).lie()
                    }

                    WHITELIST.update { add(id) }
                    "<#FAB387>[Detexturify]<r> Successfully added item to whitelist! You may need to change servers to view changes.".parse(true).lie()
                }

                "remove" {
                    val held = held?.takeIf { !it.isEmpty } ?: return@invoke "<#FAB387>[Detexturify]<r> Not holding anything!".parse(true).lie()
                    val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull() ?: return@invoke "<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true).lie()

                    if (!WHITELIST.value.contains(id)) {
                        return@invoke "<#FAB387>[Detexturify]<r> Item does not exist in whitelist!".parse(true).lie()
                    }

                    WHITELIST.update { remove(id) }
                    "<#FAB387>[Detexturify]<r> Successfully removed item from whitelist! You may need to change servers to view changes.".parse(true).lie()
                }

                "list" {
                    "<#FAB387>[Detexturify]<r> Whitelisted items:".parse(true).lie()
                    for (v in WHITELIST.value) " <dark_gray>- <green>$v".parse().lie()
                }
            }

            "blacklist".then {
                "executes" {
                    "<#FAB387>[Detexturify]<r> Blacklist commands:".parse(true).lie()

                    " <dark_gray>- <green>/detexturify blacklist list".parse().lie()
                    " <dark_gray>- <green>/detexturify blacklist add".parse().lie()
                    " <dark_gray>- <green>/detexturify blacklist remove".parse().lie()
                }

                "add" {
                    val held = held?.takeIf { !it.isEmpty } ?: return@invoke "<#FAB387>[Detexturify]<r> Not holding anything!".parse(true).lie()
                    val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull() ?: return@invoke "<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true).lie()

                    if (BLACKLIST.value.contains(id)) {
                        return@invoke "<#FAB387>[Detexturify]<r> Item already exists in blacklist!".parse(true).lie()
                    }

                    BLACKLIST.update { add(id) }
                    "<#FAB387>[Detexturify]<r> Successfully added item to blacklist! You may need to change servers to view changes.".parse(true).lie()
                }

                "remove" {
                    val held = held?.takeIf { !it.isEmpty } ?: return@invoke "<#FAB387>[Detexturify]<r> Not holding anything!".parse(true).lie()
                    val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull() ?: return@invoke "<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true).lie()

                    if (!BLACKLIST.value.contains(id)) {
                        return@invoke "<#FAB387>[Detexturify]<r> Item does not exist in blacklist!".parse(true).lie()
                    }

                    BLACKLIST.update { remove(id) }
                    "<#FAB387>[Detexturify]<r> Successfully removed item from blacklist! You may need to change servers to view changes.".parse(true).lie()
                }

                "list" {
                    "<#FAB387>[Detexturify]<r> Blacklisted items:".parse(true).lie()
                    for (v in BLACKLIST.value) " <dark_gray>- <green>$v".parse().lie()
                }
            }

            "replace".then {
                executes {
                    "<#FAB387>[Detexturify]<r> Replacement commands:".parse(true).lie()

                    " <dark_gray>- <green>/detexturify replace list".parse().lie()
                    " <dark_gray>- <green>/detexturify replace <skyblock_id> <item>".parse().lie()
                    " <dark_gray>- <green>/detexturify replace remove <skyblock_id>".parse().lie()
                    " <dark_gray>- <green>/detexturify replace clear".parse().lie()
                }

                "list" {
                    "<#FAB387>[Detexturify]<r> Custom replacements:".parse(true).lie()
                    for ((k, v) in REPLACEMENTS.value) " <dark_gray>- <green>$k <dark_gray>-> <yellow>$v".parse().lie()
                }

                "clear" {
                    val count = REPLACEMENTS.value.size
                    REPLACEMENTS.update { clear() }
                    "<#FAB387>[Detexturify]<r> Cleared <green>$count<r> replacements! You may need to change servers to view changes.".parse(true).lie()
                }

                "remove" {
                    string("id") {
                        val id = string("id").replace(':', '-')
                        if (!REPLACEMENTS.value.containsKey(id)) return@string "<#FAB387>[Detexturify]<r> No replacement found for '<red>$id<r>'!".parse(true).lie()

                        REPLACEMENTS.update { remove(id) }
                        "<#FAB387>[Detexturify]<r> Successfully removed replacement for '<green>$id<r>'! You may need to change servers to view changes.".parse(true).lie()
                    }.suggests { REPLACEMENTS.value.keys }
                }

                "add" / "withId" / string("id").suggests { MAP.keys }.then {
                    string("model") {
                        val id = string("id").replace(':', '-')
                        val model = string("model")

                        REPLACEMENTS.update { put(id, model) }
                        "<#FAB387>[Detexturify]<r> Successfully replaced '<green>$id<r>' with '<yellow>$model<r>'! You may need to change servers to view changes.".parse(true).lie()
                    }.suggests { BuiltInRegistries.ITEM.keySet().map { it.path } }
                }

                "add" / "held" / string("model") {
                    val held = held?.takeIf { !it.isEmpty } ?: return@string "<#FAB387>[Detexturify]<r> Not holding anything!".parse(true).lie()
                    val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull()?.replace(':', '-') ?: return@string "<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true).lie()
                    val model = string("model")

                    REPLACEMENTS.update { put(id, model) }
                    "<#FAB387>[Detexturify]<r> Successfully replaced '<green>$id<r>' with '<yellow>$model<r>'! You may need to change servers to view changes.".parse(true).lie()
                }.suggests { BuiltInRegistries.ITEM.keySet().map { it.path } }
            }
        }

        "https://data.starred.foo/items.json".request {
            success<JsonObject> { json ->
                MAP = json.entrySet().associate { (k, v) ->
                    val a = v.asJsonObject
                    k to SkyBlockItem(a.get("texture")?.asString, a.get("model").asString)
                }
            }
        }
    }
}
