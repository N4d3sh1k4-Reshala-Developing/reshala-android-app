package com.bignerdranch.android.reshalaalfa01.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.reshalaalfa01.data.AuthRepository
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
    object Idle : RecognitionState()
    object Capturing : RecognitionState()
    object Processing : RecognitionState()
    data class ReadyForFeedback(val taskData: RecognitionTaskData) : RecognitionState()
    data class Success(val taskData: RecognitionTaskData) : RecognitionState()
    data class Error(val message: String) : RecognitionState()
}

class RecognitionViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow<RecognitionState>(RecognitionState.Idle)
    val state: StateFlow<RecognitionState> = _state.asStateFlow()

    fun processCapturedImage(imageProxy: ImageProxy, cropRect: Rect) {
        viewModelScope.launch {
            _state.value = RecognitionState.Processing
            
            val bitmap = imageProxyToBitmap(imageProxy)
            val croppedBitmap = cropBitmap(bitmap, cropRect, imageProxy.width, imageProxy.height)
            imageProxy.close()
            
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

    fun startFeedback(task: com.bignerdranch.android.reshalaalfa01.data.local.RecognitionEntity) {
        _state.value = RecognitionState.ReadyForFeedback(
            RecognitionTaskData(
                id = task.id,
                createdAt = task.createdAt,
                status = task.status,
                originalResult = task.originalResult,
                editedResult = task.editedResult
            )
        )
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        
        // Handle rotation if necessary (CameraX usually handles this in Preview but not always in capture)
        val matrix = Matrix()
        matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun cropBitmap(bitmap: Bitmap, rect: Rect, previewWidth: Int, previewHeight: Int): Bitmap {
        val scaleX = bitmap.width.toFloat() / previewWidth
        val scaleY = bitmap.height.toFloat() / previewHeight
        
        val left = (rect.left * scaleX).toInt().coerceIn(0, bitmap.width - 1)
        val top = (rect.top * scaleY).toInt().coerceIn(0, bitmap.height - 1)
        val width = (rect.width * scaleX).toInt().coerceIn(1, bitmap.width - left)
        val height = (rect.height * scaleY).toInt().coerceIn(1, bitmap.height - top)
        
        return Bitmap.createBitmap(bitmap, left, top, width, height)
    }
}
