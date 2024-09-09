package com.example.nfcreaderapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NfcDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_details)

        val nfcDetailsTextView: TextView = findViewById(R.id.nfcDetailsTextView)

        // Recibir los datos del NFC desde el intent
        val nfcData = intent.getStringExtra("NFC_DATA")
        val nfcDetails = intent.getStringExtra("NFC_DETAILS")

        // Mostrar los detalles en el TextView
        nfcDetailsTextView.text = "NFC Data: \n$nfcData\n\nDetails:\n$nfcDetails"
    }
}
