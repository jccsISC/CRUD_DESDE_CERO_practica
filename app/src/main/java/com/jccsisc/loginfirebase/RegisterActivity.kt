package com.jccsisc.loginfirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jccsisc.loginfirebase.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val db = FirebaseFirestore.getInstance()

    companion object {
        val authEmail: FirebaseAuth? by lazy { FirebaseAuth.getInstance() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Registrar"

        register()
    }

    private fun register() {
        binding.apply {

            btnRegister.setOnClickListener {
                val name = tieName.text.toString().trim()
                val phone = tiePhone.text.toString().trim()
                val city = tieCity.text.toString().trim()
                val email = tieEmail.text.toString().trim()
                val password = tiePassword.text.toString().trim()

                if (name.isNotEmpty() && phone.isNotEmpty() && city.isNotEmpty()
                    && email.isNotEmpty() && password.isNotEmpty()
                ) {
                    progressBar.visibility = View.VISIBLE
                    FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                            if (it.isSuccessful) {
                                progressBar.visibility = View.GONE
                                authEmail?.currentUser
                                showHome(it.result?.user?.email ?: "", ProviderType.EMAIL)
                                finish()
                            } else {
                                showAlert()
                            }
                        }
                } else {
                    Toast.makeText(
                        this@RegisterActivity, "Llene todos los campos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ha ocurrido un Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
        binding.progressBar.visibility = View.GONE
    }

    private fun showHome( email: String, provider: ProviderType) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(intent)

        binding.apply {
            db.collection("users").document(email).set(
                hashMapOf(
                    "name" to tieName.text.toString(), "phone" to tiePhone.text.toString(),
                    "city" to tieCity.text.toString(), "provider" to provider
                )
            ).addOnCompleteListener {
                Toast.makeText(this@RegisterActivity, "Se registró correctamente", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this@RegisterActivity, "Algo salió mal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}