package com.example.vantha

object ExpenseRepository {
    private val expenses = mutableListOf<Expense>()

    fun addExpense(expense: Expense) {
        expenses.add(0, expense)
    }

    fun getAllExpenses(): List<Expense> {
        return expenses.toList()
    }

    fun getLastExpense(): Expense? {
        return expenses.firstOrNull()
    }

    fun clearAll() {
        expenses.clear()
    }
}