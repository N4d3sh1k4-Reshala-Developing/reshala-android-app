package com.bignerdranch.android.reshalaalfa01.ui

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.reshalaalfa01.data.AuthRepository
import com.bignerdranch.android.reshalaalfa01.data.local.RecognitionEntity
import com.bignerdranch.android.reshalaalfa01.data.remote.dto.RecognitionTaskData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

sealed class RecognitionState {
    data object Idle : RecognitionState()
    data object Processing : RecognitionState()
    data class ReadyForFeedback(val taskData: RecognitionTaskData) : RecognitionState()
    data class Success(val taskData: RecognitionTaskData) : RecognitionState()
    data class Error(val message: String) : RecognitionState()
}

class RecognitionViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow<RecognitionState>(RecognitionState.Idle)
    val state: StateFlow<RecognitionState> = _state.asStateFlow()

    fun processBitmap(bitmap: Bitmap, cropRect: Rect, uiWidth: Int, uiHeight: Int) {
        viewModelScope.launch {
            _state.value = RecognitionState.Processing
            val croppedBitmap = cropBitmap(bitmap, cropRect, uiWidth, uiHeight)

            val stream = ByteArrayOutputStream()
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            val byteArray = stream.toByteArray()

            val requestFile = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", "equation.jpg", requestFile)

            repository.processImage(body).onSuccess { response ->
                if (response.success) {
                    startPolling(response.data.id)
                } else {
                    _state.value = RecognitionState.Error("Upload failed")
                }
            }.onFailure {
                _state.value = RecognitionState.Error(it.message ?: "Network error")
            }
        }
    }

    fun solveManual(equation: String) {
        viewModelScope.launch {
            _state.value = RecognitionState.Processing
            repository.solveManual(equation).onSuccess { response ->
                if (response.success) {
                    startPolling(response.data.id)
                } else {
                    _state.value = RecognitionState.Error("Solve request failed")
                }
            }.onFailure {
                _state.value = RecognitionState.Error(it.message ?: "Network error")
            }
        }
    }

    private fun startPolling(taskId: String) {
        viewModelScope.launch {
            var attempts = 0
            while (attempts < 30) { // Poll for 60 seconds
                repository.getRecognitionStatus(taskId).onSuccess { response ->
                    val data = response.data
                    when (data.status) {
                        "READY_FOR_FEEDBACK" -> {
                            _state.value = RecognitionState.ReadyForFeedback(data)
                            return@launch
                        }
                        "SOLUTION_READY", "COMPLETED_AUTO", "COMPLETED_EDITED" -> {
                            _state.value = RecognitionState.Success(data)
                            repository.fetchAndSaveHistory() // Refresh history
                            return@launch
                        }
                        "FAILED" -> {
                            _state.value = RecognitionState.Error("Recognition failed on server")
                            return@launch
                        }
                    }
                }.onFailure {
                    // Ignore transient errors and continue polling
                }
                delay(2000)
                attempts++
            }
            _state.value = RecognitionState.Error("Timeout waiting for result")
        }
    }

    fun sendFeedback(taskId: String, accepted: Boolean, editedResult: String? = null) {
        viewModelScope.launch {
            _state.value = RecognitionState.Processing
            val editStatus = if (accepted) "false" else "true"
            repository.sendFeedback(taskId, editStatus, editedResult).onSuccess {
                startPolling(taskId)
            }.onFailure {
                _state.value = RecognitionState.Error(it.message ?: "Feedback failed")
            }
        }
    }

    fun finishSuccess() {
        _state.value = RecognitionState.Idle
    }

    fun reset() {
        _state.value = RecognitionState.Idle
    }

    fun startFeedback(task: RecognitionEntity) {
        _state.value = RecognitionState.ReadyForFeedback(
            RecognitionTaskData(
                id = task.id,
                createdAt = task.createdAt,
                status = task.status,
                originalResult = task.originalResult,
                editedResult = task.editedResult,
                solutionResult = task.solutionResult
            )
        )
    }

    private fun cropBitmap(bitmap: Bitmap, rect: Rect, uiWidth: Int, uiHeight: Int): Bitmap {
        // Вычисляем масштаб ContentScale.Fit
        val scale = minOf(uiWidth.toFloat() / bitmap.width, uiHeight.toFloat() / bitmap.height)
        
        // Вычисляем реальные размеры и отступы изображения на экране
        val actualDisplayedWidth = bitmap.width * scale
        val actualDisplayedHeight = bitmap.height * scale
        val offsetX = (uiWidth - actualDisplayedWidth) / 2
        val offsetY = (uiHeight - actualDisplayedHeight) / 2

        // Переводим координаты рамки в систему координат исходного Bitmap
        val left = ((rect.left - offsetX) / scale).toInt().coerceIn(0, bitmap.width - 1)
        val top = ((rect.top - offsetY) / scale).toInt().coerceIn(0, bitmap.height - 1)
        val right = ((rect.right - offsetX) / scale).toInt().coerceIn(left + 1, bitmap.width)
        val bottom = ((rect.bottom - offsetY) / scale).toInt().coerceIn(top + 1, bitmap.height)

        val width = right - left
        val height = bottom - top
        
        return Bitmap.createBitmap(bitmap, left, top, width, height)
    }
}
