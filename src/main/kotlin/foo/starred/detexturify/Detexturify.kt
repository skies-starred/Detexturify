@file:Suppress("ConstPropertyName", "Unused")

package foo.starred.detexturify

import foo.starred.detexturify.annotation.loader.DetexturifyAnnotationLoader
import foo.starred.detexturify.impl.cache.DetexturifyPackCacheInstance
import foo.starred.kontext.api.storage.impl.generic.extension.string
import foo.starred.kontext.api.storage.impl.generic.impl.GenericJsonStore
import foo.starred.kontext.api.storage.impl.generic.property.base.AbstractStorageProperty
import foo.starred.updater.logic.source.impl.ModrinthUpdateSource
import net.fabricmc.api.ClientModInitializer
import net.minecraft.SharedConstants
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Detexturify : ClientModInitializer {
    private val JSON: GenericJsonStore = GenericJsonStore(Data.name, "detexturify/main")

    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger(Detexturify::class.java)

    @JvmField
    val LAST_VERSION: AbstractStorageProperty<String> = JSON.string("minecraft_version")

    override fun onInitializeClient() {
        ModrinthUpdateSource("detexturify").init(Data.version)
        DetexturifyAnnotationLoader.load()

        val current = SharedConstants.getCurrentVersion().name()
        if (current == LAST_VERSION.value) return
        if (LAST_VERSION.value.isNotEmpty()) LOGGER.info("Minecraft version was changed, this should not happen! Redownloading the texture pack...")

        LAST_VERSION.value = current
        DetexturifyPackCacheInstance.remote()
    }

    object Data {
        const val version: String = /*$ mod_version*/ "0.1.4"
        const val name: String = /*$ mod_name*/ "Detexturify"
        const val id: String = /*$ mod_id*/ "detexturify"

        const val discord: String = "https://discord.gg/starred"
    }
}
