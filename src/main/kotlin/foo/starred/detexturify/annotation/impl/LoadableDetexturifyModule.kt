package foo.starred.detexturify.annotation.impl

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoadableDetexturifyModule(val priority: Int = 0)
