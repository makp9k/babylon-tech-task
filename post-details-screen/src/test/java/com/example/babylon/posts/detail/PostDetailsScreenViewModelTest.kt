package com.example.babylon.posts.detail

import com.example.babylon.common.android.ApplicationSchedulers
import com.example.babylon.posts.service.PostsService
import com.example.babylon.posts.service.PostsServiceStub
import com.example.babylon.posts.service.entities.PostEntity
import com.example.babylon.posts.service.entities.UserEntity
import com.gojuno.koptional.toOptional
import com.kvazars.arch.core.test.createAndBind
import io.reactivex.schedulers.Schedulers
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.concurrent.TimeoutException

class PostDetailsScreenViewModelTest {

    private val schedulers = ApplicationSchedulers(Schedulers.trampoline(), Schedulers.trampoline())

    private val testPostEntity = PostEntity(
        1,
        "title",
        "body",
        UserEntity(
            1, "user", "avatarUrl"
        ),
        emptyList()
    )

    private fun createViewModel(
        post: PostEntity = testPostEntity,
        error: PostsService.Error? = null
    ) = PostDetailsScreenViewModel(
        PostDetailsScreenViewModel.ScreenParams(post.id),
        PostsServiceStub(listOf(post), error),
        schedulers
    )

    @Test
    fun `should automatically start loading`() {
        val viewModel = createViewModel()

        val loadingState = viewModel.postResourceControl.loading
        val stateObservable = loadingState.observable.distinctUntilChanged().test()
        viewModel.createAndBind()

        stateObservable.assertValues(false, true, false)
    }

    @Test
    fun `should set error on failure`() {
        val error = PostsService.Error.ConnectionError(TimeoutException())
        val viewModel = createViewModel(error = error)

        val errorState = viewModel.postResourceControl.error
        val stateObservable = errorState.observable.distinctUntilChanged().test()
        viewModel.createAndBind()

        assertThat(stateObservable.values()).contains(error.toOptional())
    }

    @Test
    fun `should populate post state`() {
        val viewModel = createViewModel()

        val dataState = viewModel.postResourceControl.data
        val stateObservable = dataState.observable.test()
        viewModel.createAndBind()

        stateObservable.assertValues(testPostEntity.toOptional())
    }

    @Test
    fun `should reload post on refresh action`() {
        val viewModel = createViewModel()

        val dataState = viewModel.postResourceControl.data
        val stateObservable = dataState.observable.test()
        viewModel.createAndBind()

        viewModel.postResourceControl.reloadAction.fire(Unit)

        stateObservable.assertValueCount(2)
    }

}
