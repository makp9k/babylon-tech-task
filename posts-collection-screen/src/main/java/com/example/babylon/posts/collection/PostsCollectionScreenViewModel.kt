package com.example.babylon.posts.collection

import com.example.babylon.common.Resource
import com.example.babylon.common.android.ApplicationSchedulers
import com.example.babylon.common.android.ui.resourceControl
import com.example.babylon.posts.service.PostsService
import com.example.babylon.posts.service.entities.PostEntity
import com.gojuno.koptional.toOptional
import com.kvazars.arch.core.LibViewModel
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class PostsCollectionScreenViewModel @Inject constructor(
    private val postsService: PostsService,
    private val schedulers: ApplicationSchedulers
) : LibViewModel() {

    data class PostModel(
        val id: Int,
        val text: CharSequence,
        val authorName: String,
        val avatarUrl: String
    )

    private val resourceMapper = { resource: Resource<List<PostEntity>, PostsService.Error> ->
        when (resource) {
            is Resource.Progress -> resource
            is Resource.Failure -> resource
            is Resource.Result -> Resource.Result(resource.data.map { it.mapToPostModel() })
        }
    }

    val postsCollectionResourceControl = resourceControl<List<PostModel>, PostsService.Error>()

    val selectPostAction = Action<PostModel>()

    val dispatchSelectedPostIdCommand = Command<Int>()

    override fun onCreate() {
        postsCollectionResourceControl
            .observe(postsService.getPosts().map(resourceMapper), schedulers.ioScheduler)
            .subscribeBy(onError = {
                postsCollectionResourceControl.error.set(PostsService.Error.UnknownError(it).toOptional())
            })
            .untilDestroy()

        postsCollectionResourceControl.retryOnBind()

        selectPostAction.observable
            .map { it.id }
            .subscribe {
                dispatchSelectedPostIdCommand.consumer.accept(it)
            }
            .untilDestroy()
    }

    private fun PostEntity.mapToPostModel(): PostModel {
        return PostModel(id, title, author.name, author.avatarUrl)
    }

}
