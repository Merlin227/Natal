package com.example.natal

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private var users: List<AdminUser>, // Используем AdminUser
    private val onItemClick: (AdminUser) -> Unit // Используем AdminUser
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var selectedPosition = -1

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewLogin: TextView = itemView.findViewById(R.id.textViewLogin)
        val textViewCityId: TextView = itemView.findViewById(R.id.textViewCityId)
        val textViewBirthDate: TextView = itemView.findViewById(R.id.textViewBirthDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val user = users[position]

        holder.textViewLogin.text = "Логин: ${user.login}"
        holder.textViewCityId.text = "ID города: ${user.id_city}"
        holder.textViewBirthDate.text = "Дата рождения: ${user.date_birth}"

        holder.itemView.isSelected = selectedPosition == position

        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            onItemClick(user)
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<AdminUser>) {
        users = newUsers
        selectedPosition = -1
        notifyDataSetChanged()
    }

    fun getSelectedUser(): AdminUser? { // Возвращаем AdminUser
        return if (selectedPosition != -1) users[selectedPosition] else null
    }
}