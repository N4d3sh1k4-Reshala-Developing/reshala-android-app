package com.bignerdranch.android.reshalaalfa01.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

@Composable
fun CameraScreen(
    onClose: () -> Unit,
    onCapture: (ImageProxy, Rect) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
    
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                preview.setSurfaceProvider(previewView.surfaceProvider)
            } catch (e: Exception) {
                Log.e("CameraScreen", "Use case binding failed", e)
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            ResizableSelectionFrame(
                onCaptureClick = { rect ->
                    imageCapture.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                onCapture(image, rect)
                            }
                            override fun onError(exception: ImageCaptureException) {
                                Log.e("CameraScreen", "Capture failed", exception)
                            }
                        }
                    )
                },
                onClose = onClose
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Camera permission is required")
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@Composable
fun ResizableSelectionFrame(
    onCaptureClick: (Rect) -> Unit,
    onClose: () -> Unit
) {
    val density = LocalDensity.current
    var frameOffset by remember { mutableStateOf(Offset(100f, 500f)) }
    var frameSize by remember { mutableStateOf(Size(600f, 400f)) }
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidth = constraints.maxWidth.toFloat()
        val maxHeight = constraints.maxHeight.toFloat()
        
        // Dimmed background with a hole
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color.Black.copy(alpha = 0.5f))
            drawRect(
                color = Color.Transparent,
                topLeft = frameOffset,
                size = frameSize,
                blendMode = BlendMode.Clear
            )
            drawRect(
                color = Color.White,
                topLeft = frameOffset,
                size = frameSize,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        
        // The draggable frame itself
        Box(
            modifier = Modifier
                .offset { IntOffset(frameOffset.x.roundToInt(), frameOffset.y.roundToInt()) }
                .size(
                    width = with(density) { frameSize.width.toDp() },
                    height = with(density) { frameSize.height.toDp() }
                )
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newX = (frameOffset.x + dragAmount.x).coerceIn(0f, maxWidth - frameSize.width)
                        val newY = (frameOffset.y + dragAmount.y).coerceIn(0f, maxHeight - frameSize.height)
                        frameOffset = Offset(newX, newY)
                    }
                }
        )
        
        // Resize handle (bottom right)
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (frameOffset.x + frameSize.width - 30).roundToInt(),
                        (frameOffset.y + frameSize.height - 30).roundToInt()
                    )
                }
                .size(40.dp)
                .background(Color.White, CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newWidth = (frameSize.width + dragAmount.x).coerceIn(100f, maxWidth - frameOffset.x)
                        val newHeight = (frameSize.height + dragAmount.y).coerceIn(100f, maxHeight - frameOffset.y)
                        frameSize = Size(newWidth, newHeight)
                    }
                }
        )

        // Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            
            FloatingActionButton(
                onClick = {
                    onCaptureClick(Rect(frameOffset, frameSize))
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Camera, contentDescription = "Capture")
            }
            
            // Empty spacer for symmetry if needed
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}
