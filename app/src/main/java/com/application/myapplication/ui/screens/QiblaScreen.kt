package com.application.myapplication.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.myapplication.R
import com.application.myapplication.location.LocationGateState


@Composable
fun QiblaScreen(
    viewModel: QiblaViewModel,
    locationGateState: LocationGateState,
    onRequestLocationPermission: () -> Unit,
    onEnableDeviceLocation: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    val rotation by viewModel.pointerRotation.collectAsState(null)
    val bearing by viewModel.qiblaBearing.collectAsState(null)
    val userLocation by viewModel.userLocation.collectAsState()
    val isFacing by viewModel.isFacingQibla.collectAsState(false)

    when (locationGateState) {
        LocationGateState.PERMISSION_REQUIRED -> {
            PermissionRequiredContent(onRequestLocationPermission = onRequestLocationPermission)
        }

        LocationGateState.LOCATION_DISABLED -> {
            LocationDisabledContent(
                onEnableDeviceLocation = onEnableDeviceLocation,
                onOpenAppSettings = onOpenAppSettings
            )
        }

        LocationGateState.READY -> {
            when {
                userLocation == null || bearing == null || rotation == null -> {
                    LocatingContent()
                }
                else -> {
                    QiblaCompassContent(
                        rotation = rotation!!,
                        bearing = bearing!!,
                        isFacing = isFacing
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRequiredContent(onRequestLocationPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF003322))
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Location permission is required",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Facing Qibla needs permission to read your location and calculate the direction to the Kaaba.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestLocationPermission) {
            Text("Allow location")
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LocationDisabledContent(
    onEnableDeviceLocation: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF003322))
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Turn on device location",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "You allowed the app, but your phone's Location toggle is still off.\n\nTap the button below — Android will ask you to turn it on inside the app.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onEnableDeviceLocation) {
            Text("Turn on location")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onOpenAppSettings) {
            Text("Open app settings", color = Color.White)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Made with love by Sami",
            color = Color(0xFFC5A059).copy(alpha = 0.9f),
            fontSize = 16.sp,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(bottom = 28.dp)
        )
    }
}

@Composable
private fun LocatingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF003322))
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Locating your position...",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Go near a window or step outside if this takes long.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun QiblaCompassContent(rotation: Float, bearing: Float, isFacing: Boolean) {
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(250),
        label = "pointerRotation"
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF003322)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        Text("Qibla Direction", color = Color.White, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(60.dp))

        Box(modifier = Modifier.size(300.dp), contentAlignment = Alignment.Center) {
            if (isFacing) QiblaRippleEffect()
            Box(
                modifier = Modifier.size(300.dp).border(2.dp, Color(0xFFC5A059).copy(0.5f), CircleShape)
            )
            Box(
                modifier = Modifier.size(260.dp).rotate(animatedRotation),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().border(1.dp, Color.White.copy(0.2f), CircleShape)
                )
                Image(
                    painter = painterResource(R.drawable.pointer),
                    contentDescription = "Qibla pointer",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = (-26).dp).size(88.dp)
                )
            }
            Text("Kaaba", color = Color.White, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(50.dp))
        Text("${bearing.toInt()}°", color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Bold)
        Text("Direction to Kaaba", color = Color.White.copy(0.6f), fontSize = 20.sp)
        Spacer(modifier = Modifier.height(40.dp))

        if (isFacing) {
            Text(
                "You are facing the\nKaaba",
                color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center, lineHeight = 34.sp
            )
            Text(
                "May Allah accept\nyour prayers.",
                color = Color.White.copy(0.5f), fontSize = 16.sp,
                textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp)
            )
        } else {
            Text(
                "Rotate your phone until the pointer\nsettles toward the Kaaba",
                color = Color.White.copy(0.7f), fontSize = 16.sp,
                textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.padding(bottom = 8.dp)) {
            Text("Location", color = Color.White, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Text("Found", color = Color.White.copy(0.7f), fontSize = 16.sp)
        }
        Text(
            "Made with love by Sami",
            color = Color(0xFFC5A059).copy(0.9f), fontSize = 16.sp,
            fontStyle = FontStyle.Italic, modifier = Modifier.padding(bottom = 28.dp)
        )
    }
}

@Composable
private fun QiblaRippleEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "qiblaRipple")
    val rippleScale by infiniteTransition.animateFloat(
        0.8f, 1.25f,
        infiniteRepeatable(tween(1200), RepeatMode.Restart), label = "rippleScale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        0.35f, 0f,
        infiniteRepeatable(tween(1200), RepeatMode.Restart), label = "rippleAlpha"
    )
    Box(
        modifier = Modifier.size(300.dp).scale(rippleScale).graphicsLayer { alpha = rippleAlpha }
            .border(5.dp, Color(0xFFC5A059), CircleShape)
    )
}