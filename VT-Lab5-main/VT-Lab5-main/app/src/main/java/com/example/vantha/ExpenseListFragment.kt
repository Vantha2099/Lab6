package com.example.vantha

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vantha.api.ExpenseResponse
import com.example.vantha.api.RetrofitClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExpenseListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var totalExpenses: TextView
    private lateinit var adapter: ExpenseApiAdapter
    private lateinit var auth: FirebaseAuth

    private val expenses = mutableListOf<ExpenseResponse>()
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expense_list, container, false)

        auth = FirebaseAuth.getInstance()

        recyclerView = view.findViewById(R.id.recyclerViewExpenses)
        emptyState = view.findViewById(R.id.emptyState)
        totalExpenses = view.findViewById(R.id.totalExpenses)

        setupRecyclerView()
        setupSwipeToDelete()
        loadExpenses()

        return view
    }

    private fun setupRecyclerView() {
        adapter = ExpenseApiAdapter(expenses) { expense ->
            openExpenseDetail(expense)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val expense = expenses[position]
                deleteExpense(expense, position)
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun loadExpenses() {
        if (isLoading) return

        isLoading = true

        val currentUserId = auth.currentUser?.uid ?: ""

        lifecycleScope.launch {
            try {
                // ✅ FIXED: Pass userId to filter expenses
                val response = RetrofitClient.expenseApiService.getExpenses(
                    dbName = RetrofitClient.DB_NAME,
                    userId = currentUserId
                )

                isLoading = false

                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        expenses.clear()

                        val sortedData = data.sortedByDescending { expense ->
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

                        expenses.addAll(sortedData)
                        adapter.notifyDataSetChanged()
                        updateUI()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load expenses: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }
    }

    private fun deleteExpense(expense: ExpenseResponse, position: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.expenseApiService.deleteExpense(
                    dbName = RetrofitClient.DB_NAME,
                    id = expense.id
                )

                if (response.isSuccessful) {
                    expenses.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    updateUI()

                    Snackbar.make(
                        recyclerView,
                        "Expense deleted",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    // Refresh home fragment to update last expense
                    (activity as? MainActivity)?.supportFragmentManager?.fragments?.forEach { fragment ->
                        if (fragment is HomeFragment && fragment.isVisible) {
                            fragment.loadLastExpense()
                        }
                    }
                } else {
                    adapter.notifyItemChanged(position)
                    Toast.makeText(
                        requireContext(),
                        "Failed to delete expense",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                adapter.notifyItemChanged(position)
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }
    }

    private fun updateUI() {
        if (expenses.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            totalExpenses.text = getString(R.string.total_expenses, 0)
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            totalExpenses.text = getString(R.string.total_expenses, expenses.size)
        }
    }

    private fun openExpenseDetail(expense: ExpenseResponse) {
        val intent = Intent(context, ExpenseDetailActivity::class.java)
        intent.putExtra("expense_id", expense.id)
        intent.putExtra("amount", expense.amount)
        intent.putExtra("currency", expense.currency)
        intent.putExtra("description", expense.remark ?: expense.category)
        intent.putExtra("date", expense.createdDate)
        intent.putExtra("category", expense.category)
        startActivity(intent)
    }

    fun refreshList() {
        loadExpenses()
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
    }
}