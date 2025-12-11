package com.example.sistemmonitoringgas

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    // Firebase
    private lateinit var db: FirebaseDatabase
    private lateinit var sensorRef: DatabaseReference
    private lateinit var fanRef: DatabaseReference
    private lateinit var modeRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // =============================
        // Firebase
        // =============================
        db = FirebaseDatabase.getInstance()
        sensorRef = db.getReference("sensor")
        fanRef = db.getReference("kipas")
        modeRef = db.getReference("mode")

        // =============================
        // Ambil View dari XML
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
        // Listener Realtime Sensor
        // =============================
        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // GAS
                val gasValue = snapshot.child("gas").getValue(Int::class.java) ?: 0
                tvGasValue.text = gasValue.toString()

                val gasStatus = when {
                    gasValue >= 3000 -> "BAHAYA!"
                    gasValue >= 2000 -> "WASPADA"
                    else -> "AMAN"
                }

                tvGasStatus.text = "Status: $gasStatus"

                // Warna status
                tvGasStatus.setTextColor(
                    when (gasStatus) {
                        "BAHAYA!" -> Color.RED
                        "WASPADA" -> Color.YELLOW
                        else -> Color.GREEN
                    }
                )

                // SUHU & KELEMBAPAN
                val suhu = snapshot.child("suhu").getValue(Double::class.java) ?: 0.0
                val lembab = snapshot.child("kelembapan").getValue(Int::class.java) ?: 0

                tvTempValue.text = suhu.toString()
                tvHumidity.text = "Kelembapan: $lembab%"

                // Log realtime
                tvLog.text = "Gas: $gasValue | Suhu: $suhu °C | Lembap: $lembab%"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // =============================
        // Listener status kipas
        // =============================
        fanRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(Boolean::class.java) ?: false
                tvFanStatus.text = if (status) "Status: ON" else "Status: OFF"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // =============================
        // Mode kontrol kipas
        // =============================
        modeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mode = snapshot.getValue(String::class.java) ?: "manual"
                tvFanMode.text = mode
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // =============================
        // Tombol ON / OFF Manual
        // =============================
        btnOn.setOnClickListener {
            fanRef.setValue(true)
            tvLog.text = "Kipas dinyalakan (manual)"
        }

        btnOff.setOnClickListener {
            fanRef.setValue(false)
            tvLog.text = "Kipas dimatikan (manual)"
        }

        // =============================
        // Mode AUTO / MANUAL
        // =============================
        btnAuto.setOnClickListener {
            modeRef.setValue("auto")
            tvFanMode.text = "auto"
            tvLog.text = "Mode diubah ke AUTO"
        }

        btnManual.setOnClickListener {
            modeRef.setValue("manual")
            tvFanMode.text = "manual"
            tvLog.text = "Mode diubah ke MANUAL"
        }
    }
}
