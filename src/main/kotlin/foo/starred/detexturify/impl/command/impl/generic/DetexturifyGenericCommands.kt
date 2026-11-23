package foo.starred.detexturify.impl.command.impl.generic

import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import foo.starred.detexturify.Detexturify
import foo.starred.detexturify.annotation.impl.LoadableDetexturifyModule
import foo.starred.detexturify.impl.command.util.command
import foo.starred.kontext.abstraction.chat.message.MinecraftMessages
import foo.starred.kontext.abstraction.client.extension.nextTick
import foo.starred.kontext.abstraction.gui.MinecraftGui
import foo.starred.kontext.api.text.parser.impl.parse

@LoadableDetexturifyModule
object DetexturifyGenericCommands {
    init {
        command {
            executes {
                MinecraftMessages.display("<#FAB387>[Detexturify]<r> Available commands:".parse(true))

                MinecraftMessages.display(" <dark_gray>- <green>/detexturify config <dark_gray>- <green>Opens config")
                MinecraftMessages.display(" <dark_gray>- <green>/detexturify whitelist <dark_gray>- <green>Whitelist items")
                MinecraftMessages.display(" <dark_gray>- <green>/detexturify blacklist <dark_gray>- <green>Blacklist items")
                MinecraftMessages.display(" <dark_gray>- <green>/detexturify replace <dark_gray>- <green>Replace item textures")
            }

            "help" {
                MinecraftMessages.display("<#FAB387>[Detexturify]<r> Available commands:".parse(true))

                MinecraftMessages.display(" <dark_gray>- <green>/detexturify config <dark_gray>- <green>Opens config")
                MinecraftMessages.display(" <dark_gray>- <green>/detexturify whitelist <dark_gray>- <green>Whitelist items")
                MinecraftMessages.display(" <dark_gray>- <green>/detexturify blacklist <dark_gray>- <green>Blacklist items")
                MinecraftMessages.display(" <dark_gray>- <green>/detexturify replace <dark_gray>- <green>Replace item textures")
            }

            "config" {
                nextTick {
                    MinecraftGui.screen(ResourcefulConfigScreen.getFactory(Detexturify.Data.id).apply(null))
                }
            }
        }
    }
}
