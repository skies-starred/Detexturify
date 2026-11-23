package foo.starred.detexturify.impl.command.util

import foo.starred.detexturify.Detexturify
import foo.starred.kommand.IKommand
import foo.starred.kommand.scopes.KommandBuilderScope
import foo.starred.kommand.scopes.KommandCommandScope
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

fun command(block: KommandBuilderScope<FabricClientCommandSource>.() -> Unit) {
    DetexturifyCommandSource.command(Detexturify.Data.id, block)
}

object DetexturifyCommandSource : IKommand<FabricClientCommandSource> {
    override val loader: KommandCommandScope<FabricClientCommandSource> = KommandCommandScope()

    init {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            loader.register(dispatcher)
        }
    }
}
