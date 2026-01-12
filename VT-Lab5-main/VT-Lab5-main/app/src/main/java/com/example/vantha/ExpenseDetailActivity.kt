package com.example.vantha

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.vantha.databinding.ActivityExpenseDetailBinding

class ExpenseDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExpenseDetailBinding

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.setLocale(newBase.getLanguageCode()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val amount = intent.getDoubleExtra("amount", 0.0)
        val currency = intent.getStringExtra("currency") ?: "USD"
        val description = intent.getStringExtra("description") ?: ""
        val date = intent.getStringExtra("date") ?: ""

        displayExpenseDetails(amount, currency, description, date)

        binding.buttonAddNewExpense.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.buttonBackToHome.setOnClickListener {
            finish()
        }
    }

    private fun displayExpenseDetails(amount: Double, currency: String, description: String, date: String) {
        val amountText = if (amount % 1.0 == 0.0) amount.toInt().toString() else amount.toString()

        binding.textDetailAmount.text = getString(R.string.detail_amount, amountText, currency)
        binding.textDetailCurrency.text = getString(R.string.detail_currency, currency)
        binding.textDetailCategory.text = getString(R.string.detail_category, description)
        binding.textDetailDate.text = getString(R.string.detail_date, date)
        binding.textDetailRemark.text = getString(R.string.detail_remark, description)
    }

    private fun Context.getLanguageCode(): String {
        val prefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(MainActivity.PREF_LANG_CODE, MainActivity.DEFAULT_LANG_CODE)
            ?: MainActivity.DEFAULT_LANG_CODE
    }
}