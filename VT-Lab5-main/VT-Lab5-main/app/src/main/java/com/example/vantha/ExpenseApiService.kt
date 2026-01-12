package com.example.vantha.api

import retrofit2.Response
import retrofit2.http.*

interface ExpenseApiService {

    // ✅ ADDED: Get all expenses (no user filter)
    @GET("expenses")
    suspend fun getAllExpenses(
        @Header("X-DB-NAME") dbName: String
    ): Response<List<ExpenseResponse>>

    // ✅ ADDED: Get expenses filtered by user
    @GET("expenses")
    suspend fun getExpenses(
        @Header("X-DB-NAME") dbName: String,
        @Query("createdBy") userId: String
    ): Response<List<ExpenseResponse>>

    @POST("expenses")
    suspend fun createExpense(
        @Header("X-DB-NAME") dbName: String,
        @Body expense: ExpenseRequest
    ): Response<ExpenseResponse>

    @GET("expenses/{id}")
    suspend fun getExpenseById(
        @Header("X-DB-NAME") dbName: String,
        @Path("id") id: String
    ): Response<ExpenseResponse>

    @DELETE("expenses/{id}")
    suspend fun deleteExpense(
        @Header("X-DB-NAME") dbName: String,
        @Path("id") id: String
    ): Response<Void>
}