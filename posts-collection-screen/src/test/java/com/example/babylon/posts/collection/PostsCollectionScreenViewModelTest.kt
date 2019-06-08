package com.example.babylon.posts.collection

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

class PostsCollectionScreenViewModelTest {

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
        posts: List<PostEntity> = listOf(testPostEntity),
        error: PostsService.Error? = null
    ) = PostsCollectionScreenViewModel(PostsServiceStub(posts, error), schedulers)

    @Test
    fun `should automatically start loading`() {
        val viewModel = createViewModel()

        val loadingState = viewModel.postsCollectionResourceControl.loading
        val stateObservable = loadingState.observable.distinctUntilChanged().test()
        viewModel.createAndBind()

        stateObservable.assertValues(false, true, false)
    }

    @Test
    fun `should set error on failure`() {
        val error = PostsService.Error.ConnectionError(TimeoutException())
        val viewModel = createViewModel(error = error)

        val errorState = viewModel.postsCollectionResourceControl.error
        val stateObservable = errorState.observable.distinctUntilChanged().test()
        viewModel.createAndBind()

        assertThat(stateObservable.values()).contains(error.toOptional())
    }

    @Test
    fun `should populate posts state`() {
        val viewModel = createViewModel()

        val dataState = viewModel.postsCollectionResourceControl.data
        val stateObservable = dataState.observable.test()
        viewModel.createAndBind()

        stateObservable.assertValues(
            listOf(
                PostsCollectionScreenViewModel.PostModel(
                    testPostEntity.id,
                    testPostEntity.title,
                    testPostEntity.author.name,
                    testPostEntity.author.avatarUrl
                )
            )
        )
    }

    @Test
    fun `should reload posts on refresh action`() {
        val viewModel = createViewModel()

        val dataState = viewModel.postsCollectionResourceControl.data
        val stateObservable = dataState.observable.test()
        viewModel.createAndBind()

        viewModel.postsCollectionResourceControl.reloadAction.fire(Unit)

        stateObservable.assertValueCount(2)
    }

    @Test
    fun `should dispatch navigation command on post item click action`() {
        val viewModel = createViewModel()

        val commandObservable = viewModel.dispatchSelectedPostIdCommand.observable.test()
        viewModel.createAndBind()
        viewModel.selectPostAction.fire(viewModel.postsCollectionResourceControl.data.value[0])

        commandObservable.assertValue(1)
    }

}
