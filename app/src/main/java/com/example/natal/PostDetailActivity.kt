// PostDetailActivity.kt
package com.example.natal

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PostDetailActivity : AppCompatActivity() {

    private lateinit var postTitle: TextView
    private lateinit var postAuthor: TextView
    private lateinit var postContent: TextView
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var addCommentButton: Button
    private lateinit var commentEditText: EditText
    private lateinit var backButton: ImageButton

    private lateinit var commentsAdapter: CommentsAdapter
    private var postId: Int = 0
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
        setContentView(R.layout.activity_post_detail)

        postId = intent.getIntExtra("POST_ID", 0)
        username = intent.getStringExtra("USERNAME") ?: ""
        password = intent.getStringExtra("PASSWORD") ?: ""


        initViews()
        setupRecyclerView()
        setupClickListeners()


        loadPost()
        loadComments()
    }

    private fun initViews() {
        postTitle = findViewById(R.id.postDetailTitle)
        postAuthor = findViewById(R.id.postDetailAuthor)
        postContent = findViewById(R.id.postDetailContent)
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView)
        addCommentButton = findViewById(R.id.addCommentButton)
        commentEditText = findViewById(R.id.commentEditText)
        backButton = findViewById(R.id.buttonBack)

        // Кнопка назад
        backButton.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentsAdapter = CommentsAdapter(emptyList(), username)
        commentsRecyclerView.adapter = commentsAdapter
    }

    private fun setupClickListeners() {
        addCommentButton.setOnClickListener {
            val commentText = commentEditText.text.toString().trim()
            if (commentText.isNotEmpty()) {
                addComment(commentText)
            } else {
                Toast.makeText(this, "Введите комментарий", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPost() {
        apiService.getPost(postId).enqueue(object : Callback<CommunityResponse<Post>> {
            override fun onResponse(call: Call<CommunityResponse<Post>>, response: Response<CommunityResponse<Post>>) {
                if (response.isSuccessful) {
                    val body = response.body()

                    if (body?.status == "True") {
                        val post = body.post ?: body.data
                        post?.let {
                            postTitle.text = it.title
                            postAuthor.text = "Автор: ${it.authorName}"
                            postContent.text = it.content
                        }
                    }
                }
            }
            override fun onFailure(call: Call<CommunityResponse<Post>>, t: Throwable) {
                Toast.makeText(this@PostDetailActivity, "Ошибка загрузки поста", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadComments() {
        apiService.getComments(postId).enqueue(object : Callback<CommunityResponse<List<Comment>>> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<CommunityResponse<List<Comment>>>, response: Response<CommunityResponse<List<Comment>>>) {
                if (response.isSuccessful) {
                    val body = response.body()

                    if (body?.status == "True") {
                        val comments = body.comments ?: body.data ?: emptyList()
                        commentsAdapter.updateComments(comments)
                    }
                }
            }
            override fun onFailure(call: Call<CommunityResponse<List<Comment>>>, t: Throwable) {
                Toast.makeText(this@PostDetailActivity, "Ошибка загрузки комментариев", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addComment(commentText: String) {
        val request = CreateCommentRequest(
            content = commentText,
            post_id = postId,
            login = username,
            password = password
        )

        apiService.createComment(request).enqueue(object : Callback<CommunityResponse<String>> {
            override fun onResponse(call: Call<CommunityResponse<String>>, response: Response<CommunityResponse<String>>) {
                if (response.isSuccessful && response.body()?.status == "True") {
                    commentEditText.text.clear()
                    loadComments()
                    Toast.makeText(this@PostDetailActivity, "Комментарий добавлен", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@PostDetailActivity, "Ошибка добавления", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<CommunityResponse<String>>, t: Throwable) {
                Toast.makeText(this@PostDetailActivity, "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        })
    }
}