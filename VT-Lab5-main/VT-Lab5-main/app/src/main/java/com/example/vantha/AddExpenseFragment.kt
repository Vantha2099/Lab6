package com.example.vantha

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.vantha.api.ExpenseRequest
import com.example.vantha.api.RetrofitClient
import com.example.vantha.api.getCurrentDateISO8601
import com.example.vantha.database.AppDatabase
import com.example.vantha.databinding.FragmentAddExpenseBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private var selectedDate: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private lateinit var auth: FirebaseAuth
    private lateinit var database: AppDatabase
    private val executorService = Executors.newSingleThreadExecutor()

    private val newCategoryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Reload categories when coming back from NewCategoryActivity
            loadCategories()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = AppDatabase.getDatabase(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDatePicker()
        setupAddCategoryButton()
        setupSaveButton()
        loadCategories()
    }

    private fun setupDatePicker() {
        binding.inputDate.setText(dateFormat.format(selectedDate.time))

        binding.inputDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
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
            val intent = Intent(requireContext(), NewCategoryActivity::class.java)
            newCategoryLauncher.launch(intent)
        }
    }

    private fun loadCategories() {
        executorService.execute {
            try {
                // Get default categories
                val defaultCategories = resources.getStringArray(R.array.expense_categories).toMutableList()

                // Get custom categories from com.example.vantha.database
                val customCategories = database.categoryDao().getAllCategories()

                // Combine both lists
                val allCategories = mutableListOf<String>()
                allCategories.addAll(defaultCategories)
                allCategories.addAll(customCategories.map { it.name })

                requireActivity().runOnUiThread {
                    if (_binding != null) {
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            allCategories
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerCategory.adapter = adapter
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupSaveButton() {
        binding.buttonSaveExpense.setOnClickListener {
            if (validateInput()) {
                saveExpense()
            }
        }
    }

    private fun validateInput(): Boolean {
        val amountText = binding.editTextAmount.text.toString().trim()

        if (amountText.isEmpty()) {
            binding.editTextAmount.error = getString(R.string.error_empty_amount)
            binding.editTextAmount.requestFocus()
            return false
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.editTextAmount.error = getString(R.string.error_invalid_amount)
            binding.editTextAmount.requestFocus()
            return false
        }

        return true
    }

    private fun saveExpense() {
        showLoading(true)

        val amount = binding.editTextAmount.text.toString().toDouble()
        val currency = binding.spinnerCurrency.selectedItem.toString()
        val category = binding.spinnerCategory.selectedItem.toString()
        val remark = binding.editTextDescription.text.toString().trim()

        val userId = auth.currentUser?.uid ?: ""

        val expenseRequest = ExpenseRequest(
            id = UUID.randomUUID().toString(),
            amount = amount,
            currency = currency,
            category = category,
            remark = remark,
            createdBy = userId,
            createdDate = getCurrentDateISO8601()
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.expenseApiService.createExpense(
                    dbName = RetrofitClient.DB_NAME,
                    expense = expenseRequest
                )

                showLoading(false)

                if (response.isSuccessful) {
                    clearForm()

                    // Navigate to list immediately
                    (activity as? MainActivity)?.binding?.bottomNavigation?.selectedItemId = R.id.nav_expense_list

                    Toast.makeText(
                        requireContext(),
                        "Expense saved successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Refresh list
                    lifecycleScope.launch {
                        val delays = listOf(200L, 200L, 200L, 200L, 400L, 400L)

                        delays.forEach { delayMs ->
                            delay(delayMs)

                            (activity as? MainActivity)?.let { mainActivity ->
                                mainActivity.supportFragmentManager.fragments.forEach { fragment ->
                                    if (fragment is ExpenseListFragment && fragment.isAdded) {
                                        fragment.refreshList()
                                    }
                                    if (fragment is HomeFragment && fragment.isAdded) {
                                        fragment.loadLastExpense()
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to save expense: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.buttonSaveExpense.isEnabled = !show
        binding.buttonSaveExpense.text = if (show) "Saving..." else getString(R.string.button_add_expense)
    }

    private fun clearForm() {
        binding.editTextAmount.text?.clear()
        binding.editTextDescription.text?.clear()
        binding.spinnerCurrency.setSelection(0)
        binding.spinnerCategory.setSelection(0)
        selectedDate = Calendar.getInstance()
        binding.inputDate.setText(dateFormat.format(selectedDate.time))
    }

    override fun onResume() {
        super.onResume()
        // Reload categories when fragment resumes
        loadCategories()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        executorService.shutdown()
    }
}