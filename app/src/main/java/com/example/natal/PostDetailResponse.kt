package com.example.natal

import com.google.gson.annotations.SerializedName

data class PostDetailResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("post")
    val post: Post? = null,

    @SerializedName("data")
    val data: Post? = null
)

data class PostResponse(
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
    val authorName: String?,

    @SerializedName("category_name")
    val categoryName: String?,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("updated_at")
    val updatedAt: String?,

    @SerializedName("upvotes")
    val upvotes: Int?,

    @SerializedName("downvotes")
    val downvotes: Int?,

    @SerializedName("comment_count")
    val commentCount: Int?,

    // ИЗМЕНИТЬ: Принимаем Int
    @SerializedName("is_approved")
    val isApproved: Int?  // 0 или 1
) {
    // Также добавить helper-свойство
    val isApprovedBoolean: Boolean
        get() = isApproved == 1
}