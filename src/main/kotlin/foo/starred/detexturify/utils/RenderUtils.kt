package foo.starred.detexturify.utils

//~ if >= 26.1 'GuiGraphics' -> 'GuiGraphicsExtractor' {
import net.minecraft.client.gui.GuiGraphics
import xyz.aerii.library.api.client

object RenderUtils {
    @JvmStatic
    @JvmOverloads
    @JvmName("text_string")
    fun GuiGraphics.text(text: String, x: Int, y: Int, shadow: Boolean = true, color: Int = -1, center: Boolean = false) {
        val xx = if (center) x - client.font.width(text) / 2 else x
        //~ if >= 26.1 'drawString(' -> 'text('
        drawString(client.font, text, xx, y, color, shadow)
    }

    @JvmStatic
    @JvmOverloads
    @JvmName("drawRectangle_int")
    fun GuiGraphics.drawRectangle(x: Int, y: Int, width: Int, height: Int, color: Int = -1) {
        fill(x, y, x + width, y + height, color)
    }

    @JvmStatic
    @JvmOverloads
    fun GuiGraphics.drawOutline(x: Int, y: Int, width: Int, height: Int, border: Int, color: Int = -1, inset: Boolean = false) {
        val border = if (inset) -border else border
        fill(x - border, y - border, x + width + border, y, color)
        fill(x - border, y + height, x + width + border, y + height + border, color)
        fill(x - border, y, x, y + height, color)
        fill(x + width, y, x + width + border, y + height, color)
    }
}
//~ }