package com.example.babylon.posts.service

import com.example.babylon.common.Resource
import com.example.babylon.posts.service.entities.CommentEntity
import com.example.babylon.posts.service.entities.PostEntity
import com.example.babylon.posts.service.entities.UserEntity
import com.gojuno.koptional.None
import com.gojuno.koptional.toOptional
import com.google.gson.Gson
import okhttp3.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import retrofit2.HttpException

class HttpPostsServiceTest {

    private fun createHttpPostsService(httpClient: OkHttpClient = OkHttpClient()): HttpPostsService {
        return HttpPostsService(httpClient, Gson())
    }

    private fun createHttpClient(vararg responseBuilders: Response.Builder): OkHttpClient {
        val queue = responseBuilders.toMutableList()
        return OkHttpClient.Builder()
            .addInterceptor {
                queue.removeAt(0).request(it.request()).build()
            }
            .build()
    }

    private fun createResponseBuilder(httpCode: Int = 200, jsonBody: String): Response.Builder {
        return Response.Builder()
            .protocol(Protocol.HTTP_1_1)
            .message("Http Status Message")
            .code(httpCode)
            .body(ResponseBody.create(MediaType.get("application/json"), jsonBody))
    }

    private val defaultPostsResponseBuilder = createResponseBuilder(
        jsonBody = """
            [{
                "userId": 1,
                "id": 1,
                "title": "Post Title",
                "body": "Post Body"
            }]
        """.trimIndent()
    )

    private val defaultUserResponseBuilder = createResponseBuilder(
        jsonBody = """
            [{
                "id": 1,
                "name": "Leanne Graham",
                "username": "Bret",
                "email": "Sincere@april.biz",
                "address": {
                  "street": "Kulas Light",
                  "suite": "Apt. 556",
                  "city": "Gwenborough",
                  "zipcode": "92998-3874",
                  "geo": {
                    "lat": "-37.3159",
                    "lng": "81.1496"
                  }
                },
                "phone": "1-770-736-8031 x56442",
                "website": "hildegard.org",
                "company": {
                  "name": "Romaguera-Crona",
                  "catchPhrase": "Multi-layered client-server neural-net",
                  "bs": "harness real-time e-markets"
                }
            }]
        """.trimIndent()
    )

    private val defaultCommentsResponseBuilder = createResponseBuilder(
        jsonBody = """
            [{
                "postId": 1,
                "id": 1,
                "name": "User Name",
                "email": "Eliseo@gardner.biz",
                "body": "Comment Body"
            }]
        """.trimIndent()
    )

    @Test
    fun `should combine posts, users and comments responses in one PostEntity list`() {
        val postsService = createHttpPostsService(
            createHttpClient(
                defaultPostsResponseBuilder,
                defaultUserResponseBuilder,
                defaultCommentsResponseBuilder
            )
        )

        val observer = postsService.getPosts().test()

        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValueCount(2)

        val resource = observer.values()[1]
        assertThat(resource).isInstanceOf(Resource.Result::class.java)

        val resultResource = resource as Resource.Result
        assertThat(resultResource.data.size).isEqualTo(1)
        assertThat(resultResource.data[0]).isEqualTo(
            PostEntity(
                1,
                "Post Title",
                "Post Body",
                UserEntity(
                    1,
                    "Leanne Graham",
                    "https://api.adorable.io/avatars/1"
                ),
                listOf(CommentEntity(
                    1,
                    "User Name",
                    "Eliseo@gardner.biz",
                    "Comment Body"
                ))
            )
        )
    }

    @Test
    fun `should set user to unknown if not able to find one`() {
        val postsService = createHttpPostsService(
            createHttpClient(
                defaultPostsResponseBuilder,
                createResponseBuilder(
                    jsonBody = """
                        [{
                            "id": 2,
                            "name": "Leanne Graham",
                            "username": "Bret",
                            "email": "Sincere@april.biz",
                            "address": {
                              "street": "Kulas Light",
                              "suite": "Apt. 556",
                              "city": "Gwenborough",
                              "zipcode": "92998-3874",
                              "geo": {
                                "lat": "-37.3159",
                                "lng": "81.1496"
                              }
                            },
                            "phone": "1-770-736-8031 x56442",
                            "website": "hildegard.org",
                            "company": {
                              "name": "Romaguera-Crona",
                              "catchPhrase": "Multi-layered client-server neural-net",
                              "bs": "harness real-time e-markets"
                            }
                        }]
                    """.trimIndent()
                ),
                defaultCommentsResponseBuilder
            )
        )

        val observer = postsService.getPosts().test()
        assertThat((observer.values()[1] as Resource.Result).data[0].author).isEqualTo(
            UserEntity(
                -1,
                "Unknown User",
                "https://api.adorable.io/avatars/0"
            )
        )
    }

    @Test
    fun `should return failure with correct payload`() {
        val postsService = createHttpPostsService(
            createHttpClient(
                createResponseBuilder(409, "{}")
            )
        )

        val observer = postsService.getPosts().test()
        observer.assertNoErrors()
        observer.assertValueCount(2)
        assertThat(observer.values()[1]).isInstanceOf(Resource.Failure::class.java)

        val failureResource = observer.values()[1] as Resource.Failure
        assertThat(failureResource.error).isInstanceOf(PostsService.Error.HttpError::class.java)

        val networkError = failureResource.error as PostsService.Error.HttpError
        assertThat(networkError.httpCode).isEqualTo(409)
        assertThat(networkError.error).isInstanceOf(HttpException::class.java)
    }

    @Test
    fun `should find comment by id`() {
        val postsService = createHttpPostsService(
            createHttpClient(
                defaultPostsResponseBuilder,
                defaultUserResponseBuilder,
                defaultCommentsResponseBuilder
            )
        )

        val observer = postsService.getPostById(1).test()
        observer.assertNoErrors()
        observer.assertValueCount(2)
        assertThat((observer.values()[1] as Resource.Result).data).isEqualTo(
            PostEntity(
                1,
                "Post Title",
                "Post Body",
                UserEntity(
                    1,
                    "Leanne Graham",
                    "https://api.adorable.io/avatars/1"
                ),
                listOf(CommentEntity(
                    1,
                    "User Name",
                    "Eliseo@gardner.biz",
                    "Comment Body"
                ))
            ).toOptional()
        )
    }

    @Test
    fun `should return None if post was not found`() {
        val postsService = createHttpPostsService(
            createHttpClient(
                createResponseBuilder(200, "[]"),
                defaultUserResponseBuilder,
                defaultCommentsResponseBuilder
            )
        )

        val observer = postsService.getPostById(1).test()
        observer.assertNoErrors()
        observer.assertValueCount(2)
        assertThat((observer.values()[1] as Resource.Result).data).isEqualTo(None)
    }

}
