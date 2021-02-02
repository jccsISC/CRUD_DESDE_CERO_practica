package com.jccsisc.loginfirebase.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jccsisc.loginfirebase.data.model.UserModel
import com.jccsisc.loginfirebase.utils.ProviderType
import com.jccsisc.loginfirebase.databinding.ActivityRegisterBinding
import com.jccsisc.loginfirebase.viewmodel.FirestoreViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var viewModel: FirestoreViewModel

    companion object {
        val authEmail: FirebaseAuth? by lazy { FirebaseAuth.getInstance() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Registrar"

        //instanciamos al viewModel
        viewModel = ViewModelProvider(this).get(FirestoreViewModel::class.java)

//        register()
        crearUsuario()
//        obtenerUsuario("uno@hotmail.com")
//        obtenerTodosDocumentos()
    }

    fun obtenerTodosDocumentos() {
        binding.apply {
            btnRegister.setOnClickListener {
                viewModel.getAllDocumentsViewModel()
            }
        }
    }

    fun obtenerUsuario(email: String) {
        binding.apply {
            btnRegister.setOnClickListener {
                viewModel.getUserDataViewModel(email)
            }
        }
    }

    fun crearUsuario() {
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
                    val userModel = UserModel(email, password, name, phone, city, ProviderType.EMAIL.name)
                    viewModel.setearDatos(userModel)
                } else {
                    Toast.makeText(
                        this@RegisterActivity, "Llene todos los campos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
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

    private fun showHome(email: String, provider: ProviderType) {
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
                Toast.makeText(
                    this@RegisterActivity,
                    "Se registró correctamente",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener {
                Toast.makeText(this@RegisterActivity, "Algo salió mal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}