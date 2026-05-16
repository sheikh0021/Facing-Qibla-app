package com.application.myapplication.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun QiblaScreen(viewModel: QiblaViewModel){
    val rotation by viewModel.needleRotation.collectAsState(0f)
    val bear by viewModel.qiblaBearing.collectAsState(0f)
    val userLoc by viewModel.userLocation.collectAsState()

    val animatedRotation by animateFloatAsState(targetValue = rotation)

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF003322)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "Qibla Direction",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(60.dp))

        Box(
            contentAlignment = Alignment.Center
        ){
            Box(
                modifier = Modifier.size(300.dp).border(2.dp, Color(0xFFC5A059).copy(alpha = 0.5f),
                    CircleShape
                )
            )
            Box(
                modifier = Modifier.size(260.dp).rotate(animatedRotation).border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            )

            Text(text = "🕋", fontSize = 80.sp)
            Box(
                modifier = Modifier.align(Alignment.TopCenter).offset(y = (-20).dp).size(40.dp).background(Color(0xFFC5A059), CircleShape),
                contentAlignment = Alignment.Center
            ){
                Text("▲", color = Color.White, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(50.dp))

        Text(
            text = "${bear.toInt()}°",
            color = Color.White,
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (bear > 270 && bear < 360) "West" else "Direction",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "You are facing the \nQibla",
            color = Color.White,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )
        Text(
            text = "May Allah accept\nyour prayers.",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            Text("📍", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (userLoc != null) "Makkah, Saudi Arabia" else "Finding Location....",
                color = Color.White,
                fontSize = 16.sp
            )
        }

    }
}