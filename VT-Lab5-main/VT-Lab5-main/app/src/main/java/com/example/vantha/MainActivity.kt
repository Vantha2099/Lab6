package com.example.vantha

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.vantha.auth.LoginActivity
import com.example.vantha.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    companion object {
        const val PREFS_NAME = "MonoromPrefs"
        const val PREF_LANG_CODE = "language_code"
        const val DEFAULT_LANG_CODE = "en"
    }

    lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.setLocale(newBase.getLanguageCode()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        setupBottomNavigation()

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_out -> {
                showSignOutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { _, _ ->
                signOut()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun signOut() {
        auth.signOut()
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_add_expense -> replaceFragment(AddExpenseFragment())
                R.id.nav_expense_list -> replaceFragment(ExpenseListFragment())
                R.id.nav_settings -> replaceFragment(SettingFragment())
                else -> false
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun switchLanguage(languageCode: String) {
        if (languageCode == this.getLanguageCode()) return
        this.saveLanguageCode(languageCode)
        recreate()
    }

    fun refreshExpenseList() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is ExpenseListFragment) {
            currentFragment.refreshList()
        }
    }
}