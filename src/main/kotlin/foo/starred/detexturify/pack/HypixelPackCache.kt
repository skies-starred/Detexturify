package foo.starred.detexturify.pack

import com.google.gson.JsonParser
import foo.starred.detexturify.Detexturify
import foo.starred.detexturify.config.categories.MainCategory
import foo.starred.detexturify.utils.NetworkUtils.download
import foo.starred.detexturify.utils.NetworkUtils.request
import foo.starred.snowbird.api.client
import foo.starred.snowbird.api.mainThread
import foo.starred.snowbird.api.storage.AbstractJsonStore
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.SharedConstants
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.FilePackResources
import net.minecraft.server.packs.PackLocationInfo
import net.minecraft.server.packs.PackSelectionConfig
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.PackSource
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.Optional

object HypixelPackCache {
    private val SCRIBBLE: AbstractJsonStore = AbstractJsonStore(Detexturify.modName, "detexturify/hypixel-pack")
    private val URL: AbstractJsonStore.Value<String> = SCRIBBLE.string("url")

    private val dir: File = FabricLoader.getInstance().configDir.resolve(Detexturify.modId).resolve("pack-cache").toFile()

    @Volatile
    private var pack: File? = URL.value.takeIf { it.isNotEmpty() }?.cached()?.takeIf { it.exists() }

    @Volatile
    @JvmField
    var active: Pack? = pack?.pack()

    @Volatile
    @JvmField
    var last: String? = if (active != null) URL.value else null

    init {
        MainCategory.hypixelCache.onChange {
            if (it) enable() else disable()
        }

        if (MainCategory.hypixelCache.value && pack == null) {
            "https://data.starred.foo/hypixel/pack.json".request {
                success<String> { string ->
                    val version = SharedConstants.getCurrentVersion().name()
                    val key = if (version.startsWith("26.") && version.count { it == '.' } == 2) version.substringBeforeLast('.') else version
                    val url = runCatching { JsonParser.parseString(string).asJsonObject[key]?.asString }.getOrNull()

                    if (url.isNullOrBlank()) {
                        Detexturify.LOGGER.error("No Hypixel pack URL found for key '$key'")
                        return@success
                    }

                    update(url)
                }

                error {
                    Detexturify.LOGGER.error("Failed to fetch the Hypixel pack metadata!", it)
                }
            }
        }
    }

    @JvmStatic
    fun update(url: String) {
        if (url == last && active != null) return

        dir.mkdirs()
        mainThread {
            //~ if >= 26.2 'client.toastManager' -> 'client.gui.toastManager()'
            SystemToast.add(client.toastManager, SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.literal("Caching Hypixel pack"), Component.literal("This is a one-time download. Please wait a moment."))
        }

        val temp = File(dir, "pack.tmp")
        url.download(temp) {
            success {
                val target = url.cached()
                val previous = pack

                runCatching {
                    Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    target.pack()
                }.onSuccess { pack0 ->
                    if (pack0 == null) return@onSuccess

                    active = pack0
                    pack = target
                    last = url
                    URL.value = url

                    if (previous != null && previous != target) previous.delete()

                    Detexturify.LOGGER.info("Cached and loaded the Hypixel SkyBlock pack ($url)")
                    mainThread { client.reloadResourcePacks() }
                }.onFailure {
                    Detexturify.LOGGER.error("Failed to finalise the downloaded Hypixel SkyBlock pack", it)
                }
            }
        }
    }

    private fun enable() {
        if (active != null) return
        val file = pack ?: URL.value.ifEmpty { null }?.cached()?.takeIf { it.exists() } ?: return

        pack = file
        active = file.pack()

        if (active == null) return
        mainThread { client.reloadResourcePacks() }
    }

    private fun disable() {
        if (active == null) return
        active = null
        mainThread { client.reloadResourcePacks() }
    }

    private fun File.pack(): Pack? {
        if (!exists()) return null

        return try {
            Pack.readMetaAndCreate(PackLocationInfo("detexturify/fallback/hypixel", Component.literal("Detexturify: Hypixel SkyBlock (cached)"), PackSource.BUILT_IN, Optional.empty()), FilePackResources.FileResourcesSupplier(this), PackType.CLIENT_RESOURCES, PackSelectionConfig(true, Pack.Position.TOP, true))
        } catch (t: Throwable) {
            Detexturify.LOGGER.error("Failed to read Hypixel SkyBlock pack metadata at $this", t)
            null
        }
    }

    private fun String.cached(): File {
        val hash = MessageDigest.getInstance("SHA-256").digest(toByteArray()).joinToString("") { "%02x".format(it) }.take(16)
        return File(dir, "pack-$hash.zip")
    }
}
