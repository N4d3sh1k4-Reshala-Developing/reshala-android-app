package com.bignerdranch.android.reshalaalfa01.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

@Composable
fun CameraScreen(
    onClose: () -> Unit,
    onResult: (Bitmap, Rect, Int, Int) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    capturedBitmap = BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                Log.e("CameraScreen", "Failed to load image", e)
            }
        }
    }
    
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
        val bitmap = capturedBitmap
        if (bitmap != null) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val boxWidth = this.constraints.maxWidth
                val boxHeight = this.constraints.maxHeight
                
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                
                ResizableSelectionFrame(
                    onCaptureClick = { rect ->
                        onResult(bitmap, rect, boxWidth, boxHeight)
                    },
                    onClose = { capturedBitmap = null },
                    isGalleryMode = true
                )
            }
        } else if (hasCameraPermission) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            
            // Simple overlay with Shutter button
            Box(modifier = Modifier.fillMaxSize()) {
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
                    
                    if (isCapturing) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        FloatingActionButton(
                            onClick = {
                                isCapturing = true
                                imageCapture.takePicture(
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(image: ImageProxy) {
                                            capturedBitmap = imageProxyToBitmap(image)
                                            image.close()
                                            isCapturing = false
                                        }
                                        override fun onError(exception: ImageCaptureException) {
                                            Log.e("CameraScreen", "Capture failed", exception)
                                            isCapturing = false
                                        }
                                    }
                                )
                            },
                            containerColor = Color.White,
                            contentColor = Color.Black,
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Capture")
                        }
                    }

                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Color.White)
                    }
                }
            }
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

private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val matrix = Matrix()
    matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

@Composable
fun ResizableSelectionFrame(
    onCaptureClick: (Rect) -> Unit,
    onClose: () -> Unit,
    onGalleryClick: (() -> Unit)? = null,
    isGalleryMode: Boolean = false
) {
    val density = LocalDensity.current

    val offsetSaver = listSaver<Offset, Float>(
        save = { listOf(it.x, it.y) },
        restore = { Offset(it[0], it[1]) }
    )
    val sizeSaver = listSaver<Size, Float>(
        save = { listOf(it.width, it.height) },
        restore = { Size(it[0], it[1]) }
    )

    var frameOffset by rememberSaveable(stateSaver = offsetSaver) { mutableStateOf(Offset(100f, 500f)) }
    var frameSize by rememberSaveable(stateSaver = sizeSaver) { mutableStateOf(Size(600f, 400f)) }
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidthPx = this.constraints.maxWidth.toFloat()
        val maxHeightPx = this.constraints.maxHeight.toFloat()
        
        // Dimmed background with a hole
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.99f) // Required for BlendMode.Clear to work correctly
        ) {
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
                        val newX = (frameOffset.x + dragAmount.x).coerceIn(0f, maxWidthPx - frameSize.width)
                        val newY = (frameOffset.y + dragAmount.y).coerceIn(0f, maxHeightPx - frameSize.height)
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
                        val newWidth = (frameSize.width + dragAmount.x).coerceIn(100f, maxWidthPx - frameOffset.x)
                        val newHeight = (frameSize.height + dragAmount.y).coerceIn(100f, maxHeightPx - frameOffset.y)
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
                Icon(if (isGalleryMode) Icons.Default.Check else Icons.Default.Camera, contentDescription = if (isGalleryMode) "Confirm" else "Capture")
            }
            
            if (onGalleryClick != null) {
                IconButton(
                    onClick = onGalleryClick,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Color.White)
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}
