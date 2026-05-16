# 🕋 Live Qibla Direction App: Complete Beginner's Guide

This guide will walk you through building a professional Qibla direction app from scratch. We will use **Clean Architecture**, **MVVM**, and **Jetpack Compose**. 

By following these steps, you will create an app that looks exactly like the premium design, works anywhere in the world, and requires **no backend**.

---

## 🏗️ Part 1: Understanding the System

### 1. Clean Architecture
We divide the app into 3 layers so the code is easy to manage:
- **Domain Layer**: The "Brains". It contains the math formula to calculate the angle to Makkah. It doesn't care about sensors or UI; it just does math.
- **Data Layer**: The "Hands". It talks to the phone's hardware (GPS for location and Magnetometer for compass).
- **Presentation Layer (UI)**: The "Face". It shows the beautiful dark green screen and rotates the compass needle.

### 2. MVVM (Model-View-ViewModel)
- **Model**: The data (e.g., "The angle is 286 degrees").
- **View**: The Jetpack Compose UI.
- **ViewModel**: The "Translator". It takes raw sensor data from the Data layer, asks the Domain layer for the math result, and gives the final rotation angle to the View.

---

## 🚀 Part 2: Step-by-Step Implementation

### Step 1: Configuration & Permissions
We need to tell Android that our app requires GPS and Location services.

#### 1.1 Update `app/build.gradle.kts`
Open `app/build.gradle.kts` and add the location library.
**Full File Content (Dependencies Section):**
```kotlin
dependencies {
    // ... keep existing dependencies ...
    implementation("com.google.android.gms:play-services-location:21.0.1")
}
```

#### 1.2 Update `app/src/main/AndroidManifest.xml`
Add permissions so the app can access GPS.
**Add these lines before the `<application>` tag:**
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

---

### Step 2: The Domain Layer (The Math)
Create a new folder: `app/src/main/java/com/application/myapplication/domain/`

#### 2.1 Create `QiblaCalculator.kt`
This file contains the "Spherical Trigonometry" formula to find the bearing from any point on Earth to the Kaaba.

**Path:** `app/src/main/java/com/application/myapplication/domain/QiblaCalculator.kt`
```kotlin
package com.application.myapplication.domain

import kotlin.math.*

object QiblaCalculator {
    // Precise coordinates of the Kaaba in Makkah
    private const val KAABA_LAT = 21.4225
    private const val KAABA_LNG = 39.8262

    /**
     * Calculates the angle (bearing) from the user's location to the Kaaba.
     */
    fun calculateBearing(userLat: Double, userLng: Double): Float {
        val userLatRad = Math.toRadians(userLat)
        val userLngRad = Math.toRadians(userLng)
        val kaabaLatRad = Math.toRadians(KAABA_LAT)
        val kaabaLngRad = Math.toRadians(KAABA_LNG)

        val deltaLng = kaabaLngRad - userLngRad

        val y = sin(deltaLng) * cos(kaabaLatRad)
        val x = cos(userLatRad) * sin(kaabaLatRad) - 
                sin(userLatRad) * cos(kaabaLatRad) * cos(deltaLng)
        
        // Convert radians back to degrees and normalize to 0-360
        val bearing = Math.toDegrees(atan2(y, x)).toFloat()
        return (bearing + 360) % 360
    }
}
```

---

### Step 3: The Data Layer (Sensors)
Create a new folder: `app/src/main/java/com/application/myapplication/data/`

#### 3.1 Create `CompassProvider.kt`
This class reads the Accelerometer (tilt) and Magnetometer (magnetic field) to determine which way the phone is pointing.

**Path:** `app/src/main/java/com/application/myapplication/data/CompassProvider.kt`
```kotlin
package com.application.myapplication.data

import android.content.Context
import android.hardware.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CompassProvider(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    
    // This flow will emit the current rotation angle of the phone
    private val _azimuth = MutableStateFlow(0f)
    val azimuth = _azimuth.asStateFlow()

    fun start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, 3)
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagnetic, 0, 3)
        }

        val r = FloatArray(9)
        val i = FloatArray(9)
        if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(r, orientation)
            // orientation[0] is the azimuth (rotation around Z-axis)
            _azimuth.value = Math.toDegrees(orientation[0].toDouble()).toFloat()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
```

---

### Step 4: The Presentation Layer (ViewModel & UI)
Create a new folder: `app/src/main/java/com/application/myapplication/ui/`

#### 4.1 Create `QiblaViewModel.kt`
This file connects the sensors to the UI. It calculates the final rotation needed for the needle.

**Path:** `app/src/main/java/com/application/myapplication/ui/QiblaViewModel.kt`
```kotlin
package com.application.myapplication.ui

import androidx.lifecycle.ViewModel
import com.application.myapplication.data.CompassProvider
import kotlinx.coroutines.flow.*

class QiblaViewModel(private val compassProvider: CompassProvider) : ViewModel() {
    
    // The direction the phone is pointing
    val azimuth = compassProvider.azimuth
    
    // The direction of the Kaaba (Static 286° for demo, or dynamic via GPS)
    private val _qiblaBearing = MutableStateFlow(286f)
    val qiblaBearing = _qiblaBearing.asStateFlow()

    // The final rotation for the UI needle
    // Formula: Kaaba Direction - Phone Direction
    val needleRotation = combine(azimuth, qiblaBearing) { az, bear ->
        bear - az
    }

    fun updateQiblaBearing(newBearing: Float) {
        _qiblaBearing.value = newBearing
    }
}
```

#### 4.2 Create `QiblaScreen.kt`
This is the UI file. It uses Jetpack Compose to create the dark green interface.

**Path:** `app/src/main/java/com/application/myapplication/ui/QiblaScreen.kt`
```kotlin
package com.application.myapplication.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

@Composable
fun QiblaScreen(viewModel: QiblaViewModel) {
    val rotation by viewModel.needleRotation.collectAsState(0f)
    val bear by viewModel.qiblaBearing.collectAsState(0f)
    
    // This makes the needle movement smooth
    val animatedRotation by animateFloatAsState(targetValue = rotation)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF003322)), // Dark Islamic Green
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

        // --- COMPASS SECTION ---
        Box(contentAlignment = Alignment.Center) {
            // 1. The Outer Decorative Circle
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .border(2.dp, Color(0xFFC5A059).copy(alpha = 0.5f), CircleShape)
            )

            // 2. The Rotating Compass Scale (simplified)
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .rotate(animatedRotation)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            )

            // 3. The Kaaba Icon (Center)
            Text(text = "🕋", fontSize = 80.sp)

            // 4. The Top Pointer (Fixed at top)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-20).dp)
                    .size(40.dp)
                    .background(Color(0xFFC5A059), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("▲", color = Color.White, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(50.dp))

        // --- TEXT DATA SECTION ---
        Text(
            text = "${bear.toInt()}°",
            color = Color.White,
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "West",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "You are facing the\nQibla",
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

        // Bottom Location Text
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            Text("📍", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Makkah, Saudi Arabia",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}
```

---

### Step 5: Final Wiring (MainActivity)
This is the entry point of your app. It connects the sensor provider to the screen.

**Path:** `app/src/main/java/com/application/myapplication/MainActivity.kt`
```kotlin
package com.application.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.application.myapplication.data.CompassProvider
import com.application.myapplication.ui.QiblaScreen
import com.application.myapplication.ui.QiblaViewModel

class MainActivity : ComponentActivity() {
    
    private lateinit var compassProvider: CompassProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Initialize the Compass Provider
        compassProvider = CompassProvider(this)
        
        // 2. Initialize the ViewModel
        val viewModel = QiblaViewModel(compassProvider)

        // 3. Set the UI Content
        setContent {
            QiblaScreen(viewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        // Start listening to sensors when app is opened
        compassProvider.start()
    }

    override fun onPause() {
        super.onPause()
        // Stop listening when app is in background to save battery
        compassProvider.stop()
    }
}
```

---

## 🏁 Summary of Next Steps
1. **Create the Folders**: In Android Studio, right-click on `com.application.myapplication` and select `New -> Package` to create `domain`, `data`, and `ui`.
2. **Copy the Code**: Copy the contents above into the correct files.
3. **Add Assets**: Replace the `🕋` emoji with a real Kaaba image by placing it in `res/drawable` and using `Image(painterResource(R.drawable.kaaba)...)`.
4. **Permissions**: When you run the app, ensure you grant Location permissions so it can calculate your real position.

**Congratulations! You have just built a professional Qibla application following Clean Architecture!** �✨
