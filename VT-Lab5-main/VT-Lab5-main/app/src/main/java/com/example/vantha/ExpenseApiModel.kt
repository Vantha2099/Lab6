package com.example.vantha.api

import com.google.gson.annotations.SerializedName
import java.util.*

data class ExpenseRequest(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("currency")
    val currency: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("remark")
    val remark: String,

    @SerializedName("createdBy")
    val createdBy: String,

    @SerializedName("createdDate")
    val createdDate: String // ISO 8601 format
)

data class ExpenseResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("currency")
    val currency: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("remark")
    val remark: String?,

    @SerializedName("createdBy")
    val createdBy: String,

    @SerializedName("createdDate")
    val createdDate: String
)

data class PaginatedExpenseResponse(
    @SerializedName("data")
    val data: List<ExpenseResponse>,

    @SerializedName("total")
    val total: Int,

    @SerializedName("page")
    val page: Int,

    @SerializedName("pageSize")
    val pageSize: Int
)