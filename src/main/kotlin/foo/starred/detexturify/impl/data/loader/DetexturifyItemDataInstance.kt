package foo.starred.detexturify.impl.data.loader

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import foo.starred.detexturify.Detexturify
import foo.starred.detexturify.annotation.impl.LoadableDetexturifyModule
import foo.starred.detexturify.impl.data.data.SkyBlockItemData
import foo.starred.detexturify.impl.network.NetworkAPI.request
import foo.starred.kontext.api.storage.impl.generic.extension.mutableMap
import foo.starred.kontext.api.storage.impl.generic.extension.mutableSet
import foo.starred.kontext.api.storage.impl.generic.impl.GenericJsonStore
import foo.starred.kontext.api.storage.impl.generic.property.base.AbstractStorageProperty

@LoadableDetexturifyModule
object DetexturifyItemDataInstance {
    private val JSON: GenericJsonStore = GenericJsonStore(Detexturify.Data.name, "detexturify/whitelist")

    @JvmField
    val WHITELIST: AbstractStorageProperty<MutableSet<String>> = JSON.mutableSet("whitelist", Codec.STRING)

    @JvmField
    val BLACKLIST: AbstractStorageProperty<MutableSet<String>> = JSON.mutableSet("blacklist", Codec.STRING)

    @JvmField
    val REPLACEMENTS: AbstractStorageProperty<MutableMap<String, String>> = JSON.mutableMap("replacements", Codec.STRING, Codec.STRING)

    @JvmField
    var MAP: Map<String, SkyBlockItemData> = mapOf()

    init {
        "https://data.starred.foo/items.json".request {
            success<JsonObject> { json ->
                MAP = json.entrySet().associate { (key, value) ->
                    val json = value.asJsonObject
                    key to SkyBlockItemData(json.get("texture")?.asString, json.get("model").asString)
                }
            }
        }
    }
}
