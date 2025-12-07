// CommentsAdapter.kt
package com.example.natal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class CommentsAdapter(
    private var comments: List<Comment>,
    private val currentUsername: String
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val author: TextView = itemView.findViewById(R.id.commentAuthor)
        val content: TextView = itemView.findViewById(R.id.commentContent)
        val time: TextView = itemView.findViewById(R.id.commentTime)
        val upvotes: TextView = itemView.findViewById(R.id.commentUpvotes)
        val downvotes: TextView = itemView.findViewById(R.id.commentDownvotes)
        val upvoteButton: ImageButton = itemView.findViewById(R.id.commentUpvoteButton)
        val downvoteButton: ImageButton = itemView.findViewById(R.id.commentDownvoteButton)
        val replyButton: Button = itemView.findViewById(R.id.replyButton)
        val repliesContainer: LinearLayout = itemView.findViewById(R.id.repliesContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        holder.author.text = comment.authorName
        holder.content.text = comment.content
        holder.time.text = formatTime(comment.createdAt)
        holder.upvotes.text = comment.upvotes.toString()
        holder.downvotes.text = comment.downvotes.toString()

        // Проверяем, является ли текущий пользователь автором
        val isOwner = comment.authorName == currentUsername

        // Подсветка голосов
        updateVoteButtons(holder, comment.userVote)

        // Обработчики кликов
        holder.upvoteButton.setOnClickListener {
            // TODO: Реализовать голосование за комментарий
            updateVoteButtons(holder, 1)
        }

        holder.downvoteButton.setOnClickListener {
            // TODO: Реализовать голосование за комментарий
            updateVoteButtons(holder, -1)
        }

        holder.replyButton.setOnClickListener {
            // TODO: Реализовать ответ на комментарий
            Toast.makeText(holder.itemView.context, "Ответить на комментарий", Toast.LENGTH_SHORT).show()
        }

        // Отображаем ответы (рекурсивно)
        displayReplies(holder.repliesContainer, comment.replies)
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
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

    private fun updateVoteButtons(holder: CommentViewHolder, voteType: Int) {
        val context = holder.itemView.context

        when (voteType) {
            1 -> {
                // Upvote активен
                holder.upvoteButton.setColorFilter(ContextCompat.getColor(context, R.color.upvote_active))
                holder.downvoteButton.setColorFilter(ContextCompat.getColor(context, R.color.vote_inactive))
            }
            -1 -> {
                // Downvote активен
                holder.upvoteButton.setColorFilter(ContextCompat.getColor(context, R.color.vote_inactive))
                holder.downvoteButton.setColorFilter(ContextCompat.getColor(context, R.color.downvote_active))
            }
            else -> {
                // Нет голоса
                holder.upvoteButton.setColorFilter(ContextCompat.getColor(context, R.color.vote_inactive))
                holder.downvoteButton.setColorFilter(ContextCompat.getColor(context, R.color.vote_inactive))
            }
        }
    }

    private fun displayReplies(container: LinearLayout, replies: List<Comment>) {
        // Очищаем контейнер
        container.removeAllViews()

        if (replies.isEmpty()) {
            container.visibility = View.GONE
            return
        }

        container.visibility = View.VISIBLE

        // Добавляем каждый ответ
        for (reply in replies) {
            val replyView = LayoutInflater.from(container.context)
                .inflate(R.layout.item_comment, container, false)

            // Находим элементы
            val author: TextView = replyView.findViewById(R.id.commentAuthor)
            val content: TextView = replyView.findViewById(R.id.commentContent)
            val time: TextView = replyView.findViewById(R.id.commentTime)
            val upvotes: TextView = replyView.findViewById(R.id.commentUpvotes)
            val downvotes: TextView = replyView.findViewById(R.id.commentDownvotes)
            val upvoteButton: ImageButton = replyView.findViewById(R.id.commentUpvoteButton)
            val downvoteButton: ImageButton = replyView.findViewById(R.id.commentDownvoteButton)
            val replyButton: Button = replyView.findViewById(R.id.replyButton)
            val repliesContainer: LinearLayout = replyView.findViewById(R.id.repliesContainer)

            // Заполняем данные
            author.text = reply.authorName
            content.text = reply.content
            time.text = formatTime(reply.createdAt)
            upvotes.text = reply.upvotes.toString()
            downvotes.text = reply.downvotes.toString()

            // Проверяем, является ли текущий пользователь автором
            val isOwner = reply.authorName == currentUsername

            // Подсветка голосов
            when (reply.userVote) {
                1 -> {
                    upvoteButton.setColorFilter(ContextCompat.getColor(container.context, R.color.upvote_active))
                    downvoteButton.setColorFilter(ContextCompat.getColor(container.context, R.color.vote_inactive))
                }
                -1 -> {
                    upvoteButton.setColorFilter(ContextCompat.getColor(container.context, R.color.vote_inactive))
                    downvoteButton.setColorFilter(ContextCompat.getColor(container.context, R.color.downvote_active))
                }
                else -> {
                    upvoteButton.setColorFilter(ContextCompat.getColor(container.context, R.color.vote_inactive))
                    downvoteButton.setColorFilter(ContextCompat.getColor(container.context, R.color.vote_inactive))
                }
            }

            // Скрываем кнопку ответа для вложенных комментариев
            replyButton.visibility = View.GONE

            // Обработчики кликов
            upvoteButton.setOnClickListener {
                // TODO: Реализовать голосование за ответ
                updateVoteButtonsForView(replyView, 1)
            }

            downvoteButton.setOnClickListener {
                // TODO: Реализовать голосование за ответ
                updateVoteButtonsForView(replyView, -1)
            }

            // Рекурсивно отображаем вложенные ответы
            displayReplies(repliesContainer, reply.replies)

            // Добавляем в контейнер
            container.addView(replyView)
        }
    }

    private fun updateVoteButtonsForView(view: View, voteType: Int) {
        val upvoteButton: ImageButton = view.findViewById(R.id.commentUpvoteButton)
        val downvoteButton: ImageButton = view.findViewById(R.id.commentDownvoteButton)

        when (voteType) {
            1 -> {
                upvoteButton.setColorFilter(ContextCompat.getColor(view.context, R.color.upvote_active))
                downvoteButton.setColorFilter(ContextCompat.getColor(view.context, R.color.vote_inactive))
            }
            -1 -> {
                upvoteButton.setColorFilter(ContextCompat.getColor(view.context, R.color.vote_inactive))
                downvoteButton.setColorFilter(ContextCompat.getColor(view.context, R.color.downvote_active))
            }
            else -> {
                upvoteButton.setColorFilter(ContextCompat.getColor(view.context, R.color.vote_inactive))
                downvoteButton.setColorFilter(ContextCompat.getColor(view.context, R.color.vote_inactive))
            }
        }
    }
}