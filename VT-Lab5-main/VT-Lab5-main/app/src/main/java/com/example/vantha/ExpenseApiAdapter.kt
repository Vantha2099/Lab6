package com.example.vantha

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vantha.api.ExpenseResponse
import java.text.SimpleDateFormat
import java.util.*

class ExpenseApiAdapter(
    private val expenses: List<ExpenseResponse>,
    private val onItemClick: (ExpenseResponse) -> Unit
) : RecyclerView.Adapter<ExpenseApiAdapter.ExpenseViewHolder>() {

    private val categoryColors = listOf(
        "#8B6F47",
        "#D97757",
        "#5FA777",
        "#6B9AC4",
        "#9B6B9E",
        "#C47C5C"
    )

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val description: TextView = view.findViewById(R.id.textDescription)
        val amount: TextView = view.findViewById(R.id.textAmount)
        val date: TextView = view.findViewById(R.id.textDate)
        val categoryIcon: View = view.findViewById(R.id.categoryIcon)
        val categoryInitial: TextView = view.findViewById(R.id.categoryInitial)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

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

        holder.description.text = description
        holder.amount.text = "$amountText ${expense.currency}"

        try {
            val date = isoFormat.parse(expense.createdDate)
            holder.date.text = date?.let { displayFormat.format(it) } ?: expense.createdDate
        } catch (e: Exception) {
            holder.date.text = expense.createdDate
        }

        val initial = description.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        holder.categoryInitial.text = initial

        val colorIndex = Math.abs(description.hashCode()) % categoryColors.size
        val color = categoryColors[colorIndex]

        holder.categoryIcon.setBackgroundResource(R.drawable.circle_background)
        try {
            holder.categoryIcon.background.setTint(Color.parseColor(color))
        } catch (e: Exception) {
            holder.categoryIcon.background.setTint(Color.parseColor("#8B6F47"))
        }

        holder.itemView.setOnClickListener {
            onItemClick(expense)
        }
    }

    override fun getItemCount() = expenses.size
}