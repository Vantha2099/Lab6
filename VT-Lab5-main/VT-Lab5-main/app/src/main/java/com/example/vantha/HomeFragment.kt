package com.example.vantha

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.vantha.api.ExpenseResponse
import com.example.vantha.api.RetrofitClient
import com.example.vantha.databinding.FragmentHomeBinding
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var lastExpense: ExpenseResponse? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        // Initialize UI
        binding.formTitle.text = getString(R.string.title_home, getString(R.string.my_name))
        setupLanguageSwitchButtons()
        setupViewDetailButton()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadLastExpense()
    }

    override fun onResume() {
        super.onResume()
        loadLastExpense()
    }

    // ✅ MADE PUBLIC so AddExpenseFragment can call it
    fun loadLastExpense() {
        if (!isAdded || _binding == null) return

        val currentUserId = auth.currentUser?.uid ?: ""

        lifecycleScope.launch {
            try {
                // ✅ FIXED: Filter by current user
                val response = RetrofitClient.expenseApiService.getExpenses(
                    dbName = RetrofitClient.DB_NAME,
                    userId = currentUserId
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    val expenses = response.body()
                    if (!expenses.isNullOrEmpty()) {
                        // Sort by date (most recent first)
                        val sortedExpenses = expenses.sortedByDescending { expense ->
                            try {
                                val dateFormat = SimpleDateFormat(
                                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                                    Locale.US
                                )
                                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                                dateFormat.parse(expense.createdDate)?.time ?: 0L
                            } catch (e: Exception) {
                                0L
                            }
                        }

                        lastExpense = sortedExpenses.first()
                        displayLastExpense()
                    } else {
                        showNoExpenses()
                    }
                } else {
                    showNoExpenses()
                }
            } catch (e: Exception) {
                if (!isAdded || _binding == null) return@launch
                showNoExpenses()
                e.printStackTrace()
            }
        }
    }

    private fun displayLastExpense() {
        if (_binding == null) return

        lastExpense?.let { expense ->
            val amountText = if (expense.amount % 1.0 == 0.0) {
                expense.amount.toInt().toString()
            } else {
                String.format("%.2f", expense.amount)
            }

            val description = if (!expense.remark.isNullOrBlank()) {
                expense.remark
            } else {
                expense.category
            }

            binding.textLastExpense.text =
                "Your last expense was $amountText ${expense.currency} for $description"
            binding.buttonViewDetail.isEnabled = true
        }
    }

    private fun showNoExpenses() {
        if (_binding == null) return

        binding.textLastExpense.text = getString(R.string.summary_initial)
        binding.buttonViewDetail.isEnabled = false
        lastExpense = null
    }

    private fun setupViewDetailButton() {
        binding.buttonViewDetail.setOnClickListener {
            lastExpense?.let { expense ->
                val intent = Intent(context, ExpenseDetailActivity::class.java)
                intent.putExtra("expense_id", expense.id)
                intent.putExtra("amount", expense.amount)
                intent.putExtra("currency", expense.currency)
                intent.putExtra("description", expense.remark ?: expense.category)
                intent.putExtra("date", expense.createdDate)
                intent.putExtra("category", expense.category)
                startActivity(intent)
            }
        }
    }

    private fun setupLanguageSwitchButtons() {
        binding.btnSwitchLangKm.setOnClickListener {
            (activity as? MainActivity)?.switchLanguage("km")
        }

        binding.btnSwitchLangEn.setOnClickListener {
            (activity as? MainActivity)?.switchLanguage("en")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}