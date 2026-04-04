package com.hichamdzz.translator.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hichamdzz.translator.model.Language
import com.hichamdzz.translator.model.Voice
import com.hichamdzz.translator.repository.UpdateRepository
import com.hichamdzz.translator.repository.VersionInfo
import com.hichamdzz.translator.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val updateRepository: UpdateRepository
) : ViewModel() {

    private val _onboardingDone = MutableStateFlow(false)
    val onboardingDone: StateFlow<Boolean> = _onboardingDone

    private val _updateRequired = MutableStateFlow<VersionInfo?>(null)
    val updateRequired: StateFlow<VersionInfo?> = _updateRequired

    private val _sourceLang = MutableStateFlow(Language.fromCode("ar"))
    val sourceLang: StateFlow<Language> = _sourceLang

    private val _targetLang = MutableStateFlow(Language.fromCode("en"))
    val targetLang: StateFlow<Language> = _targetLang

    private val _selectedVoice = MutableStateFlow(Voice.DEFAULT_VOICES[0])
    val selectedVoice: StateFlow<Voice> = _selectedVoice

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating

    private val _hearMyLanguage = MutableStateFlow(true)
    val hearMyLanguage: StateFlow<Boolean> = _hearMyLanguage

    private val _hearTheirLanguage = MutableStateFlow(true)
    val hearTheirLanguage: StateFlow<Boolean> = _hearTheirLanguage

    init {
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                _onboardingDone.value = prefs[booleanPreferencesKey(Constants.PREFS_ONBOARDING_DONE)] ?: false
                _sourceLang.value = Language.fromCode(prefs[stringPreferencesKey(Constants.PREFS_SOURCE_LANG)] ?: "ar")
                _targetLang.value = Language.fromCode(prefs[stringPreferencesKey(Constants.PREFS_TARGET_LANG)] ?: "en")
            }
        }
        checkForUpdate()
    }

    fun completeOnboarding() = viewModelScope.launch {
        dataStore.edit { it[booleanPreferencesKey(Constants.PREFS_ONBOARDING_DONE)] = true }
        _onboardingDone.value = true
    }

    fun setSourceLanguage(lang: Language) { _sourceLang.value = lang; saveLangPrefs() }
    fun setTargetLanguage(lang: Language) { _targetLang.value = lang; saveLangPrefs() }
    fun setSelectedVoice(voice: Voice) { _selectedVoice.value = voice }
    fun toggleTranslation() { _isTranslating.value = !_isTranslating.value }
    fun toggleHearMyLanguage() { _hearMyLanguage.value = !_hearMyLanguage.value }
    fun toggleHearTheirLanguage() { _hearTheirLanguage.value = !_hearTheirLanguage.value }

    private fun saveLangPrefs() = viewModelScope.launch {
        dataStore.edit {
            it[stringPreferencesKey(Constants.PREFS_SOURCE_LANG)] = _sourceLang.value.code
            it[stringPreferencesKey(Constants.PREFS_TARGET_LANG)] = _targetLang.value.code
        }
    }

    private fun checkForUpdate() = viewModelScope.launch {
        updateRepository.checkForUpdate().onSuccess { _updateRequired.value = it }
    }

    suspend fun getApiKey(key: String): String = dataStore.data.first()[stringPreferencesKey(key)] ?: ""
    fun saveApiKey(key: String, value: String) = viewModelScope.launch {
        dataStore.edit { it[stringPreferencesKey(key)] = value }
    }
}
