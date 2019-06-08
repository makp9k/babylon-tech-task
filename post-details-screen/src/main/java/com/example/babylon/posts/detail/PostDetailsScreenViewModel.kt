package com.example.babylon.posts.detail

import com.example.babylon.common.android.ApplicationSchedulers
import com.example.babylon.common.android.ui.resourceControl
import com.example.babylon.posts.service.PostsService
import com.example.babylon.posts.service.entities.PostEntity
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import com.kvazars.arch.core.LibViewModel
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class PostDetailsScreenViewModel @Inject constructor(
    private val screenParams: ScreenParams,
    private val postsService: PostsService,
    private val schedulers: ApplicationSchedulers
) : LibViewModel() {

    data class ScreenParams(val postId: Int)

    val postResourceControl = resourceControl<Optional<PostEntity>, PostsService.Error>()

    override fun onCreate() {
        postResourceControl
            .observe(postsService.getPostById(screenParams.postId), schedulers.ioScheduler)
            .subscribeBy(onError =  {
                postResourceControl.error.set(PostsService.Error.UnknownError(it).toOptional())
            })
            .untilDestroy()

        postResourceControl.retryOnBind()
    }

}
