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
    // Tambahkan variabel baru untuk menulis status kontrol manual kipas
    private lateinit var fanControlWriteRef: DatabaseReference // Tambahan: digunakan untuk menulis status manual ON/OFF

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }


        // =============================
        // Firebase
        // =============================
        db = FirebaseDatabase.getInstance()
        sensorRef = db.getReference("alat1") // FIX 1: Ubah dari "sensor" ke "alat1"
        fanRef = db.getReference("alat1/kipas") // FIX 2: Ubah dari "kipas" ke "alat1/kipas" (Untuk membaca Status ON/OFF)
        modeRef = db.getReference("alat1/kontrol/mode") // FIX 3: Ubah dari "mode" ke "alat1/kontrol/mode" (Untuk membaca/menulis Mode)
        fanControlWriteRef = db.getReference("alat1/kontrol/on") // FIX 4: Referensi untuk menulis status manual (true/false)

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
        // Listener Realtime Sensor (Membaca dari /alat1)
        // =============================
        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // GAS (Mengambil child dari /alat1/)
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

                // SUHU & KELEMBAPAN (Mengambil child dari /alat1/)
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
        // Listener status kipas (Membaca String "ON"/"OFF" dari /alat1/kipas)
        // =============================
        fanRef.addValueEventListener(object : ValueEventListener { // fanRef kini menunjuk ke /alat1/kipas
            override fun onDataChange(snapshot: DataSnapshot) {
                // FIX 5: Mengambil nilai sebagai String, karena Arduino menulis String "ON" atau "OFF"
                val status = snapshot.getValue(String::class.java) ?: "OFF"
                tvFanStatus.text = "Status: $status"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // =============================
        // Mode kontrol kipas (Membaca String dari /alat1/kontrol/mode)
        // =============================
        modeRef.addValueEventListener(object : ValueEventListener { // modeRef kini menunjuk ke /alat1/kontrol/mode
            override fun onDataChange(snapshot: DataSnapshot) {
                val mode = snapshot.getValue(String::class.java) ?: "manual"
                tvFanMode.text = mode
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // =============================
        // Tombol ON / OFF Manual (Menulis boolean ke /alat1/kontrol/on)
        // =============================
        btnOn.setOnClickListener {
            fanControlWriteRef.setValue(true) // FIX 6: Menggunakan fanControlWriteRef untuk menulis boolean ke /alat1/kontrol/on
            tvLog.text = "Kipas dinyalakan (manual)"
        }

        btnOff.setOnClickListener {
            fanControlWriteRef.setValue(false) // FIX 6: Menggunakan fanControlWriteRef
            tvLog.text = "Kipas dimatikan (manual)"
        }

        // =============================
        // Mode AUTO / MANUAL (Menulis String ke /alat1/kontrol/mode)
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