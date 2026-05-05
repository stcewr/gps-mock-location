package com.gpsmocklocation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject
import java.io.IOException

class MockLocationService : Service() {

    private val CHANNEL_ID = "MockLocationChannel"
    private val NOTIFICATION_ID = 1

    private lateinit var locationManager: LocationManager
    private var server: MockLocationServer? = null
    private val handler = Handler(Looper.getMainLooper())

    private var currentLng = 116.397428
    private var currentLat = 39.90923
    private var isRunning = false

    companion object {
        const val PORT = 8080
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        try {
            server = MockLocationServer(PORT, this)
            server?.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun updateLocation(lng: Double, lat: Double) {
        currentLng = lng
        currentLat = lat

        if (!isRunning) {
            isRunning = true
            startMockLocationUpdates()
        }
    }

    private fun startMockLocationUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                if (isRunning) {
                    setMockLocation(LocationManager.GPS_PROVIDER, currentLat, currentLng)
                    setMockLocation(LocationManager.NETWORK_PROVIDER, currentLat, currentLng)
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun setMockLocation(provider: String, latitude: Double, longitude: Double) {
        try {
            try {
                locationManager.removeTestProvider(provider)
            } catch (e: Exception) {
                //  provider doesn't exist yet
            }

            locationManager.addTestProvider(
                provider,
                false, false, false, false,
                true, true, true, 0, 5
            )
            locationManager.setTestProviderEnabled(provider, true)

            val location = Location(provider).apply {
                this.latitude = latitude
                this.longitude = longitude
                accuracy = 1.0f
                time = System.currentTimeMillis()
                elapsedRealtimeNanos = System.nanoTime()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    verticalAccuracyMeters = 1.0f
                    speedAccuracyMetersPerSecond = 0.1f
                    bearingAccuracyDegrees = 1.0f
                }
            }

            locationManager.setTestProviderLocation(provider, location)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "模拟定位服务"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = "GPS Mock Location Service"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GPS 模拟定位运行中")
            .setContentText("端口: $PORT | 等待连接...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacksAndMessages(null)

        try {
            try {
                locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
                locationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        server?.stop()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

class MockLocationServer(port: Int, private val service: MockLocationService) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        if (session.method == Method.POST && session.uri == "/location") {
            try {
                val body = HashMap<String, String>()
                session.parseBody(body)
                val json = JSONObject(body["postData"])

                val lng = json.getDouble("lng")
                val lat = json.getDouble("lat")

                service.updateLocation(lng, lat)

                return newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json",
                    JSONObject().put("success", true).toString()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return newFixedLengthResponse(
                    Response.Status.BAD_REQUEST,
                    "application/json",
                    JSONObject().put("success", false).toString()
                )
            }
        }

        if (session.method == Method.GET && session.uri == "/") {
            return newFixedLengthResponse(
                Response.Status.OK,
                "text/plain",
                "GPS Mock Location Server is running on port $port\n\n" +
                "POST /location with {\"lng\": x.x, \"lat\": x.x}\n" +
                "to set mock location."
            )
        }

        return newFixedLengthResponse(
            Response.Status.NOT_FOUND,
            "text/plain",
            "Not Found"
        )
    }
}
