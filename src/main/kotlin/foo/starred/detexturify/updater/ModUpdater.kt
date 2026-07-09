package foo.starred.detexturify.updater

import com.google.gson.JsonElement
import foo.starred.detexturify.Detexturify
import foo.starred.detexturify.handlers.Chronos
import moe.nea.libautoupdate.CurrentVersion
import moe.nea.libautoupdate.PotentialUpdate
import moe.nea.libautoupdate.UpdateContext
import moe.nea.libautoupdate.UpdateTarget
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.SharedConstants
import xyz.aerii.library.api.client
import xyz.aerii.library.api.lie
import xyz.aerii.library.handlers.parser.parse
import xyz.aerii.library.handlers.time.Task
import xyz.aerii.library.kommand.ICommand
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration.Companion.seconds

object ModUpdater : ICommand {
    private var skippedVersion: String by Detexturify.SCRIBBLE.string("version")
    private var bool: Boolean = false
    private var task: Task? = null

    private val context = UpdateContext(
        ModrinthUpdateSource("agQYgu5m", SharedConstants.getCurrentVersion().name()),
        UpdateTarget.deleteAndSaveInTheSameFolder(Detexturify::class.java),
        current(),
        Detexturify.modId
    )

    init {
        context.cleanup()

        command(Detexturify.modId) {
            "update" {
                installUpdate()
            }
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            if (bool) return@register

            fun fn() {
                task?.cancel()
                task = Chronos.schedule(3.seconds) {
                    //~ if >= 26.2 'client.screen' -> 'client.gui.screen()'
                    if (client.screen != null) return@schedule fn()

                    bool = true
                    task = null
                    checkAndNotify()
                }
            }

            fn()
        }
    }

    fun checkForUpdate(stream: String = "release"): CompletableFuture<PotentialUpdate> {
        return context.checkUpdate(stream)
    }

    fun checkAndNotify(stream: String = "release", silent: Boolean = true) {
        checkForUpdate(stream).thenAccept { update ->
            if (!silent && !update.isUpdateAvailable) return@thenAccept "<#FAB387>[Detexturify]<r> No update available!".parse(true).lie()
            if (!update.isUpdateAvailable) return@thenAccept

            val newVersion = update.update.versionName

            "<#FAB387>[Detexturify]<r> Update available: $newVersion".parse(true).lie()
            "<#FAB387>[Detexturify]<r> Run /${Detexturify.modId} update to install".parse(true).lie()

            if (newVersion == skippedVersion) return@thenAccept
            UpdateGUI(Detexturify.modVersion, newVersion, onUpdate = { installUpdate(stream) }, onSkip = { skippedVersion = newVersion }, onRemind = {}).open()
        }.exceptionally {
            Detexturify.LOGGER.error("Failed to check for updates: ${it.message}")
            null
        }
    }

    fun installUpdate(stream: String = "release"): CompletableFuture<Boolean> {
        return checkForUpdate(stream).thenCompose { update ->
            if (!update.isUpdateAvailable) {
                "<#FAB387>[Detexturify]<r> Already on latest version".parse(true).lie()
                return@thenCompose CompletableFuture.completedFuture(false)
            }

            "<#FAB387>[Detexturify]<r> Downloading update: ${update.update.versionName}".parse(true).lie()
            update.launchUpdate().thenApply {
                "<#FAB387>[Detexturify]<r> Update downloaded! Restart to apply.".parse(true).lie()
                true
            }
        }.exceptionally {
            "<#FAB387>[Detexturify]<r> Update failed: ${it.message}".parse(true).lie()
            Detexturify.LOGGER.error("Failed to install update: ${it.message}")
            false
        }
    }

    private fun current() = object : CurrentVersion {
        override fun display() = Detexturify.modVersion

        override fun isOlderThan(element: JsonElement): Boolean {
            if (!element.isJsonPrimitive) return true

            fun String.parse() = removePrefix("v").split('.', '-').map { it.toIntOrNull() ?: 0 }

            val local = Detexturify.modVersion.parse()
            val remote = element.asString.parse()

            val maxLength = maxOf(local.size, remote.size)
            val l = local + List(maxLength - local.size) { 0 }
            val r = remote + List(maxLength - remote.size) { 0 }

            for (i in 0 until maxLength) {
                if (l[i] < r[i]) return true
                if (l[i] > r[i]) return false
            }

            return false
        }
    }
}