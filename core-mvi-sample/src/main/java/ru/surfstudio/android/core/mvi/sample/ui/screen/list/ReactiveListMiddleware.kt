package ru.surfstudio.android.core.mvi.sample.ui.screen.list

import io.reactivex.Observable
import ru.surfstudio.android.core.mvi.sample.domain.datalist.DataList
import ru.surfstudio.android.core.mvi.sample.ui.base.middleware.*
import ru.surfstudio.android.core.mvi.sample.ui.screen.list.event.ReactiveListEvent
import ru.surfstudio.android.core.mvi.sample.ui.screen.list.reactor.ReactiveListStateHolder
import ru.surfstudio.android.core.mvi.util.filterIsInstance
import ru.surfstudio.android.dagger.scope.PerScreen
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import ru.surfstudio.android.core.mvi.sample.ui.screen.list.event.ReactiveListEvent.*
import ru.surfstudio.android.core.mvi.sample.ui.screen.list.extension.canGetMore

@PerScreen
class ReactiveListMiddleware @Inject constructor(
        baseMiddlewareDependency: BaseMiddlewareDependency,
        private val sh: ReactiveListStateHolder
) : BaseMiddleware<ReactiveListEvent>(baseMiddlewareDependency),
        SwrMiddleware<ReactiveListEvent>,
        LoadNextMiddleware<ReactiveListEvent>,
        LifecycleMiddleware<ReactiveListEvent>,
        PaginationMiddleware<ReactiveListEvent> {

    override fun transform(eventStream: Observable<ReactiveListEvent>): Observable<out ReactiveListEvent> {
        return merge(eventStream) {
            +onCreate().eventMap { loadData() }
            +mapLoadNext { sh.list.data.nextPage }
            +mapPagination(FilterNumbers()) { sh.list.canGetMore() }
            +mapSwr()
            +map<QueryChangedDebounced> { FilterNumbers() }
            +eventMap<Reload> { loadData() }
            +streamMap(::debounceQuery)
        }
    }

    private fun debounceQuery(
            eventStream: Observable<ReactiveListEvent>
    ): Observable<out ReactiveListEvent> =
            eventStream
                    .filterIsInstance<QueryChanged>()
                    .map { it.query }
                    .distinctUntilChanged()
                    .debounce(1000, TimeUnit.MILLISECONDS)
                    .map { QueryChangedDebounced(it) }

    override fun loadData(page: Int, isSwr: Boolean) = createObservable(page)
            .io()
            .handleError()
            .mapToLoadable(LoadList(isSwr = isSwr))

    private fun createObservable(page: Int = 0) = Observable.timer(2, TimeUnit.SECONDS).map {
        DataList(
                (1..20).map { (it + (page - 1) * 20).toString() },
                page,
                20,
                100,
                5
        )
    }
}