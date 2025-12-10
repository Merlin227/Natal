// CommentsAdapter.kt
package com.example.natal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
            val repliesContainer: LinearLayout = replyView.findViewById(R.id.repliesContainer)

            // Заполняем данные
            author.text = reply.authorName
            content.text = reply.content
            time.text = formatTime(reply.createdAt)

            // Рекурсивно отображаем вложенные ответы
            displayReplies(repliesContainer, reply.replies)

            // Добавляем в контейнер
            container.addView(replyView)
        }
    }

}