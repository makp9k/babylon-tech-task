package com.kvazars.arch.core

import io.reactivex.Observable
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito
import org.mockito.Mockito.*

@RunWith(JUnit4::class)
class LibViewModelTest {

    private open class ViewModelUnderTest : LibViewModel() {
        val exposedLifecycle = lifecycleEvents

        public override fun onBind() {
            super.onBind()
        }

        public override fun onUnbind() {
            super.onUnbind()
        }

        public override fun onCleared() {
            super.onCleared()
        }
    }

    private lateinit var vmUnderTest: ViewModelUnderTest

    @Before
    fun setUp() {
        vmUnderTest = spy(ViewModelUnderTest())
    }

    @Test
    fun onBindShouldMoveToBoundState() {
        val lifecycleTestObserver = vmUnderTest.exposedLifecycle.test()

        vmUnderTest.bind()

        lifecycleTestObserver.assertValues(LibViewModel.Lifecycle.BIND)
        verify(vmUnderTest, only()).onBind()
    }

    @Test
    fun onUnbindShouldMoveToUnboundState() {
        val lifecycleTestObserver = vmUnderTest.exposedLifecycle.test()

        vmUnderTest.unbind()

        lifecycleTestObserver.assertValues(LibViewModel.Lifecycle.UNBIND)
        verify(vmUnderTest, only()).onUnbind()
    }

    @Test
    fun onClearedShouldMoveToDestroyedState() {
        val lifecycleTestObserver = vmUnderTest.exposedLifecycle.test()

        vmUnderTest.onCleared()

        lifecycleTestObserver.assertValues(LibViewModel.Lifecycle.DESTROY)
        verify(vmUnderTest, only()).onCleared()
    }

    @Test
    fun shouldReactOnlyOnceOnRepeatableCalls() {
        val lifecycleTestSubscriber = vmUnderTest.exposedLifecycle.test()
        val inOrder = Mockito.inOrder(vmUnderTest)

        vmUnderTest.create()
        vmUnderTest.create()
        vmUnderTest.create()
        vmUnderTest.bind()
        vmUnderTest.bind()
        vmUnderTest.unbind()
        vmUnderTest.unbind()
        vmUnderTest.bind()
        vmUnderTest.unbind()
        vmUnderTest.unbind()
        vmUnderTest.unbind()
        vmUnderTest.clear()
        vmUnderTest.clear()

        lifecycleTestSubscriber.assertValues(
            LibViewModel.Lifecycle.CREATE,
            LibViewModel.Lifecycle.BIND,
            LibViewModel.Lifecycle.UNBIND,
            LibViewModel.Lifecycle.BIND,
            LibViewModel.Lifecycle.UNBIND,
            LibViewModel.Lifecycle.DESTROY
        )

        inOrder.verify(vmUnderTest).onCreate()
        inOrder.verify(vmUnderTest).onBind()
        inOrder.verify(vmUnderTest).onUnbind()
        inOrder.verify(vmUnderTest).onBind()
        inOrder.verify(vmUnderTest).onUnbind()
        // we don't check if onCleared was called once because the android arch components are responsible for that
    }

    @Test
    fun unbindShouldDisposeUntilUnbindSubscriptions() {
        val observable = Observable.never<Unit>()
        val subscription = with(vmUnderTest) {
            observable.subscribe().apply { untilUnbind() }
        }

        vmUnderTest.unbind()

        assertThat(subscription.isDisposed, equalTo(true))
    }

    @Test
    fun clearShouldDisposeUntilDestroyedSubscriptions() {
        val observable = Observable.never<Unit>()
        val subscription = with(vmUnderTest) {
            observable.subscribe().apply { untilDestroy() }
        }

        vmUnderTest.clear()

        assertThat(subscription.isDisposed, equalTo(true))
    }

    @Test
    fun stateShouldCacheItsValue() {
        val state: LibViewModel.State<Int?> = vmUnderTest.State()

        assertThat(state.valueOrNull, equalTo<Int>(null))

        state.relay.accept(1)

        assertThat(state.valueOrNull, equalTo(1))
    }

    @Test
    fun commandShouldBufferEmissionsUntilBound() {
        val command = vmUnderTest.Command<Int>(2)
        val commandsObserver = command.observable.test()

        command.relay.accept(1)

        commandsObserver.assertEmpty()

        vmUnderTest.bind()
        command.relay.accept(2)

        commandsObserver.assertValues(1, 2)

        vmUnderTest.unbind()

        command.relay.accept(3)
        command.relay.accept(4)
        command.relay.accept(5)

        commandsObserver.assertValues(1, 2)

        vmUnderTest.bind()
        commandsObserver.assertValues(1, 2, 4, 5)
    }

}