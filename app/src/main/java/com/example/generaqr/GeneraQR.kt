package com.example.generaqr

import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.lang.Exception

class GeneraQR : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var codigosQRRef: CollectionReference
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var imgCodigoQR: ImageView
    private var valCodigoQR: String? = null
    private var docId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genera_qr)
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        codigosQRRef = db.collection("codigosQR")
        sharedPreferences = getSharedPreferences("com.example.generaqr", MODE_PRIVATE)

        var toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        imgCodigoQR = findViewById<ImageView>(R.id.codigoQR)
        var btnGenerar = findViewById<Button>(R.id.btnGenerar)
        val savedQRCode = sharedPreferences.getString("savedQRCode", null)
        if (savedQRCode != null) {
            valCodigoQR = savedQRCode
            generarCodigoQR()
        } else {
            buscandoCodigoQR()
        }

        btnGenerar.setOnClickListener {
            if (valCodigoQR != null) {
                Toast.makeText(this, "El codigo QR debe ser utilizado y debe haber códigos disponibles para generar otro.", Toast.LENGTH_SHORT).show()
            } else {
                buscandoCodigoQR()
            }
        }

        setSupportActionBar(toolbar)
        buscandoNuevosCodigos()
    }

    private fun buscandoCodigoQR() {
        codigosQRRef.get().addOnSuccessListener { documents ->
            var sinGenerar = false
            for (document in documents) {
                if (document.getString("status") == "Sin generar") {
                    valCodigoQR = document.getString("codigo")
                    docId = document.id
                    document.reference.update("status", "Generado")
                    sinGenerar = true
                    break
                }
            }
            if (!sinGenerar) {
                Toast.makeText(this, "Todos los códigos han sido utilizados, favor de esperar uno nuevo.", Toast.LENGTH_LONG).show()
            } else if (valCodigoQR == null) {
                Toast.makeText(this, "No hay códigos disponibles, favor de esperar uno nuevo.", Toast.LENGTH_SHORT).show()
            } else {
                generarCodigoQR()
                buscandoCambiosdeStatus()
            }
        }.addOnFailureListener { exception ->
            Log.w(ContentValues.TAG, "Error obteniendo documentos: ", exception)
        }
    }

    private fun generarCodigoQR() {
        try {
            var barcodeEncoder : BarcodeEncoder = BarcodeEncoder()
            var bitmap = barcodeEncoder.encodeBitmap(
                valCodigoQR,
                BarcodeFormat.QR_CODE,
                400,
                400
            )

            imgCodigoQR.setImageBitmap(bitmap)
            Toast.makeText(this, "Código QR generado. Debe ser utilizado para generar otro.", Toast.LENGTH_SHORT).show()

            // Guardar el código QR en las preferencias compartidas
            val editor = sharedPreferences.edit()
            editor.putString("savedQRCode", valCodigoQR)
            editor.apply()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al generar el código QR: $e", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buscandoCambiosdeStatus() {
        docId?.let {
            codigosQRRef.document(it).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(ContentValues.TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    if (snapshot.getString("status") == "Utilizado") {
                        valCodigoQR = null
                        Toast.makeText(this, "El código QR ha sido utilizado. Buscando uno nuevo...", Toast.LENGTH_SHORT).show()

                        // Borrar el código QR de las preferencias compartidas
                        val editor = sharedPreferences.edit()
                        editor.remove("savedQRCode")
                        editor.apply()

                        buscandoCodigoQR()
                    }
                } else {
                    Log.d(ContentValues.TAG, "Current data: null")
                }
            }
        }
    }

    private fun buscandoNuevosCodigos() {
        codigosQRRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(ContentValues.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshots != null && !snapshots.isEmpty) {
                for (documentChange in snapshots.documentChanges) {
                    if (documentChange.type == DocumentChange.Type.ADDED) {
                        buscandoCodigoQR()
                        break
                    }
                }
            } else {
                Log.d(ContentValues.TAG, "Current data: null")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                auth.signOut()
                startActivity(Intent(this, Login::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}