package foo.starred.detexturify.annotation.loader

import foo.starred.detexturify.annotation.impl.LoadableDetexturifyModule
import foo.starred.klassgraph.KlassGraph
import foo.starred.kontext.util.safely

object DetexturifyAnnotationLoader {
    private val comparator = compareBy<Class<*>>({ it.getAnnotation(LoadableDetexturifyModule::class.java)?.priority ?: 0 }, { it.name })

    fun load(pack: String = "foo.starred.detexturify") {
        KlassGraph.scan(DetexturifyAnnotationLoader::class.java, pack).use { result ->
            val list = result.annotated<LoadableDetexturifyModule>().sortedWith(comparator)

            for (klass in list) {
                safely {
                    Class.forName(klass.name)
                }
            }
        }
    }
}
