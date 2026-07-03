package foo.starred.detexturify.data

import com.google.common.collect.ImmutableMultimap
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import foo.starred.detexturify.Detexturify
import net.minecraft.resources.Identifier
import net.minecraft.world.item.component.ResolvableProfile
import java.util.UUID

data class SkyBlockItem(
    val texture: String?,
    val model: String
) {
    val id: Identifier = Identifier.parse(model)
    val profile: ResolvableProfile = fn(texture)

    companion object {
        private fun fn(texture: String?): ResolvableProfile {
            val uuid = UUID.nameUUIDFromBytes($$"$${Detexturify.modId}$custom$$${texture.orEmpty()}".toByteArray())
            return ResolvableProfile.createResolved(GameProfile(uuid, Detexturify.modName, PropertyMap(ImmutableMultimap.of("textures", Property("textures", texture)))))
        }
    }
}