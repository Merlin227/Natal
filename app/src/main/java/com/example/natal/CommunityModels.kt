package com.example.natal
import com.google.gson.annotations.SerializedName
import java.util.*

data class Category(
    val id: Int,
    val name: String,
    val description: String,
    val subscribers: Int,
    val isSubscribed: Boolean = false
)

data class Post(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("category_id")
    val categoryId: Int,

    @SerializedName("author_name")
    val authorName: String,

    @SerializedName("category_name")
    val categoryName: String,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("updated_at")
    val updatedAt: String?,

    @SerializedName("upvotes")
    val upvotes: Int,

    @SerializedName("downvotes")
    val downvotes: Int,

    @SerializedName("comment_count")
    val commentCount: Int,

    @SerializedName("is_approved")
    val isApproved: Boolean?,


    val userVote: Int = 0,
    val isOwner: Boolean = false
)

data class Comment(
    val id: Int,
    val postId: Int,
    val authorName: String,
    val content: String,
    val createdAt: String?,
    val upvotes: Int,
    val downvotes: Int,
    val parentCommentId: Int?,
    val replies: List<Comment> = emptyList(),
    val userVote: Int = 0,
    val isOwner: Boolean = false
)

// ОБНОВЛЕННЫЕ МОДЕЛИ ДЛЯ СОЗДАНИЯ
data class CreatePostRequest(
    val title: String,
    val content: String,
    val category_id: Int,
    val login: String,      // Добавлено
    val password: String    // Добавлено
)

data class CreateCommentRequest(
    val content: String,
    val post_id: Int,
    val parent_comment_id: Int? = null,
    val login: String,      // Добавлено
    val password: String    // Добавлено
)

data class VoteRequest(
    val vote_type: Int,
    val login: String,      // Добавлено
    val password: String    // Добавлено
)

data class SubscriptionRequest(
    val login: String,
    val password: String
)

data class CommunityResponse<T>(
    val status: String,
    val message: String,
    val data: T? = null,
    val categories: List<Category>? = null,    // Для /community/categories
    val posts: List<Post>? = null,            // Для /community/posts
    val post: Post? = null,                   // Для /community/posts/{id}
    val comments: List<Comment>? = null,      // Для /community/posts/{id}/comments
    val pagination: PaginationData? = null    // Для пагинации
)

data class PaginationData(
    val page: Int,
    val limit: Int,
    val total_posts: Int,
    val total_pages: Int
)