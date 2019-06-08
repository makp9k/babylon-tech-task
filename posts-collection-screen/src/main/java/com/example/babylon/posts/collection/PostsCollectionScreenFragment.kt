package com.example.babylon.posts.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.babylon.common.android.findComponentDependencies
import com.example.babylon.posts.collection.di.DaggerPostsCollectionScreenComponent
import com.example.babylon.posts.service.PostsService
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import com.kvazars.arch.controls.list.recyclerview.bind
import com.kvazars.arch.core.base.LibFragment
import com.kvazars.arch.core.setBindings
import kotlinx.android.synthetic.main.layout_posts_collection_fragment.*
import kotlinx.android.synthetic.main.view_post_list_item.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider

class PostsCollectionScreenFragment : LibFragment<PostsCollectionScreenViewModel>() {

    companion object {
        fun create() = PostsCollectionScreenFragment()
    }

    interface ScreenCallbacks {
        fun onPostSelected(postId: Int)
    }

    @Inject
    internal lateinit var vmProvider: Provider<PostsCollectionScreenViewModel>

    @Inject
    internal lateinit var screenCallbacks: ScreenCallbacks

    private var currentSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerPostsCollectionScreenComponent.builder()
            .postsCollectionScreenDependencies(
                findComponentDependencies()
            )
            .build()
            .inject(this)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_posts_collection_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipe_refresh_layout.setBindings {
            bind(vm.postsCollectionResourceControl.loading.observable.debounce(100, TimeUnit.MILLISECONDS)) {
                swipe_refresh_layout.isRefreshing = it
            }

            swipe_refresh_layout.setOnRefreshListener {
                vm.postsCollectionResourceControl.reloadAction.fire(Unit)
            }
        }

        view.setBindings {
            bind(vm.postsCollectionResourceControl.error.observable.distinctUntilChanged()) {
                currentSnackbar?.dismiss()

                it.toNullable()?.let { error ->
                    currentSnackbar = Snackbar.make(
                        view,
                        when (error) {
                            is PostsService.Error.ConnectionError -> "Network error, swipe to refresh"
                            else -> "Unknown error"
                        },
                        Snackbar.LENGTH_LONG
                    ).apply {
                        show()
                    }
                }
            }

            bind(vm.dispatchSelectedPostIdCommand) { postId ->
                screenCallbacks.onPostSelected(postId)
            }
        }

        recycler_view.setBindings {
            bind(
                items = vm.postsCollectionResourceControl.data.observable,
                recyclerView = recycler_view,
                layoutId = R.layout.view_post_list_item,
                initializer = { adapter ->
                    bind(containerView.clicks().map { adapter.currentList[adapterPosition] }, vm.selectPostAction)
                },
                binder = { _, item, _ ->
                    post_title_txt.text = item.text
                    author_name_txt.text = "by ${item.authorName}"
                    Glide.with(this@PostsCollectionScreenFragment).load(item.avatarUrl).into(author_avatar_img)
                }
            )
        }
    }

    override fun createViewModel(): PostsCollectionScreenViewModel {
        return vmProvider.get()
    }

}
