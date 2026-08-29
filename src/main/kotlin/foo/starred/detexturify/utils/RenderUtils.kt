package foo.starred.detexturify.utils

import foo.starred.snowbird.api.client
import net.minecraft.client.gui.GuiGraphicsExtractor

object RenderUtils {
    @JvmStatic
    @JvmOverloads
    @JvmName("text_string")
    fun GuiGraphicsExtractor.text(text: String, x: Int, y: Int, shadow: Boolean = true, color: Int = -1, center: Boolean = false) {
        val xx = if (center) x - client.font.width(text) / 2 else x
        text(client.font, text, xx, y, color, shadow)
    }

    @JvmStatic
    @JvmOverloads
    @JvmName("drawRectangle_int")
    fun GuiGraphicsExtractor.drawRectangle(x: Int, y: Int, width: Int, height: Int, color: Int = -1) {
        fill(x, y, x + width, y + height, color)
    }

    @JvmStatic
    @JvmOverloads
    fun GuiGraphicsExtractor.drawOutline(x: Int, y: Int, width: Int, height: Int, border: Int, color: Int = -1, inset: Boolean = false) {
        val border = if (inset) -border else border
        fill(x - border, y - border, x + width + border, y, color)
        fill(x - border, y + height, x + width + border, y + height + border, color)
        fill(x - border, y, x, y + height, color)
        fill(x + width, y, x + width + border, y + height, color)
    }
}
