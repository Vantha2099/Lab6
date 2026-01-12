package com.example.vantha

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Expense(
    val id: String = "",
    val description: String,
    val amount: Double,
    val currency: String,
    val date: String,
    val category: String = "",
    val createdBy: String = ""
) : Parcelable, Serializable