package foo.starred.detexturify.impl.cache

import com.google.gson.JsonParser
import foo.starred.detexturify.Detexturify
import foo.starred.detexturify.annotation.impl.LoadableDetexturifyModule
import foo.starred.detexturify.config.impl.DetexturifyConfig
import foo.starred.detexturify.impl.network.NetworkAPI.download
import foo.starred.detexturify.impl.network.NetworkAPI.request
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.abstraction.client.extension.mainThread
import foo.starred.kontext.api.format.extension.component.literal
import foo.starred.kontext.api.storage.impl.generic.extension.string
import foo.starred.kontext.api.storage.impl.generic.impl.GenericJsonStore
import foo.starred.kontext.api.storage.impl.generic.property.base.AbstractStorageProperty
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.SharedConstants
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.FilePackResources
import net.minecraft.server.packs.PackLocationInfo
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackSelectionConfig
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.repository.KnownPack
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.resources.IoSupplier
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.Optional

@LoadableDetexturifyModule
object DetexturifyPackCacheInstance {
    private val CACHE: File = FabricLoader.getInstance().configDir.resolve(Detexturify.Data.id).resolve("pack-cache").toFile()

    private val JSON: GenericJsonStore = GenericJsonStore(Detexturify.Data.name, "detexturify/hypixel-pack")
    private val URL: AbstractStorageProperty<String> = JSON.string("url")

    @Volatile
    private var PACK: File? = URL.value.takeIf { it.isNotEmpty() }?.cached()?.takeIf { it.exists() }

    @Volatile
    var active: Pack? = PACK?.pack()

    @Volatile
    var last: String? = if (active != null) URL.value else null

    init {
        DetexturifyConfig.hypixelCache.observe {
            if (it) enable() else disable()
        }

        if (PACK == null) {
            remote()
        }
    }

    fun update(url: String) {
        if (url == last && active != null) return

        CACHE.mkdirs()
        mainThread {
            //~ if >= 26.2 'client.toastManager' -> 'client.gui.toastManager()'
            SystemToast.add(client.toastManager, PERIODIC_NOTIFICATION, "Caching Hypixel pack".literal(), "This is a one-time download. Please wait a moment.".literal())
        }

        val temp = File(CACHE, "pack.tmp")
        url.download(temp) {
            success {
                val target = url.cached()
                val previous = PACK

                runCatching {
                    Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    target.pack()
                }.onSuccess { pack0 ->
                    if (pack0 == null) return@onSuccess

                    active = pack0
                    PACK = target
                    last = url
                    URL.value = url

                    if (previous != null && previous != target) {
                        previous.delete()
                    }

                    Detexturify.LOGGER.info("Cached and loaded the Hypixel SkyBlock pack ($url)")
                    mainThread { client.reloadResourcePacks() }
                }.onFailure {
                    Detexturify.LOGGER.error("Failed to finalise the downloaded Hypixel SkyBlock pack", it)
                }
            }
        }
    }

    fun remote() {
        if (!DetexturifyConfig.hypixelCache.value) return

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

    private fun enable() {
        if (active != null) return
        val file = PACK ?: URL.value.ifEmpty { null }?.cached()?.takeIf { it.exists() } ?: return

        PACK = file
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

        val original = FilePackResources.FileResourcesSupplier(this)
        //? if >= 26.3 {
        /*val resources = object : Pack.ResourcesSupplier {
            override fun openMetadata(location: PackLocationInfo) = original.openMetadata(location)
            override fun openResources(location: PackLocationInfo, metadata: Pack.Metadata) = original.openResources(location, metadata).map { it.wrap() }
        }
        *///?} else {
        val resources = object : Pack.ResourcesSupplier {
            override fun openPrimary(info: PackLocationInfo): PackResources = original.openPrimary(info).wrap()
            override fun openFull(info: PackLocationInfo, metadata: Pack.Metadata): PackResources = original.openFull(info, metadata).wrap()
        }
        //?}

        return try {
            val location = PackLocationInfo("detexturify/fallback/hypixel", "Detexturify: Hypixel SkyBlock (cached)".literal(), BUILT_IN, Optional.empty())
            val original = Pack.readPackMetadata(location, resources, SharedConstants.getCurrentVersion().packVersion(CLIENT_RESOURCES), CLIENT_RESOURCES) ?: return null
            val metadata = Pack.Metadata("Hypixel SkyBlock textures cached by Detexturify".literal(), original.compatibility(), original.requestedFeatures(), original.overlays())

            Pack(location, resources, metadata, PackSelectionConfig(true, BOTTOM, false))
        } catch (t: Throwable) {
            Detexturify.LOGGER.error("Failed to read Hypixel SkyBlock pack metadata at $this", t)
            null
        }
    }

    private fun String.cached(): File {
        val hash = MessageDigest.getInstance("SHA-256").digest(toByteArray()).joinToString("") { "%02x".format(it) }.take(16)
        return File(CACHE, "pack-$hash.zip")
    }

    private fun PackResources.wrap(): PackResources {
        val original = this

        return object : PackResources by original {
            override fun getResource(type: PackType, location: Identifier): IoSupplier<InputStream>? {
                if (location.path.startsWith("textures/gui/title/background/panorama")) return null
                if (location.namespace == "minecraft" && location.path.startsWith("textures/gui/sprites/tooltip")) return null
                return original.getResource(type, location)
            }

            override fun listResources(type: PackType, namespace: String, path: String, output: PackResources.ResourceOutput) {
                original.listResources(type, namespace, path) { id: Identifier, stream: IoSupplier<InputStream> ->
                    if (id.path.startsWith("textures/gui/title/background/panorama")) return@listResources
                    if (id.namespace == "minecraft" && id.path.startsWith("textures/gui/sprites/tooltip")) return@listResources
                    output.accept(id, stream)
                }
            }

            override fun packId(): String {
                return original.packId()
            }

            override fun knownPackInfo(): Optional<KnownPack> {
                return original.knownPackInfo()
            }
        }
    }
}
