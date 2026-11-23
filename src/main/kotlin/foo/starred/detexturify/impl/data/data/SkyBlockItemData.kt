package foo.starred.detexturify.impl.data.data

import com.google.common.collect.ImmutableMultimap
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import foo.starred.detexturify.Detexturify
import net.minecraft.resources.Identifier
import net.minecraft.world.item.component.ResolvableProfile
import java.util.UUID

data class SkyBlockItemData(
    val texture: String?,
    val model: String
) {
    val id: Identifier = Identifier.parse(model)
    val profile: ResolvableProfile = tryCreate(texture)

    companion object {
        private fun tryCreate(texture: String?): ResolvableProfile {
            val uuid = UUID.nameUUIDFromBytes($$"$${Detexturify.Data.id}$custom$$${texture.orEmpty()}".toByteArray())
            return ResolvableProfile.createResolved(GameProfile(uuid, Detexturify.Data.name, PropertyMap(ImmutableMultimap.of("textures", Property("textures", texture)))))
        }
    }
}
