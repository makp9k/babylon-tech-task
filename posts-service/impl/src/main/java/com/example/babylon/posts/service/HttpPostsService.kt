package com.example.babylon.posts.service

import com.example.babylon.common.Resource
import com.example.babylon.posts.service.entities.CommentEntity
import com.example.babylon.posts.service.entities.PostEntity
import com.example.babylon.posts.service.entities.UserEntity
import com.example.babylon.posts.service.models.CommentModel
import com.example.babylon.posts.service.models.PostModel
import com.example.babylon.posts.service.models.UserModel
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function3
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

class HttpPostsService constructor(httpClient: OkHttpClient, gson: Gson) : PostsService {

    private val remoteApi = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com")
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(PostsRemoteApi::class.java)

    private var cachedPostsHolder = AtomicReference(emptyList<PostEntity>())

    override fun getPosts(): Observable<Resource<List<PostEntity>, PostsService.Error>> {
        val cachedPosts = cachedPostsHolder.get()
        return if (cachedPosts.isNotEmpty()) {
            Observable.just(Resource.Result(cachedPosts))
        } else {
            createResourceEmitter(loadRemotePosts())
                .doOnNext { resource ->
                    if (resource is Resource.Result) {
                        cachedPostsHolder.set(resource.data)
                    }
                }
        }
    }

    override fun getPostById(id: Int): Observable<Resource<Optional<PostEntity>, PostsService.Error>> {
        return getPosts()
            .map<Resource<Optional<PostEntity>, PostsService.Error>> { resource ->
                when (resource) {
                    is Resource.Result -> Resource.Result(resource.data.firstOrNull { it.id == id }.toOptional())
                    is Resource.Progress -> resource
                    is Resource.Failure -> resource
                }
            }
    }

    private fun <T> createResourceEmitter(dataSource: Single<T>): Observable<Resource<T, PostsService.Error>> {
        return Observable
            .concat(
                Observable.just(Resource.Progress),
                dataSource
                    .map<Resource<T, PostsService.Error>> {
                        Resource.Result(it)
                    }
                    .onErrorReturn {
                        when (it) {
                            is HttpException -> Resource.Failure(PostsService.Error.HttpError(it.code(), it))
                            is IOException -> Resource.Failure(PostsService.Error.ConnectionError(it))
                            else -> Resource.Failure(PostsService.Error.UnknownError(it))
                        }
                    }
                    .toObservable()
            )
    }

    private fun loadRemotePosts(): Single<List<PostEntity>> {
        return Single.zip(
            remoteApi.loadPosts(),
            remoteApi.loadUsers(),
            remoteApi.loadComments(),
            Function3 { posts: List<PostModel>, users: List<UserModel>, comments: List<CommentModel> ->
                combine(posts, users, comments)
            }
        )
    }

    private fun combine(
        posts: List<PostModel>,
        users: List<UserModel>,
        comments: List<CommentModel>
    ): List<PostEntity> {
        val commentsByPostIds = comments.groupBy { it.postId }
        val usersByIds = users.associateBy { it.id }
        return posts.map { post ->
            PostEntity(
                post.id,
                post.title,
                post.body,
                usersByIds[post.userId].toEntity(),
                commentsByPostIds[post.id].toEntities()
            )
        }
    }

    private fun UserModel?.toEntity(): UserEntity {
        return this?.run {
            UserEntity(id, name, "https://api.adorable.io/avatars/$id")
        } ?: UserEntity(-1, "Unknown User", "https://api.adorable.io/avatars/0")
    }

    private fun List<CommentModel>?.toEntities(): List<CommentEntity> {
        return this?.map {
            CommentEntity(it.id, it.name, it.email, it.body)
        } ?: emptyList()
    }
}
