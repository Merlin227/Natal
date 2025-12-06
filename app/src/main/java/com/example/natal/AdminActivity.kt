package com.example.natal

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

class AdminActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var textViewLoading: TextView
    private lateinit var buttonViewDetails: Button
    private lateinit var buttonDeleteUser: Button
    private lateinit var buttonRefresh: Button
    private lateinit var buttonBack: Button

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://consciously-replete-ox.cloudpub.ru/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(AdminApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // Инициализация
        initViews()
        setupRecyclerView()
        setupClickListeners()
        loadUsers()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewUsers)
        textViewLoading = findViewById(R.id.textViewLoading)
        buttonViewDetails = findViewById(R.id.buttonViewDetails)
        buttonDeleteUser = findViewById(R.id.buttonDeleteUser)
        buttonRefresh = findViewById(R.id.buttonRefresh)
        buttonBack = findViewById(R.id.buttonBack)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(emptyList()) { user ->
            buttonViewDetails.isEnabled = true
            buttonDeleteUser.isEnabled = true
            Toast.makeText(this, "Выбран: ${user.login}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = userAdapter
    }

    private fun setupClickListeners() {
        buttonViewDetails.setOnClickListener {
            val selectedUser = userAdapter.getSelectedUser()
            selectedUser?.let { showUserDetails(it) }
        }

        buttonDeleteUser.setOnClickListener {
            val selectedUser = userAdapter.getSelectedUser()
            selectedUser?.let { deleteUser(it) }
        }

        buttonRefresh.setOnClickListener {
            loadUsers()
        }

        buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun loadUsers() {
        showLoading(true)

        apiService.getAllUsers().enqueue(object : Callback<UsersResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<UsersResponse>, response: Response<UsersResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val usersResponse = response.body()!!

                    if (usersResponse.status == "True" && usersResponse.users.isNotEmpty()) {
                        userAdapter.updateUsers(usersResponse.users)
                        showLoading(false)
                        showSuccess("Загружено ${usersResponse.users.size} пользователей")
                    } else {
                        showLoading(true)
                        textViewLoading.text = usersResponse.message
                        showError(usersResponse.message)
                    }
                } else {
                    showLoading(true)
                    textViewLoading.text = "Ошибка сервера"
                    showError("Ошибка ответа сервера")
                }
            }

            override fun onFailure(call: Call<UsersResponse>, t: Throwable) {
                showLoading(true)
                textViewLoading.text = "Ошибка подключения"
                showError("Ошибка: ${t.message}")
                t.printStackTrace()
            }
        })
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            textViewLoading.visibility = TextView.VISIBLE
            recyclerView.visibility = RecyclerView.GONE
        } else {
            textViewLoading.visibility = TextView.GONE
            recyclerView.visibility = RecyclerView.VISIBLE
        }
        buttonViewDetails.isEnabled = false
        buttonDeleteUser.isEnabled = false
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showUserDetails(user: AdminUser) {
        val details = """
            ID: ${user.id_user}
            Логин: ${user.login}
            Пароль: ${user.pass}
            Город: ${user.id_city}
            Время рождения: ${user.time_birth}
            Дата рождения: ${user.date_birth}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Детальная информация")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun deleteUser(user: AdminUser) {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение удаления")
            .setMessage("Вы уверены, что хотите удалить пользователя ${user.login}?")
            .setPositiveButton("Удалить") { _, _ ->
                performDeleteUser(user)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun performDeleteUser(user: AdminUser) {
        showLoading(true)

        val deleteRequest = DeleteUserRequest(
            user_id = user.id_user,
            admin_login = "admin",
            admin_password = "admin"
        )

        apiService.deleteUser(deleteRequest).enqueue(object : Callback<DeleteResponse> {
            override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val deleteResponse = response.body()!!

                    if (deleteResponse.status == "True") {
                        Toast.makeText(
                            this@AdminActivity,
                            deleteResponse.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        // Обновляем список пользователей после удаления
                        loadUsers()
                    } else {
                        showLoading(false)
                        Toast.makeText(
                            this@AdminActivity,
                            "Ошибка: ${deleteResponse.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    showLoading(false)
                    Toast.makeText(
                        this@AdminActivity,
                        "Ошибка сервера при удалении",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(
                    this@AdminActivity,
                    "Ошибка подключения: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
                t.printStackTrace()
            }
        })
    }
}

// Интерфейс API
interface AdminApiService {
    @GET("get-all-users")
    fun getAllUsers(): Call<UsersResponse>

    @POST("delete-user")
    fun deleteUser(@Body deleteRequest: DeleteUserRequest): Call<DeleteResponse>
}

// Data классы
data class AdminUser(
    val id_user: Int,  // Изменено с id на id_user для соответствия серверу
    val login: String,
    val pass: String,
    val id_city: String,  // Оставлено как String, так как сервер возвращает название города
    val time_birth: String,
    val date_birth: String
)

data class UsersResponse(
    val status: String,
    val message: String,
    val users: List<AdminUser>
)

data class DeleteUserRequest(
    val user_id: Int,
    val admin_login: String = "admin",
    val admin_password: String = "admin"
)

data class DeleteResponse(
    val status: String,
    val message: String
)