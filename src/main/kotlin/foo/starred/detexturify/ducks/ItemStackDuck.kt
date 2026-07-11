@file:Suppress("FunctionName", "Unused")

package foo.starred.detexturify.ducks

import net.minecraft.resources.Identifier
import net.minecraft.world.item.component.ResolvableProfile

interface ItemStackDuck {
    fun `detexturify$id`(): Identifier?
    fun `detexturify$id`(identifier: Identifier?)

    fun `detexturify$special`(): Int
    fun `detexturify$special`(int: Int)

    fun `detexturify$filtered`(): Boolean?
    fun `detexturify$filtered`(int: Boolean?)

    fun `detexturify$profile`(): ResolvableProfile?
    fun `detexturify$profile`(profile: ResolvableProfile?)
}