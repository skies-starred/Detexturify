@file:Suppress("FunctionName", "Unused")

package foo.starred.detexturify.ducks

import net.minecraft.resources.Identifier
import net.minecraft.world.item.component.ResolvableProfile

interface ItemStackDuck {
    fun `detexturify$id`(): Identifier?
    fun `detexturify$id`(identifier: Identifier?)

    fun `detexturify$profile`(): ResolvableProfile?
    fun `detexturify$profile`(profile: ResolvableProfile?)
}