package com.example.vantha

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vantha.database.AppDatabase
import com.example.vantha.database.Category
import com.example.vantha.databinding.ActivityNewCategoryBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NewCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewCategoryBinding
    private lateinit var database: AppDatabase
    private lateinit var executorService: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        executorService = Executors.newSingleThreadExecutor()

        setupUI()
    }

    private fun setupUI() {
        binding.buttonAddCategory.setOnClickListener {
            val categoryName = binding.editTextCategoryName.text.toString().trim()

            if (categoryName.isEmpty()) {
                binding.editTextCategoryName.error = "Category name is required"
                binding.editTextCategoryName.requestFocus()
                return@setOnClickListener
            }

            saveCategory(categoryName)
        }

        binding.buttonCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveCategory(categoryName: String) {
        executorService.execute {
            try {
                val category = Category(name = categoryName)
                database.categoryDao().insert(category)

                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Category '$categoryName' added successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Error saving category: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executorService.shutdown()
    }
}