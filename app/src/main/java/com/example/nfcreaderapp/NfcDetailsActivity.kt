package com.example.nfcreaderapp

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NfcDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_details)

        // Habilitar el botón de regreso en la AppBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "NFC Details"

        val nfcDetailsTextView: TextView = findViewById(R.id.nfcDetailsTextView)

        // Recibir los datos del NFC desde el intent
        val nfcData = intent.getStringExtra("NFC_DATA")
        val nfcDetails = intent.getStringExtra("NFC_DETAILS")

        // Mostrar los detalles en el TextView
        nfcDetailsTextView.text = "NFC Data: \n$nfcData\n\nDetails:\n$nfcDetails"
    }

    // Manejar el botón de regreso
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Finalizar la actividad y regresar a MainActivity
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
