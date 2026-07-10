@file:Suppress("ConstPropertyName", "Unused")

package foo.starred.detexturify

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import foo.starred.detexturify.config.Config
import foo.starred.detexturify.data.SkyBlockItem
import foo.starred.detexturify.updater.ModUpdater
import foo.starred.detexturify.utils.NetworkUtils.request
import net.fabricmc.api.ClientModInitializer
import net.minecraft.core.component.DataComponents
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import xyz.aerii.library.api.client
import xyz.aerii.library.api.held
import xyz.aerii.library.api.lie
import xyz.aerii.library.api.nextTick
import xyz.aerii.library.handlers.data.AbstractScribble
import xyz.aerii.library.handlers.parser.parse
import xyz.aerii.library.kommand.ICommand
import kotlin.jvm.optionals.getOrNull

object Detexturify : ClientModInitializer, ICommand {
    const val modVersion: String = /*$ mod_version*/ "0.0.4"
    const val modId: String = /*$ mod_id*/ "detexturify"
    const val modName: String = /*$ mod_name*/ "Detexturify"
    const val discordUrl: String = "https://discord.gg/starred"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(Detexturify::class.java)

    @JvmField
    val SCRIBBLE: AbstractScribble = AbstractScribble(modName, "detexturify/whitelist")

    @JvmField
    val WHITELIST: AbstractScribble.Value<MutableSet<String>> = SCRIBBLE.mutableSet("whitelist", Codec.STRING)

    @JvmField
    val BLACKLIST: AbstractScribble.Value<MutableSet<String>> = SCRIBBLE.mutableSet("blacklist", Codec.STRING)

    @JvmField
    var MAP: Map<String, SkyBlockItem> = mapOf()

    @JvmField
    var texture: Boolean = false

    override fun onInitializeClient() {
        Config.toString()
        ModUpdater.toString()

        command(modId) {
            executes {
                "<#FAB387>[Detexturify]<r> Available commands:".parse(true).lie()

                " <dark_gray>- <green>/detexturify config <dark_gray>- <green>Opens config".parse().lie()
                " <dark_gray>- <green>/detexturify whitelist <dark_gray>- <green>Whitelist items".parse().lie()
                " <dark_gray>- <green>/detexturify blacklist <dark_gray>- <green>Blacklist items".parse().lie()
            }

            "help" {
                "<#FAB387>[Detexturify]<r> Available commands:".parse(true).lie()

                " <dark_gray>- <green>/detexturify config <dark_gray>- <green>Opens config".parse().lie()
                " <dark_gray>- <green>/detexturify whitelist <dark_gray>- <green>Whitelist items".parse().lie()
                " <dark_gray>- <green>/detexturify blacklist <dark_gray>- <green>Blacklist items".parse().lie()
            }

            "config" {
                nextTick {
                    //~ if >= 26.2 'setScreen(' -> 'gui.setScreen('
                    client.setScreen(ResourcefulConfigScreen.getFactory(modId).apply(null))
                }
            }

            "whitelist" {
                "<#FAB387>[Detexturify]<r> Whitelist commands:".parse(true).lie()

                " <dark_gray>- <green>/detexturify whitelist list".parse().lie()
                " <dark_gray>- <green>/detexturify whitelist add".parse().lie()
                " <dark_gray>- <green>/detexturify whitelist remove".parse().lie()
            }

            "whitelist" / "add" {
                val held = held?.takeIf { !it.isEmpty } ?: return@invoke "<#FAB387>[Detexturify]<r> Not holding anything!".parse(true).lie()
                val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull() ?: return@invoke "<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true).lie()

                if (WHITELIST.value.contains(id)) {
                    return@invoke "<#FAB387>[Detexturify]<r> Item already exists in whitelist!".parse(true).lie()
                }

                WHITELIST.update { add(id) }
                "<#FAB387>[Detexturify]<r> Successfully added item to whitelist!".parse(true).lie()
            }

            "whitelist" / "remove" {
                val held = held?.takeIf { !it.isEmpty } ?: return@invoke "<#FAB387>[Detexturify]<r> Not holding anything!".parse(true).lie()
                val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull() ?: return@invoke "<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true).lie()

                if (!WHITELIST.value.contains(id)) {
                    return@invoke "<#FAB387>[Detexturify]<r> Item does not exist in whitelist!".parse(true).lie()
                }

                WHITELIST.update { remove(id) }
                "<#FAB387>[Detexturify]<r> Successfully removed item from whitelist!".parse(true).lie()
            }

            "whitelist" / "list" {
                "<#FAB387>[Detexturify]<r> Whitelisted items:".parse(true).lie()
                for (v in WHITELIST.value) " <dark_gray>- <green>$v".parse().lie()
            }

            "blacklist" {
                "<#FAB387>[Detexturify]<r> Blacklist commands:".parse(true).lie()

                " <dark_gray>- <green>/detexturify blacklist list".parse().lie()
                " <dark_gray>- <green>/detexturify blacklist add".parse().lie()
                " <dark_gray>- <green>/detexturify blacklist remove".parse().lie()
            }

            "blacklist" / "add" {
                val held = held?.takeIf { !it.isEmpty } ?: return@invoke "<#FAB387>[Detexturify]<r> Not holding anything!".parse(true).lie()
                val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull() ?: return@invoke "<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true).lie()

                if (BLACKLIST.value.contains(id)) {
                    return@invoke "<#FAB387>[Detexturify]<r> Item already exists in blacklist!".parse(true).lie()
                }

                BLACKLIST.update { add(id) }
                "<#FAB387>[Detexturify]<r> Successfully added item to blacklist!".parse(true).lie()
            }

            "blacklist" / "remove" {
                val held = held?.takeIf { !it.isEmpty } ?: return@invoke "<#FAB387>[Detexturify]<r> Not holding anything!".parse(true).lie()
                val id = held.get(DataComponents.CUSTOM_DATA)?.copyTag()?.getString("id")?.getOrNull() ?: return@invoke "<#FAB387>[Detexturify]<r> Could not resolve SkyBlock ID of item!".parse(true).lie()

                if (!BLACKLIST.value.contains(id)) {
                    return@invoke "<#FAB387>[Detexturify]<r> Item does not exist in blacklist!".parse(true).lie()
                }

                BLACKLIST.update { remove(id) }
                "<#FAB387>[Detexturify]<r> Successfully removed item from blacklist!".parse(true).lie()
            }

            "blacklist" / "list" {
                "<#FAB387>[Detexturify]<r> Blacklisted items:".parse(true).lie()
                for (v in BLACKLIST.value) " <dark_gray>- <green>$v".parse().lie()
            }
        }

        "https://athen.aerii.xyz/items".request {
            onSuccess<JsonObject> { json ->
                MAP = json.entrySet().associate { (k, v) ->
                    val a = v.asJsonObject
                    k to SkyBlockItem(a.get("texture")?.asString, a.get("model").asString)
                }
            }
        }
    }
}