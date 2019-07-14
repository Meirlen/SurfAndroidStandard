package ru.surfstudio.android.core.mvi.ui.reactor

import io.reactivex.Observable
import ru.surfstudio.android.core.mvi.event.Event
import ru.surfstudio.android.core.mvi.ui.effect.CommandSideEffect
import ru.surfstudio.android.core.mvi.ui.effect.SideEffect
import ru.surfstudio.android.core.mvi.ui.effect.ObservableSideEffect
import ru.surfstudio.android.core.mvi.ui.effect.StateSideEffect
import ru.surfstudio.android.core.mvp.binding.rx.relation.mvp.Command
import ru.surfstudio.android.core.mvp.binding.rx.relation.mvp.State

/**
 * Класс, содержащий состояние экрана
 *
 * @property sideEffects список возможных сайд-эффектов для состояний экрана
 */
interface RxStateHolder<E : Event> {

    val sideEffects: List<SideEffect<out E, *>>

    infix fun <T> Observable<T>.with(transformer: (T) -> E): SideEffect<E, T> =
            ObservableSideEffect(this, transformer)

    infix fun <T> State<T>.with(transformer: (T) -> E): SideEffect<E, T> =
            StateSideEffect(this, transformer)

    infix fun <T> Command<T>.with(transformer: (T) -> E): SideEffect<E, T> =
            CommandSideEffect(this, transformer)
}