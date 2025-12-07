// CreatePostActivity.kt
package com.example.natal

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CreatePostActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var submitButton: Button
    private lateinit var cancelButton: Button
    private lateinit var backButton: ImageButton

    private var categories = listOf<Category>()
    private lateinit var username: String
    private lateinit var password: String

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://consciously-replete-ox.cloudpub.ru/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(CommunityApiService::class.java)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        // Получаем данные пользователя
        username = intent.getStringExtra("USERNAME") ?: ""
        password = intent.getStringExtra("PASSWORD") ?: ""

        // Инициализация
        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        submitButton = findViewById(R.id.submitButton)
        cancelButton = findViewById(R.id.cancelButton)
        backButton = findViewById(R.id.buttonBack)

        // Кнопка назад
        backButton.setOnClickListener { finish() }

        // Кнопка отмены
        cancelButton.setOnClickListener { finish() }

        // Кнопка отправки
        submitButton.setOnClickListener {
            createPost()
        }

        // Загрузка категорий
        loadCategories()
    }

    private fun loadCategories() {
        apiService.getCategories().enqueue(object : Callback<CommunityResponse<List<Category>>> {
            override fun onResponse(call: Call<CommunityResponse<List<Category>>>, response: Response<CommunityResponse<List<Category>>>) {
                if (response.isSuccessful && response.body()?.status == "True") {
                    categories = response.body()?.data ?: response.body()?.categories ?: emptyList()

                    if (categories.isNotEmpty()) {
                        val categoryNames = categories.map { it.name }
                        val adapter = ArrayAdapter(this@CreatePostActivity,
                            android.R.layout.simple_spinner_item, categoryNames)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        categorySpinner.adapter = adapter
                    } else {
                        Toast.makeText(this@CreatePostActivity, "Нет доступных категорий", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call<CommunityResponse<List<Category>>>, t: Throwable) {
                Toast.makeText(this@CreatePostActivity, "Ошибка загрузки категорий", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createPost() {
        val title = titleEditText.text.toString().trim()
        val content = contentEditText.text.toString().trim()
        val selectedCategoryPosition = categorySpinner.selectedItemPosition

        if (title.isEmpty()) {
            Toast.makeText(this, "Введите заголовок", Toast.LENGTH_SHORT).show()
            return
        }

        if (content.isEmpty()) {
            Toast.makeText(this, "Введите содержание", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCategoryPosition == -1 || categories.isEmpty()) {
            Toast.makeText(this, "Выберите категорию", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCategory = categories[selectedCategoryPosition]

        // Создаем запрос
        val request = CreatePostRequest(
            title = title,
            content = content,
            category_id = selectedCategory.id,
            login = username,
            password = password
        )

        // Отправляем запрос
        apiService.createPost(request).enqueue(object : Callback<CommunityResponse<Int>> {
            override fun onResponse(call: Call<CommunityResponse<Int>>, response: Response<CommunityResponse<Int>>) {
                if (response.isSuccessful && response.body()?.status == "True") {
                    Toast.makeText(this@CreatePostActivity, "Пост создан успешно!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val errorMessage = response.body()?.message ?: "Ошибка создания поста"
                    Toast.makeText(this@CreatePostActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CommunityResponse<Int>>, t: Throwable) {
                Toast.makeText(this@CreatePostActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}