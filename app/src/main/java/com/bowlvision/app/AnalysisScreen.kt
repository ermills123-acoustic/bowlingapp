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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

    var isAnalyzing by remember { mutableStateOf(true) }
    var poseResult by remember { mutableStateOf<PoseLandmarkerResult?>(null) }
    var finalShotResult by remember { mutableStateOf<ShotResult?>(null) }
    
    val analyzer = remember { BowlingAnalyzer() }
    val landmarkerHelper = remember {
        PoseLandmarkerHelper(context, object : PoseLandmarkerHelper.LandmarkerListener {
            override fun onError(error: String) {}
            override fun onResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
                if (!isAnalyzing) return
                
                poseResult = resultBundle.results
                val result = analyzer.analyzeFrame(resultBundle.results)
                if (result != null) {
                    finalShotResult = result
                    isAnalyzing = false
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
            // Live Camera View
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
                            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
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
            
            // Draw Skeleton Overlays dynamically (freezes when isAnalyzing becomes false)
            poseResult?.let { result ->
                if (result.landmarks().isNotEmpty()) {
                    val landmarks = result.landmarks()[0]
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        
                        fun mapToScreen(norm: com.google.mediapipe.tasks.components.containers.NormalizedLandmark): Offset {
                            return Offset(norm.x() * w, norm.y() * h)
                        }
                        
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

            if (!isAnalyzing && finalShotResult != null) {
                // Results Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xEE121212))
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color(0x88FFFFFF), CircleShape)
                    ) {
                        Text("X", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 64.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Analysis Complete", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${finalShotResult!!.score}",
                            fontSize = 64.sp,
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Feedback:", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        finalShotResult!!.feedbackBullets.forEach { bullet ->
                            Text(
                                "• $bullet",
                                color = Color.LightGray,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        Text("Tutorial: How to Improve", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Mock Web Tutorial Image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎳", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Web Tutorial Images Load Here", color = Color.Gray)
                            }
                        }
                        Text(
                            "Focus on keeping your arm swing tighter to your torso and ensuring your slide foot plants before the ball begins its final descent.",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(48.dp))
                        Button(
                            onClick = {
                                finalShotResult = null
                                analyzer.reset()
                                isAnalyzing = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Begin Next Analysis", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(32.dp)) // padding bottom
                    }
                }
            } else {
                // Live HUD
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Text("LIVE ANALYSIS", color = Color.Red, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Recording your shot...", color = Color.White, fontSize = 14.sp)
                }

                // Close Button in Live Mode
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color(0x88000000), CircleShape)
                ) {
                    Text("X", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Camera permission required", color = Color.White)
            }
        }
    }
}
