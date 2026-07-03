package foo.starred.detexturify.config

import com.teamresourceful.resourcefulconfigkt.api.ConfigDelegateProvider
import com.teamresourceful.resourcefulconfigkt.api.RConfigKtEntry
import com.teamresourceful.resourcefulconfigkt.api.builders.EntriesBuilder
import xyz.aerii.library.handlers.Observable
import kotlin.reflect.KProperty

fun <T> ConfigDelegateProvider<RConfigKtEntry<T>>.observe(): StateEntry<T> {
    return StateEntry(this)
}

class StateEntry<T>(
    private val entry: ConfigDelegateProvider<RConfigKtEntry<T>>,
) : ConfigDelegateProvider<ReactDelegate<T>> {
    override fun provideDelegate(entries: EntriesBuilder, prop: KProperty<*>): ReactDelegate<T> {
        val delegate = entry.provideDelegate(entries, prop)
        val state = Observable(delegate.get())

        delegate.onChange = { _, n -> state.value = n }
        state.onChange { delegate.set(it) }

        return ReactDelegate(state)
    }
}

class ReactDelegate<T>(val react: Observable<T>) {
    operator fun getValue(thisRef: Any?, property: Any?): Observable<T> = react
    operator fun setValue(thisRef: Any?, property: Any?, value: Observable<T>) {}
}