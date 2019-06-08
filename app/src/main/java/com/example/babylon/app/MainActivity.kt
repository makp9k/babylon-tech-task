package com.example.babylon.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.example.babylon.app.di.DaggerMainActivityComponent
import com.example.babylon.common.android.ComponentDependenciesProvider
import com.example.babylon.common.android.HasComponentDependencies
import com.example.babylon.posts.collection.PostsCollectionScreenFragment
import com.example.babylon.posts.detail.PostDetailsScreenFragment
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasComponentDependencies {

    @Inject
    override lateinit var dependencies: ComponentDependenciesProvider
        internal set

    private val postsCollectionScreenCallbacks = object : PostsCollectionScreenFragment.ScreenCallbacks {
        override fun onPostSelected(postId: Int) {
            openPostDetailsScreen(postId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        performInject()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            openPostsCollectionScreen()
        }
    }

    private fun performInject() {
        DaggerMainActivityComponent.factory()
            .create(
                (application as BabylonTechTestApplication).appComponent,
                postsCollectionScreenCallbacks
            )
            .inject(this)
    }

    private fun openPostsCollectionScreen() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, PostsCollectionScreenFragment.create())
            .commit()
    }

    private fun openPostDetailsScreen(postId: Int) {
        supportFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.container, PostDetailsScreenFragment.create(postId))
            .addToBackStack(null)
            .commit()
    }
}
