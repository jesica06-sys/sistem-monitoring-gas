package com.example.sistemmonitoringgas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.firebase.database.*

class DetailActivity : AppCompatActivity() {

    private lateinit var db: FirebaseDatabase
    private lateinit var sensorRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Inisialisasi Firebase
        db = FirebaseDatabase.getInstance()
        sensorRef = db.getReference("alat1") // FIX 1: Ubah dari "sensor" ke "alat1"

        // Ambil komponen
        val tvGas = findViewById<TextView>(R.id.tvGas)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvUpdated = findViewById<TextView>(R.id.tvUpdated)

        // Listener Firebase
        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val gasValue = snapshot.child("gas").getValue(Int::class.java) ?: 0
                // FIX 2: Hapus referensi ke "update" karena Arduino tidak mengirimkannya, 
                // dan ganti dengan placeholder
                val waktuUpdate = "Real-time"

                // Tampilkan nilai analog, dan gunakan logika status yang sama dengan MainActivity
                tvGas.text = "$gasValue (Analog)"
                tvUpdated.text = "Update: $waktuUpdate"

                // Gunakan batas analog untuk penentuan status yang lebih konsisten
                val status = when {
                    gasValue >= 3000 -> "BAHAYA!"
                    gasValue >= 2000 -> "WASPADA"
                    else -> "AMAN"
                }

                tvStatus.text = "Status: $status"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }
}