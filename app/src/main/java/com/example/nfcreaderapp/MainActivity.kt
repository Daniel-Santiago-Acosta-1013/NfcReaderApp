package com.example.nfcreaderapp

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.view.View
import android.provider.Settings
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfcDataTextView: TextView

    private fun startNFCListening() {
        // Aquí puedes agregar la lógica necesaria para comenzar a escuchar NFC
        nfcAdapter?.enableReaderMode(this, { tag ->
            // Aquí puedes manejar el tag NFC detectado
            runOnUiThread {
                handleNfcTag(tag)
            }
        }, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B, null)
    }

    private fun stopNFCListening() {
        // Detener el modo de lectura NFC cuando ya no se necesite escuchar NFC
        nfcAdapter?.disableReaderMode(this)
    }

    private fun createCircleAnimation(duration: Long): AnimationSet {
        val scaleAnimation = ScaleAnimation(
            1f, 1.5f, // Aumentar el tamaño en 50%
            1f, 1.5f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleAnimation.duration = duration
        scaleAnimation.repeatCount = Animation.INFINITE
        scaleAnimation.repeatMode = Animation.REVERSE

        val alphaAnimation = AlphaAnimation(1f, 0f)
        alphaAnimation.duration = duration
        alphaAnimation.repeatCount = Animation.INFINITE
        alphaAnimation.repeatMode = Animation.REVERSE

        val animationSet = AnimationSet(true)
        animationSet.addAnimation(scaleAnimation)
        animationSet.addAnimation(alphaAnimation)

        return animationSet
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nfcDataTextView = findViewById(R.id.nfcDataTextView)

        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            // Si el dispositivo no soporta NFC o el adaptador NFC es null, mostramos un modal (AlertDialog)
            showNfcNotSupportedDialog()
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
            // Animación de "respiración" (pulsación) para el botón
            val scaleAnimation = ScaleAnimation(
                1f, 1.2f, // De tamaño normal a 20% más grande
                1f, 1.2f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 1000 // Cada pulsación dura 1 segundo
                repeatCount = Animation.INFINITE // La animación se repite indefinidamente
                repeatMode = Animation.REVERSE
            }

            nfcButton.startAnimation(scaleAnimation)

            // Animaciones de escala y opacidad para los círculos concéntricos
            val circle1 = findViewById<View>(R.id.circle1)
            val circle2 = findViewById<View>(R.id.circle2)
            val circle3 = findViewById<View>(R.id.circle3)

            val circleAnimation1 = createCircleAnimation(3000)
            val circleAnimation2 = createCircleAnimation(4000)
            val circleAnimation3 = createCircleAnimation(5000)

            circle1.startAnimation(circleAnimation1)
            circle2.startAnimation(circleAnimation2)
            circle3.startAnimation(circleAnimation3)

            nfcDataTextView.setText(R.string.nfc_waiting)

            // Iniciar el proceso de escucha NFC (10 segundos de escucha)
            startNFCListening()

            // Detener la animación después de 10 segundos
            nfcButton.postDelayed({
                scaleAnimation.cancel()
                circle1.clearAnimation()
                circle2.clearAnimation()
                circle3.clearAnimation()

                // Detener la escucha de NFC
                stopNFCListening()
            }, 10000)
        }
    }

    // Manejar la información del tag NFC y pasarla a la nueva actividad
    private fun handleNfcTag(tag: Tag) {
        val ndef = Ndef.get(tag)
        ndef?.cachedNdefMessage?.let { message ->
            val records = message.records
            if (records.isNotEmpty()) {
                val nfcData = String(records[0].payload)
                val nfcDetails = records.joinToString("\n") { record ->
                    "Record Type: ${record.tnf}, Data: ${String(record.payload)}"
                }

                // Enviar los datos a NfcDetailsActivity
                val intent = Intent(this, NfcDetailsActivity::class.java)
                intent.putExtra("NFC_DATA", nfcData)
                intent.putExtra("NFC_DETAILS", nfcDetails)
                startActivity(intent)
            } else {
                nfcDataTextView.setText(R.string.nfc_empty)
            }
        } ?: run {
            nfcDataTextView.setText(R.string.nfc_empty)
        }
    }

    // Función para mostrar el diálogo de NFC no soportado
    private fun showNfcNotSupportedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.nfc_not_supported))
        builder.setMessage(getString(R.string.nfc_not_supported_message))
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            finish() // Finalizar la actividad ya que no tiene sentido continuar sin NFC
        }
        builder.setCancelable(false)
        builder.show()
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
                handleNfcTag(it)
            }
        }
    }
}
