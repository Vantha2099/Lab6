package com.example.vantha

import android.content.Context
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat

class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val languagePreference = findPreference<ListPreference>("language")
        languagePreference?.setOnPreferenceChangeListener { _, newValue ->
            val languageCode = newValue as String

            // Save the language using LocaleHelper
            requireContext().saveLanguageCode(languageCode)

            // Recreate activity to apply language change
            requireActivity().recreate()

            true
        }
    }
}