package com.example.generaqr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth

class Singnup : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singnup)

        auth = Firebase.auth

        var email = findViewById<EditText>(R.id.txtEmail)
        var password = findViewById<EditText>(R.id.txtPassword)
        var confPassword = findViewById<EditText>(R.id.txtConfPassword)
        var btnRegistrarte = findViewById<Button>(R.id.btnSign)
        var btnRegresar = findViewById<Button>(R.id.btnRegresar)

        btnRegistrarte.setOnClickListener {
            if (revisarCampos(email.text.toString(), password.text.toString(), confPassword.text.toString())) {
                auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString()).addOnCompleteListener {
                    task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Usuario creado!", Toast.LENGTH_LONG).show()
                    } else {
                        if (task.exception is FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "Este correo ya fue registrado!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Error: " + task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        btnRegresar.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
    }

    private fun revisarCampos(email: String, password: String, confPassword: String): Boolean {
        if (email == "") {
            Toast.makeText(this, "El campo correo es requerido", Toast.LENGTH_LONG).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "El correo no es válido", Toast.LENGTH_LONG).show()
            return false
        }

        if (password == "") {
            Toast.makeText(this, "El campo contraseña es requerido", Toast.LENGTH_LONG).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_LONG).show()
            return false
        }
        if (confPassword == "") {
            Toast.makeText(this, "El campo confirma tu contraseña es requerido", Toast.LENGTH_LONG).show()
            return false
        }
        if (!password.equals(confPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }
}