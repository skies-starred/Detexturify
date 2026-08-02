package foo.starred.detexturify.config.categories

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import foo.starred.detexturify.config.observe

object MainCategory : CategoryKt("Main") {
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

    enum class RetextureType {
        VISUAL,
        COMPONENT;
    }

    enum class FilterType {
        WHITELIST,
        BLACKLIST;
    }
}