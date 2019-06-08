package com.example.babylon.posts.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.babylon.common.android.findComponentDependencies
import com.example.babylon.posts.detail.di.DaggerPostDetailsScreenComponent
import com.example.babylon.posts.service.PostsService
import com.gojuno.koptional.rxjava2.filterSome
import com.google.android.material.snackbar.Snackbar
import com.kvazars.arch.core.base.LibFragment
import com.kvazars.arch.core.setBindings
import kotlinx.android.synthetic.main.layout_post_details_fragment.*
import javax.inject.Inject
import javax.inject.Provider

class PostDetailsScreenFragment : LibFragment<PostDetailsScreenViewModel>() {

    companion object {
        private const val ARG_POST_ID = "postId"

        fun create(postId: Int) = PostDetailsScreenFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_POST_ID, postId)
            }
        }
    }

    @Inject
    internal lateinit var vmProvider: Provider<PostDetailsScreenViewModel>

    private var currentSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerPostDetailsScreenComponent.factory()
            .create(
                findComponentDependencies(),
                PostDetailsScreenViewModel.ScreenParams(
                    arguments?.getInt(ARG_POST_ID)
                        ?: throw IllegalArgumentException("postId is missing, please use PostDetailsScreenFragment#create")
                )
            )
            .inject(this)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_post_details_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBindings {
            bind(vm.postResourceControl.loading) {
                if (it) progress_bar.show() else progress_bar.hide()
            }

            bind(vm.postResourceControl.data.observable.filterSome()) {
                post_title_txt.text = it.title
                post_body_txt.text = it.body
                author_txt.text = "by ${it.author.name}"
                total_comments_txt.text = "Number of comments: ${it.comments.size}"
                Glide.with(this@PostDetailsScreenFragment).load(it.author.avatarUrl).into(author_avatar_img)
            }

            bind(vm.postResourceControl.error.observable.distinctUntilChanged()) {
                currentSnackbar?.dismiss()

                it.toNullable()?.let { error ->
                    currentSnackbar = Snackbar
                        .make(
                            view,
                            when (error) {
                                is PostsService.Error.ConnectionError -> "Network error"
                                else -> "Unknown error"
                            },
                            Snackbar.LENGTH_INDEFINITE
                        )
                        .setAction("Retry") {
                            vm.postResourceControl.reloadAction.fire(Unit)
                        }
                        .apply {
                            show()
                        }
                }
            }
        }
    }

    override fun createViewModel(): PostDetailsScreenViewModel {
        return vmProvider.get()
    }

}
