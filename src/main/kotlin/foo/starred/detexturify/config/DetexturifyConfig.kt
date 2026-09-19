package foo.starred.detexturify.config

import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import foo.starred.detexturify.Detexturify
import foo.starred.snowbird.utils.open

object DetexturifyConfig : ConfigKt("detexturify/config") {
    override val name = Literal(Detexturify.modName)
    override val description = Literal("Removes the forced Hypixel SkyBlock texture pack.")

    init {
        separator {
            title = "§cConfig"
            description = "Customise Detexturify"
        }
    }

    var enabled by boolean(true) {
        name = Literal("Enabled")
        description = Literal("Whether the mod is enabled.")
    }.observe()

    var hypixelCache by boolean(true) {
        name = Literal("Cache hypixel pack")
        description = Literal("Prevents Hypixel's texture pack from downloading and shows a cached copy instead, so some items don't render with a missing texture. The cache is saved across launches, so it's usually only downloaded once. §4Not recommended to turn off§r.")
    }.observe()

    var vanillaColor by boolean(true) {
        name = Literal("Vanilla colors")
        description = Literal("Prevents Hypixel from loading custom color shaders.")
    }

    var vanillaTooltip by boolean(true) {
        name = Literal("Minecraft tooltip")
        description = Literal("Restores Minecraft's vanilla tooltip.")
    }.observe()

    var retexture by boolean(true) {
        name = Literal("Retexture items")
        description = Literal("Retextures items to be how they were like before the update.")
    }.observe()

    var retextureType by enum(RetextureType.VISUAL) {
        name = Literal("Retexture type")
        description = Literal("The method to use to re-texture items.")
    }.observe()

    var filterType by enum(FilterType.WHITELIST) {
        name = Literal("Filter type")
        description = Literal("Determines which items use custom textures. Use Blacklist to apply custom textures to all items except those listed, or Whitelist to apply custom textures only to the listed items.\nTry running \"/detexturify whitelist\" and \"/detexturify blacklist\"!")
    }

    init {
        separator {
            title = "§cLinks"
            description = "Links to stuff"
        }

        button {
            title = "Discord"
            description = "Join if you need help, or want to check out the other mods made by Starred!"
            text = "Join"

            onClick {
                Detexturify.discordUrl.open()
            }
        }

        button {
            title = "GitHub"
            description = "The source code for the mod! Star the repo?"
            text = "Open page"

            onClick {
                "https://github.com/skies-starred/detexturify".open()
            }
        }

        button {
            title = "Patreon"
            description = "Want to support development? You can support through Patreon!"
            text = "Open page"

            onClick {
                "https://patreon.com/starredskies".open()
            }
        }

        separator {
            title = "§cOther mods"
        }

        button {
            title = "Athen"
            description = "A very cool Quality-of-Life mod for Hypixel Skyblock."
            text = "Open page"

            onClick {
                "https://modrinth.com/mod/athen".open()
            }
        }

        button {
            title = "JEC"
            description = "A small mod that adds cat-istic features!"
            text = "Open page"

            onClick {
                "https://modrinth.com/mod/jec".open()
            }
        }

        register(Configurator(Detexturify.modId))
    }

    enum class RetextureType {
        VISUAL,
        COMPONENT;
    }

    enum class FilterType {
        WHITELIST,
        BLACKLIST;
    }
}
