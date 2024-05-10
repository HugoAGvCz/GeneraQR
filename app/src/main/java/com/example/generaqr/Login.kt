package com.example.generaqr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var btnIniciarSesion = findViewById<Button>(R.id.btnLogin)
        var email = findViewById<EditText>(R.id.edtTxtEmail)
        var password = findViewById<EditText>(R.id.edtTxtPassword)
        var btnOlvideContrasena = findViewById<Button>(R.id.btnPassword)
        var btnRegistrarse = findViewById<Button>(R.id.btnRegistrarse)

        btnRegistrarse.setOnClickListener {
            startActivity(Intent(this, Singnup::class.java))
        }
        btnOlvideContrasena.setOnClickListener {
            startActivity(Intent(this, ResetPassword::class.java))
        }
        btnIniciarSesion.setOnClickListener {

        }

    }
}