package com.example.sistemmonitoringgas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    // =============================
    // Firebase Reference
    // =============================
    private lateinit var db: FirebaseDatabase
    private lateinit var sensorRef: DatabaseReference
    private lateinit var fanRef: DatabaseReference
    private lateinit var modeRef: DatabaseReference
    private lateinit var fanControlWriteRef: DatabaseReference

    // =============================
    // Mode (AUTO / MANUAL)
    // =============================
    private var currentMode = "manual"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Permission notifikasi (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }

        // =============================
        // Firebase Init
        // =============================
        db = FirebaseDatabase.getInstance()
        sensorRef = db.getReference("alat1")
        fanRef = db.getReference("alat1/kipas")
        modeRef = db.getReference("alat1/kontrol/mode")
        fanControlWriteRef = db.getReference("alat1/kontrol/fan")

        // =============================
        // View Init
        // =============================
        val tvGasValue = findViewById<TextView>(R.id.tvGasValue)
        val tvGasStatus = findViewById<TextView>(R.id.tvGasStatus)

        val tvTempValue = findViewById<TextView>(R.id.tvTempValue)
        val tvHumidity = findViewById<TextView>(R.id.tvHumidity)

        val tvFanMode = findViewById<TextView>(R.id.tvFanMode)
        val tvFanStatus = findViewById<TextView>(R.id.tvFanStatus)

        val btnOn = findViewById<Button>(R.id.btnOn)
        val btnOff = findViewById<Button>(R.id.btnOff)

        val btnAuto = findViewById<Button>(R.id.btnAuto)
        val btnManual = findViewById<Button>(R.id.btnManual)

        val tvLog = findViewById<TextView>(R.id.tvLog)

        // =============================
        // MODE LISTENER
        // =============================
        modeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentMode = snapshot.getValue(String::class.java) ?: "manual"
                tvFanMode.text = currentMode
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // =============================
        // SENSOR LISTENER
        // =============================
        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val gasValue = snapshot.child("gas").getValue(Int::class.java) ?: 0
                tvGasValue.text = gasValue.toString()

                val gasStatus = when {
                    gasValue >= 3000 -> {
                        showGasNotification(
                            "🚨 BAHAYA GAS!",
                            "Gas sangat tinggi ($gasValue). Segera evakuasi!"
                        )
                        "BAHAYA!"
                    }
                    gasValue >= 2000 -> {
                        showGasNotification(
                            "⚠️ WASPADA GAS",
                            "Gas terdeteksi ($gasValue). Periksa ruangan!"
                        )
                        "WASPADA"
                    }
                    else -> "AMAN"
                }

                tvGasStatus.text = "Status: $gasStatus"
                tvGasStatus.setTextColor(
                    when (gasStatus) {
                        "BAHAYA!" -> Color.RED
                        "WASPADA" -> Color.YELLOW
                        else -> Color.GREEN
                    }
                )

                val suhu = snapshot.child("suhu").getValue(Double::class.java) ?: 0.0
                val lembab = snapshot.child("kelembapan").getValue(Int::class.java) ?: 0

                tvTempValue.text = "$suhu °C"
                tvHumidity.text = "Kelembapan: $lembab%"

                tvLog.text = "Gas: $gasValue | Suhu: $suhu °C | Lembap: $lembab%"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // =============================
        // STATUS KIPAS
        // =============================
        fanRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java) ?: "OFF"
                tvFanStatus.text = "Status: $status"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // =============================
        // MANUAL ON / OFF
        // =============================
        btnOn.setOnClickListener {
            if (currentMode == "manual") {
                fanControlWriteRef.setValue(true)
                tvLog.text = "MANUAL: Kipas ON"
            }
        }

        btnOff.setOnClickListener {
            if (currentMode == "manual") {
                fanControlWriteRef.setValue(false)
                tvLog.text = "MANUAL: Kipas OFF"
            }
        }

        // =============================
        // UBAH MODE
        // =============================
        btnAuto.setOnClickListener {
            modeRef.setValue("auto")
            tvLog.text = "Mode AUTO diaktifkan"
        }

        btnManual.setOnClickListener {
            modeRef.setValue("manual")
            tvLog.text = "Mode MANUAL diaktifkan"
        }
    }

    // =============================
    // NOTIFIKASI GAS
    // =============================
    private fun showGasNotification(title: String, message: String) {

        val channelId = "GAS_ALERT"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Peringatan Gas",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(1, notification)
    }
}
