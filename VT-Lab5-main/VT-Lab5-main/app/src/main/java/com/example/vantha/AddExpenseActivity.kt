package com.example.vantha

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.vantha.databinding.ActivityAddExpenseBinding
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpenseBinding
    private var selectedDate: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.setLocale(newBase.getLanguageCode()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDatePicker()
        setupAddCategoryButton()
        setupAddExpenseButton()
    }

    private fun setupDatePicker() {
        binding.inputDate.setText(dateFormat.format(selectedDate.time))

        binding.inputDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedDate.set(year, month, dayOfMonth)
                    binding.inputDate.setText(dateFormat.format(selectedDate.time))
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    private fun setupAddCategoryButton() {
        binding.buttonAddCategory.setOnClickListener {
            val input = android.widget.EditText(this)
            input.hint = getString(R.string.button_add_category)

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.button_add_category))
                .setView(input)
                .setPositiveButton("Add") { _, _ ->
                    val newCategory = input.text.toString().trim()
                    if (newCategory.isNotEmpty()) {
                        addCategoryToSpinner(newCategory)
                        Toast.makeText(this, "Category added: $newCategory", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun addCategoryToSpinner(newCategory: String) {
        val currentCategories = resources.getStringArray(R.array.expense_categories).toMutableList()
        currentCategories.add(newCategory)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currentCategories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
        binding.spinnerCategory.setSelection(currentCategories.size - 1)
    }

    private fun setupAddExpenseButton() {
        binding.buttonAdd.setOnClickListener {
            if (validateInput()) {
                saveExpense()
            }
        }
    }

    private fun validateInput(): Boolean {
        val amountText = binding.inputAmount.text.toString().trim()

        if (amountText.isEmpty()) {
            binding.inputAmount.error = getString(R.string.error_empty_amount)
            binding.inputAmount.requestFocus()
            return false
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.inputAmount.error = getString(R.string.error_invalid_amount)
            binding.inputAmount.requestFocus()
            return false
        }

        return true
    }

    private fun saveExpense() {
        val amount = binding.inputAmount.text.toString().toDouble()
        val currency = binding.spinnerCurrency.selectedItem.toString()
        val category = binding.spinnerCategory.selectedItem.toString()
        val date = binding.inputDate.text.toString()
        val remark = binding.inputRemark.text.toString().trim()

        val description = if (remark.isEmpty()) category else remark

        val resultIntent = Intent()
        resultIntent.putExtra("amount", amount)
        resultIntent.putExtra("currency", currency)
        resultIntent.putExtra("description", description)
        resultIntent.putExtra("date", date)

        setResult(RESULT_OK, resultIntent)
        finish()
    }
}