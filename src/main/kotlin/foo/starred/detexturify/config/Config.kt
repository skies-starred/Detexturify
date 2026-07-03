package foo.starred.detexturify.config

import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import foo.starred.detexturify.Detexturify
import foo.starred.detexturify.config.categories.MainCategory
import xyz.aerii.library.utils.open

object Config : ConfigKt("detexturify/config") {
    override val name = Literal(Detexturify.modName)
    override val description = Literal("Removes the forced Hypixel SkyBlock texture pack.")

    init {
        separator {
            title = "Links"
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
            title = "Issues"
            description = "Opens the page to create bug reports"
            text = "Open page"

            onClick {
                "https://github.com/skies-starred/detexturify/issues".open()
            }
        }

        separator {
            title = "Other mods"
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

        category(MainCategory)
        register(Configurator(Detexturify.modId))
    }
}