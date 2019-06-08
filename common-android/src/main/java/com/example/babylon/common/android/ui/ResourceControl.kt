package com.example.babylon.common.android.ui

import com.example.babylon.common.Resource
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.gojuno.koptional.toOptional
import com.kvazars.arch.core.LibViewModel
import com.kvazars.arch.core.WidgetControl
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.withLatestFrom

class ResourceControl<Data, Error : Any> internal constructor(private val vm: LibViewModel) : WidgetControl {

    val loading = vm.State(initialValue = false)
    val error = vm.State<Optional<Error>>()
    val data = vm.State<Data>()

    val reloadAction = vm.Action<Unit>()

    fun observe(dataSource: Observable<Resource<Data, Error>>, scheduler: Scheduler): Observable<Resource<Data, Error>> {
        return reloadAction.relay
            .startWith(Unit)
            .observeOn(scheduler)
            .switchMap { dataSource }
            .doOnSubscribe {
                loading.relay.accept(false)
                error.relay.accept(None)
            }
            .doOnNext {
                loading.relay.accept(false)
                error.relay.accept(None)
            }
            .doOnNext { resource ->
                when(resource) {
                    Resource.Progress -> loading.relay.accept(true)
                    is Resource.Result -> data.relay.accept(resource.data)
                    is Resource.Failure -> error.relay.accept(resource.error.toOptional())
                }
            }
    }

    fun retryOnBind() {
        with (vm) {
            lifecycleEvents.filter { it == LibViewModel.Lifecycle.BIND }
                .withLatestFrom(error.observable)
                .filter { (_, error) -> error is Some }
                .map { Unit }
                .subscribe(reloadAction.consumer)
                .untilDestroy()
        }
    }
}

fun <Data, Error : Any> LibViewModel.resourceControl(): ResourceControl<Data, Error> {
    return ResourceControl(this)
}
