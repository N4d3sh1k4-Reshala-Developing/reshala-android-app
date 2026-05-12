package com.bignerdranch.android.reshalaalfa01.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.reshalaalfa01.BuildConfig
import com.bignerdranch.android.reshalaalfa01.data.UpdateRepository
import com.bignerdranch.android.reshalaalfa01.data.remote.dto.GitHubReleaseResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UpdateState {
    object Idle : UpdateState()
    data class UpdateAvailable(val release: GitHubReleaseResponse) : UpdateState()
}

class UpdateViewModel(private val repository: UpdateRepository) : ViewModel() {
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    fun checkForUpdates(owner: String, repo: String) {
        viewModelScope.launch {
            repository.getLatestRelease(owner, repo).onSuccess { release ->
                val currentVersion = BuildConfig.VERSION_NAME
                // Choose the string that looks more like a version (has more digits)
                val latestVersionStr = if ((release.name?.count { it.isDigit() } ?: 0) > release.tagName.count { it.isDigit() }) {
                    release.name ?: release.tagName
                } else {
                    release.tagName
                }

                android.util.Log.d("UpdateCheck", "Latest: $latestVersionStr, Current: $currentVersion")

                if (isNewerVersion(latestVersionStr, currentVersion)) {
                    _updateState.value = UpdateState.UpdateAvailable(release)
                }
            }
        }
    }

    private fun isNewerVersion(latestVersion: String, currentVersion: String): Boolean {
        return try {
            // Extracts numbers from strings like "alfa_0.5.1" -> [0, 5, 1]
            val latestNumbers = latestVersion.split(Regex("[^0-9]+")).filter { it.isNotEmpty() }.map { it.toInt() }
            val currentNumbers = currentVersion.split(Regex("[^0-9]+")).filter { it.isNotEmpty() }.map { it.toInt() }

            val maxLength = maxOf(latestNumbers.size, currentNumbers.size)
            for (i in 0 until maxLength) {
                val latest = latestNumbers.getOrNull(i) ?: 0
                val current = currentNumbers.getOrNull(i) ?: 0
                if (latest > current) return true
                if (latest < current) return false
            }
            false
        } catch (e: Exception) {
            latestVersion != currentVersion && latestVersion > currentVersion
        }
    }
}
