package com.example.nfcreaderapp

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.provider.Settings
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfcDataTextView: TextView

    private fun startNFCListening() {
        // Aquí puedes agregar la lógica necesaria para comenzar a escuchar NFC
        nfcAdapter?.enableReaderMode(this, { tag ->
            // Aquí puedes manejar el tag NFC detectado
            runOnUiThread {
                nfcDataTextView.text = getString(R.string.nfc_data, tag.toString())
            }
        }, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B, null)
    }

    private fun stopNFCListening() {
        // Detener el modo de lectura NFC cuando ya no se necesite escuchar NFC
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nfcDataTextView = findViewById(R.id.nfcDataTextView)

        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            // Si el dispositivo no soporta NFC o el adaptador NFC es null, se muestra un mensaje.
            nfcDataTextView.setText(R.string.nfc_not_supported)
            return // Terminar la ejecución de onCreate si no hay soporte NFC
        }

        // Verificar si el NFC está activado
        if (!nfcAdapter!!.isEnabled) {
            nfcDataTextView.setText(R.string.nfc_disabled)
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        }

        // Configurar botón para iniciar la lectura de NFC
        val nfcButton: Button = findViewById(R.id.nfcButton)
        nfcButton.setOnClickListener {
            // Iniciar la animación durante 10 segundos
            val scaleAnimation = ScaleAnimation(
                1f, 1.2f, // De tamaño normal a 20% más grande
                1f, 1.2f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 10000 // Duración de la animación en milisegundos
                repeatCount = 0 // No repetir la animación
                fillAfter = true // Mantener el estado al finalizar
            }

            // Iniciar la animación
            nfcButton.startAnimation(scaleAnimation)

            nfcDataTextView.setText(R.string.nfc_waiting)

            // Aquí deberías empezar el proceso de lectura de NFC (10 segundos de escucha)
            startNFCListening()

            // Detener la animación después de 10 segundos
            nfcButton.postDelayed({
                scaleAnimation.cancel()
                // Detener la escucha de NFC
                stopNFCListening()
            }, 10000)
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))

        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, filters, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent?.action) {
            val tag: Tag? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }

            tag?.let {
                val ndef = Ndef.get(tag)
                ndef?.cachedNdefMessage?.let { message ->
                    val records = message.records
                    if (records.isNotEmpty()) {
                        val nfcData = String(records[0].payload)
                        nfcDataTextView.text = getString(R.string.nfc_data, nfcData)
                    } else {
                        nfcDataTextView.setText(R.string.nfc_empty)
                    }
                } ?: run {
                    nfcDataTextView.setText(R.string.nfc_empty)
                }
            }
        }
    }
}

