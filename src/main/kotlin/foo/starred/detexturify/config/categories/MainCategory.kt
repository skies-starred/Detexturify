package foo.starred.detexturify.config.categories

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import foo.starred.detexturify.config.observe

object MainCategory : CategoryKt("Main") {
    var enabled by boolean(true) {
        name = Literal("Enabled")
        description = Literal("Whether the mod is enabled.")
    }.observe()

    var retexture by boolean(true) {
        name = Literal("Retexture items")
        description = Literal("Retextures items to be how they were like before the update.")
    }.observe()

    var retextureType by enum(RetextureType.VISUAL) {
        name = Literal("Retexture type")
        description = Literal("The method to use to re-texture items.")
    }.observe()

    enum class RetextureType {
        VISUAL,
        COMPONENT;
    }
}