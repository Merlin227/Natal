// PostsAdapter.kt
package com.example.natal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class PostsAdapter(
    private var posts: List<Post>,
    private val currentUsername: String,
    private val currentPassword: String,
    private val apiService: CommunityApiService,
    private val onItemClick: (Post) -> Unit,
    private val onVoteSuccess: (() -> Unit)? = null
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.postTitle)
        val author: TextView = itemView.findViewById(R.id.postAuthor)
        val category: TextView = itemView.findViewById(R.id.postCategory)
        val content: TextView = itemView.findViewById(R.id.postContent)
        val upvotes: TextView = itemView.findViewById(R.id.upvotesCount)
        val downvotes: TextView = itemView.findViewById(R.id.downvotesCount)
        val comments: TextView = itemView.findViewById(R.id.commentsCount)
        val time: TextView = itemView.findViewById(R.id.postTime)
        // ИЗМЕНИТЬ: ImageView вместо ImageButton
        val upvoteButton: ImageView = itemView.findViewById(R.id.upvoteButton)
        val downvoteButton: ImageView = itemView.findViewById(R.id.downvoteButton)
        val commentButton: ImageView = itemView.findViewById(R.id.commentButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.title.text = post.title
        holder.author.text = "Автор: ${post.authorName}"
        holder.category.text = post.categoryName
        holder.content.text = post.content
        holder.upvotes.text = post.upvotes.toString()
        holder.downvotes.text = post.downvotes.toString()
        holder.comments.text = post.commentCount.toString()
        holder.time.text = formatTime(post.createdAt)

        // Подсветка текущего голоса пользователя
        updateVoteButtons(holder, post.userVote)

        // Обработчики кликов
        holder.itemView.setOnClickListener {
            onItemClick(post)
        }

        holder.upvoteButton.setOnClickListener {
            // У ImageView нет свойства isEnabled, используем isClickable
            it.isClickable = false
            it.postDelayed({ it.isClickable = true }, 1000)
            vote(post.id, 1, holder, position)
        }

        holder.downvoteButton.setOnClickListener {
            it.isClickable = false
            it.postDelayed({ it.isClickable = true }, 1000)
            vote(post.id, -1, holder, position)
        }

        holder.commentButton.setOnClickListener {
            onItemClick(post)
        }
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    fun addPosts(newPosts: List<Post>) {
        val oldSize = posts.size
        posts = posts + newPosts
        notifyItemRangeInserted(oldSize, newPosts.size)
    }

    private fun formatTime(timeString: String?): String {
        if (timeString == null) return ""

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(timeString)
            val now = Date()
            val diff = now.time - date.time

            when {
                diff < 60000 -> "только что"
                diff < 3600000 -> "${diff / 60000} мин назад"
                diff < 86400000 -> "${diff / 3600000} ч назад"
                else -> SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)
            }
        } catch (e: Exception) {
            timeString
        }
    }

    private fun updateVoteButtons(holder: PostViewHolder, voteType: Int) {
        val context = holder.itemView.context

        when (voteType) {
            1 -> {
                // Upvote активен
                holder.upvoteButton.setColorFilter(ContextCompat.getColor(context, R.color.upvote_active))
                holder.downvoteButton.setColorFilter(ContextCompat.getColor(context, R.color.vote_inactive))
                holder.upvoteButton.isClickable = false  // У ImageView используем isClickable
                holder.downvoteButton.isClickable = true
            }
            -1 -> {
                // Downvote активен
                holder.upvoteButton.setColorFilter(ContextCompat.getColor(context, R.color.vote_inactive))
                holder.downvoteButton.setColorFilter(ContextCompat.getColor(context, R.color.downvote_active))
                holder.upvoteButton.isClickable = true
                holder.downvoteButton.isClickable = false  // У ImageView используем isClickable
            }
            else -> {
                // Нет голоса
                holder.upvoteButton.setColorFilter(ContextCompat.getColor(context, R.color.vote_inactive))
                holder.downvoteButton.setColorFilter(ContextCompat.getColor(context, R.color.vote_inactive))
                holder.upvoteButton.isClickable = true
                holder.downvoteButton.isClickable = true
            }
        }
    }

    private fun vote(postId: Int, voteType: Int, holder: PostViewHolder, position: Int) {
        // Сохраняем текущие значения для отката
        val currentPost = posts[position]
        val oldVoteType = currentPost.userVote
        val oldUpvotes = currentPost.upvotes
        val oldDownvotes = currentPost.downvotes

        // Сразу показываем новое состояние
        updateVoteButtons(holder, voteType)

        // Рассчитываем новые значения
        var newUpvotes = oldUpvotes
        var newDownvotes = oldDownvotes

        // Убираем старый голос
        when (oldVoteType) {
            1 -> newUpvotes -= 1
            -1 -> newDownvotes -= 1
        }

        // Добавляем новый голос
        when (voteType) {
            1 -> newUpvotes += 1
            -1 -> newDownvotes += 1
        }

        // Обновляем UI сразу
        holder.upvotes.text = newUpvotes.toString()
        holder.downvotes.text = newDownvotes.toString()

        // Создаем объект запроса
        val voteRequest = VoteRequest(
            vote_type = voteType,
            login = currentUsername,
            password = currentPassword
        )

        // Отправляем запрос на сервер
        apiService.vote(
            postId = postId,
            request = voteRequest
        ).enqueue(object : Callback<CommunityResponse<VoteResponse>> {
            override fun onResponse(
                call: Call<CommunityResponse<VoteResponse>>,
                response: Response<CommunityResponse<VoteResponse>>
            ) {
                if (response.isSuccessful && response.body()?.status == "True") {
                    // Успешно проголосовали
                    val updatedPost = currentPost.copy(
                        upvotes = newUpvotes,
                        downvotes = newDownvotes,
                        userVote = voteType
                    )


                    val updatedPosts = posts.toMutableList()
                    updatedPosts[position] = updatedPost
                    posts = updatedPosts


                        onVoteSuccess?.invoke()

                } else {

                    updateVoteButtons(holder, oldVoteType)
                    holder.upvotes.text = oldUpvotes.toString()
                    holder.downvotes.text = oldDownvotes.toString()


                    holder.upvoteButton.isClickable = true
                    holder.downvoteButton.isClickable = true


                    val errorMsg = response.body()?.message ?: "Неизвестная ошибка"
                    Toast.makeText(holder.itemView.context,
                        "Ошибка голосования: $errorMsg",
                        Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CommunityResponse<VoteResponse>>, t: Throwable) {

                updateVoteButtons(holder, oldVoteType)
                holder.upvotes.text = oldUpvotes.toString()
                holder.downvotes.text = oldDownvotes.toString()


                holder.upvoteButton.isClickable = true
                holder.downvoteButton.isClickable = true

                Toast.makeText(holder.itemView.context,
                    "Ошибка сети: ${t.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }
}