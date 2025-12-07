// CommunityApiService.kt
package com.example.natal

import retrofit2.Call
import retrofit2.http.*

interface CommunityApiService {
    // Categories
    @GET("community/categories")
    fun getCategories(): Call<CommunityResponse<List<Category>>>

    @POST("community/categories")
    fun createCategory(@Body category: CreateCategoryRequest): Call<CommunityResponse<String>>

    @POST("community/subscribe/{category_id}")
    fun subscribeToCategory(
        @Path("category_id") categoryId: Int,
        @Body request: SubscriptionRequest
    ): Call<CommunityResponse<String>>

    @DELETE("community/unsubscribe/{category_id}")
    fun unsubscribeFromCategory(
        @Path("category_id") categoryId: Int,
        @Body request: SubscriptionRequest
    ): Call<CommunityResponse<String>>

    // Posts
    @GET("community/posts")
    fun getPosts(
        @Query("category_id") categoryId: Int? = null,
        @Query("sort_by") sortBy: String = "new",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Call<CommunityResponse<List<Post>>>

    @GET("community/posts/{post_id}")
    fun getPost(@Path("post_id") postId: Int): Call<CommunityResponse<Post>>

    @POST("community/posts")
    fun createPost(@Body request: CreatePostRequest): Call<CommunityResponse<Int>>

    // Comments
    @GET("community/posts/{post_id}/comments")
    fun getComments(@Path("post_id") postId: Int): Call<CommunityResponse<List<Comment>>>

    @POST("community/comments")
    fun createComment(@Body request: CreateCommentRequest): Call<CommunityResponse<String>>

    // Votes
    @POST("community/vote")
    fun vote(
        @Query("post_id") postId: Int? = null,
        @Query("comment_id") commentId: Int? = null,
        @Body request: VoteRequest
    ): Call<CommunityResponse<VoteResponse>>
}

data class CreateCategoryRequest(
    val name: String,
    val description: String
)

data class VoteResponse(
    val status: String,
    val message: String
)