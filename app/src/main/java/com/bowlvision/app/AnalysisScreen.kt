package com.bowlvision.app

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.util.concurrent.Executors

@Composable
fun AnalysisScreen(onNavigateBack: () -> Unit = {}) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var poseResult by remember { mutableStateOf<PoseLandmarkerResult?>(null) }
    var feedback by remember { mutableStateOf<BowlingFeedback?>(null) }
    
    val analyzer = remember { BowlingAnalyzer() }
    val landmarkerHelper = remember {
        PoseLandmarkerHelper(context, object : PoseLandmarkerHelper.LandmarkerListener {
            override fun onError(error: String) {}
            override fun onResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
                poseResult = resultBundle.results
                val resultFeedback = analyzer.analyzeFrame(resultBundle.results)
                if (resultFeedback != null) {
                    feedback = resultFeedback
                }
            }
        })
    }

    DisposableEffect(Unit) {
        onDispose {
            landmarkerHelper.clearPoseLandmarker()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                    landmarkerHelper.detectLiveStream(imageProxy, isFrontCamera = false)
                                }
                            }
                            
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    
                    previewView
                }
            )
            
            // Draw Skeleton Overlays dynamically
            poseResult?.let { result ->
                if (result.landmarks().isNotEmpty()) {
                    val landmarks = result.landmarks()[0]
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        
                        // Scale coordinates
                        fun mapToScreen(norm: com.google.mediapipe.tasks.components.containers.NormalizedLandmark): Offset {
                            return Offset(norm.x() * w, norm.y() * h)
                        }
                        
                        // Draw lines between key joints (simplistic)
                        val connections = listOf(
                            Pair(11, 12), Pair(11, 13), Pair(13, 15),
                            Pair(12, 14), Pair(14, 16), Pair(11, 23),
                            Pair(12, 24), Pair(23, 24), Pair(23, 25),
                            Pair(25, 27), Pair(24, 26), Pair(26, 28)
                        )
                        
                        for (pair in connections) {
                            if (pair.first < landmarks.size && pair.second < landmarks.size) {
                                drawLine(
                                    color = Color(0xFF1E88E5),
                                    start = mapToScreen(landmarks[pair.first]),
                                    end = mapToScreen(landmarks[pair.second]),
                                    strokeWidth = 6f
                                )
                            }
                        }
                        
                        for (lm in landmarks) {
                            drawCircle(color = Color(0xFFE53935), radius = 8f, center = mapToScreen(lm))
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Camera permission required", color = Color.White)
            }
        }

        // Overlay HUD
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text("LIVE ANALYSIS", color = Color.Red, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x88000000)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Form Score", color = Color.LightGray, fontSize = 12.sp)
                    Text(feedback?.score?.toString() ?: "--", color = Color.Green, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        // Voice Feedback 
        feedback?.feedbackMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .background(Color(0x881E1E1E), shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(msg, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Close Button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color(0x88000000), shape = androidx.compose.foundation.shape.CircleShape)
        ) {
            Text("X", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
