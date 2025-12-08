// CommunityActivity.kt
package com.example.natal

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CommunityActivity : AppCompatActivity() {

    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var categoriesSpinner: Spinner
    private lateinit var sortSpinner: Spinner
    private lateinit var createPostButton: Button
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var backButton: ImageButton

    private lateinit var postsAdapter: PostsAdapter
    private var currentCategoryId: Int? = null
    private var currentSort = "new"
    private var categories = listOf<Category>()
    private var currentPage = 1
    private var totalPages = 1
    private var isLoading = false


    private lateinit var currentUsername: String
    private lateinit var currentPassword: String

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://consciously-replete-ox.cloudpub.ru/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(CommunityApiService::class.java)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)


        currentUsername = intent.getStringExtra("EXTRA_MESSAGE1") ?: ""
        currentPassword = intent.getStringExtra("EXTRA_MESSAGE2") ?: ""


        initViews()
        setupRecyclerView()
        setupClickListeners()


        loadCategories()
        loadPosts()
    }

    private fun initViews() {
        postsRecyclerView = findViewById(R.id.postsRecyclerView)
        categoriesSpinner = findViewById(R.id.categoriesSpinner)
        sortSpinner = findViewById(R.id.sortSpinner)
        createPostButton = findViewById(R.id.createPostButton)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        backButton = findViewById(R.id.buttonBack)


        val sortOptions = arrayOf("Сначала новые", "Популярные", "Лучшие")
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = sortAdapter
    }

    private fun setupRecyclerView() {

        postsRecyclerView.layoutManager = LinearLayoutManager(this)
        postsRecyclerView.itemAnimator = null
        postsRecyclerView.setHasFixedSize(true)


        postsRecyclerView.isNestedScrollingEnabled = true

        postsAdapter = PostsAdapter(
            emptyList(),
            currentUsername,
            currentPassword,
            apiService,
            onItemClick = { post ->
                openPostDetail(post.id)
            },
            onVoteSuccess = {

            }
        )
        postsRecyclerView.adapter = postsAdapter


        postsRecyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && currentPage < totalPages) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                        loadMorePosts()
                    }
                }
            }
        })
    }
    private fun setupClickListeners() {
        backButton.setOnClickListener { finish() }

        createPostButton.setOnClickListener {
            if (currentUsername.isEmpty() || currentPassword.isEmpty()) {
                Toast.makeText(this, "Требуется авторизация", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, CreatePostActivity::class.java)
            intent.putExtra("USERNAME", currentUsername)
            intent.putExtra("PASSWORD", currentPassword)
            startActivityForResult(intent, CREATE_POST_REQUEST)
        }

        swipeRefreshLayout.setOnRefreshListener {
            currentPage = 1
            loadCategories()
            loadPosts()
        }

        categoriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (position == 0) {
                    currentCategoryId = null
                } else {
                    currentCategoryId = categories[position - 1].id
                }
                currentPage = 1
                loadPosts()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                currentSort = when (position) {
                    0 -> "new"
                    1 -> "hot"
                    2 -> "top"
                    else -> "new"
                }
                currentPage = 1
                loadPosts()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadCategories() {
        apiService.getCategories().enqueue(object : Callback<CommunityResponse<List<Category>>> {
            override fun onResponse(call: Call<CommunityResponse<List<Category>>>, response: Response<CommunityResponse<List<Category>>>) {
                if (response.isSuccessful && response.body()?.status == "True") {
                    categories = response.body()?.data ?: response.body()?.categories ?: emptyList()


                    // Обновляем спиннер
                    val categoryNames = mutableListOf("Все категории")
                    categoryNames.addAll(categories.map { it.name })

                    val adapter = ArrayAdapter(this@CommunityActivity,
                        android.R.layout.simple_spinner_item, categoryNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    categoriesSpinner.adapter = adapter
                }
            }
            override fun onFailure(call: Call<CommunityResponse<List<Category>>>, t: Throwable) {
                Toast.makeText(this@CommunityActivity, "Ошибка загрузки категорий", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadPosts() {
        isLoading = true
        swipeRefreshLayout.isRefreshing = true

        apiService.getPosts(currentCategoryId, currentSort, currentPage).enqueue(object : Callback<CommunityResponse<List<Post>>> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<CommunityResponse<List<Post>>>, response: Response<CommunityResponse<List<Post>>>) {
                isLoading = false
                swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body?.status == "True") {
                        val posts = body.posts ?: body.data ?: emptyList()
                        totalPages = body.pagination?.total_pages ?: 1

                        if (currentPage == 1) {
                            postsAdapter.updatePosts(posts)
                        } else {
                            postsAdapter.addPosts(posts)
                        }

                        if (posts.isEmpty() && currentPage == 1) {
                            Toast.makeText(this@CommunityActivity, "Нет постов в выбранной категории", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@CommunityActivity, body?.message ?: "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@CommunityActivity, "Ошибка сервера", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CommunityResponse<List<Post>>>, t: Throwable) {
                isLoading = false
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(this@CommunityActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadMorePosts() {
        if (isLoading || currentPage >= totalPages) return

        currentPage++
        isLoading = true

        apiService.getPosts(currentCategoryId, currentSort, currentPage).enqueue(object : Callback<CommunityResponse<List<Post>>> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<CommunityResponse<List<Post>>>, response: Response<CommunityResponse<List<Post>>>) {
                isLoading = false

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body?.status == "True") {
                        val posts = body.posts ?: body.data ?: emptyList()
                        postsAdapter.addPosts(posts)
                    }
                }
            }

            override fun onFailure(call: Call<CommunityResponse<List<Post>>>, t: Throwable) {
                isLoading = false
                currentPage-- // Откатываем страницу при ошибке
            }
        })
    }

    private fun openPostDetail(postId: Int) {
        val intent = Intent(this, PostDetailActivity::class.java)
        intent.putExtra("POST_ID", postId)
        intent.putExtra("USERNAME", currentUsername)
        intent.putExtra("PASSWORD", currentPassword)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CREATE_POST_REQUEST && resultCode == RESULT_OK) {
            // Обновляем список после создания поста
            currentPage = 1
            loadPosts()
        }
    }

    companion object {
        private const val CREATE_POST_REQUEST = 1001
    }
}