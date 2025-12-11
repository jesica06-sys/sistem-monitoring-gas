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
        sensorRef = db.getReference("sensor")

        // Ambil komponen
        val tvGas = findViewById<TextView>(R.id.tvGas)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvUpdated = findViewById<TextView>(R.id.tvUpdated)

        // Listener Firebase
        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val gasValue = snapshot.child("gas").getValue(Int::class.java) ?: 0
                val waktuUpdate = snapshot.child("update").getValue(String::class.java) ?: "-"

                tvGas.text = "$gasValue %"
                tvUpdated.text = "Update: $waktuUpdate"

                val status = when {
                    gasValue >= 80 -> "BAHAYA!"
                    gasValue >= 50 -> "WASPADA"
                    else -> "AMAN"
                }

                tvStatus.text = "Status: $status"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }
}
