package com.jccsisc.loginfirebase.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.jccsisc.loginfirebase.utils.ProviderType
import com.jccsisc.loginfirebase.R
import com.jccsisc.loginfirebase.data.model.Model
import com.jccsisc.loginfirebase.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var GOOGLE_SIGN_IN = 100
    private val authUser: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db = FirebaseFirestore.getInstance()
    lateinit var  analitycs: FirebaseAnalytics

    val authEmail = RegisterActivity.authEmail

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_LoginFirebase)
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("http://demo1466250.mockable.io/cities")
        startActivity(intent)

        //Lanzaremos un evento personalizado en analitycs
        analitycs = FirebaseAnalytics.getInstance(this)
        analytics()
        remoteConfig()
        pushNotificationShow()
        setUp()
        session()
    }


    private fun analytics() {
        val bundle = Bundle()
        bundle.putString("message", "Actual")
        analitycs.logEvent("MODELO", bundle)
    }

    private fun remoteConfig() {
        //Remote Config
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 30
        }

        val firebaseConfig = Firebase.remoteConfig
        firebaseConfig.setConfigSettingsAsync(configSettings)
        firebaseConfig.setDefaultsAsync(
            mapOf(
                "cerrar_sesion" to "Cerrar sesion",
                "show_button_log_out" to true,
                "show_text" to ""
            )
        )
    }

    private fun pushNotificationShow() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            it.result?.token?.let {
                Log.d("TOKEN", "Este es el token del dispositivo: ${it}")
            }
        }

        //asignando tema
        FirebaseMessaging.getInstance().subscribeToTopic("tutorial")

        //recuperando info
        val url = intent.getStringExtra("url")
        url?.let {
            Log.d("NOTIFICATION", "Este es la info recuperada: ${it}")
        }
    }

    private fun session() {

        val prefer: SharedPreferences = getSharedPreferences(getString(R.string.auth), MODE_PRIVATE)
        val email = prefer.getString("email", null)
        val provider = prefer.getString("provider", null)

        if (authUser.currentUser != null || authEmail?.currentUser != null) {
            showHome(email.toString(), ProviderType.valueOf(provider.toString()))
        }
    }

    private fun setUp() {
        title = "Login"
        binding.apply {

            val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())

            btnRegister.setOnClickListener {
                val register = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(register)
            }

            btnLogIn.setOnClickListener {
                val email = tieEmail.text.toString()
                val password = tiePassword.text.toString()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    progressBar.visibility = View.VISIBLE
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(email, password).addOnCompleteListener {
                            if (it.isSuccessful) {
                                progressBar.visibility = View.GONE
                                showHome(it.result?.user?.email ?: "", ProviderType.EMAIL)
                                analitycs.logEvent("operation_succes", Model.operation_success("Sesión iniciada"))
                                finish()
                            } else {
//                                showAlert()
                            }
                        }
                } else {
                    Toast.makeText(
                        this@LoginActivity, "Llene todos los campos",
                        Toast.LENGTH_SHORT
                    ).show()
                    analitycs.logEvent("user_error", Model.userError("Iniciar Sesion", "Llene todos los campos"))
                }
            }

            imvBtnGoogle.setOnClickListener {
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(true) //lo que hace es abrir por defecto con la cuenta anterior logeada
                        .build(), GOOGLE_SIGN_IN
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                Toast.makeText(this, "Bienvenido ${user!!.displayName} ${user!!.email} ", Toast.LENGTH_SHORT).show()
                val email = user!!.email
                db.collection("users").document(email.toString()).set(
                    hashMapOf(
                        "name" to user!!.displayName,
                        "city" to "México",
                        "provider" to ProviderType.GOOGLE
                    )
                )
                showHome(email.toString(), ProviderType.GOOGLE)
            } else {
               showAlert(response!!)
            }
        }
    }

    private fun showAlert(response: IdpResponse) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ha ocurrido un Error")
        builder.setMessage("algo salió mal ${response!!.error!!.errorCode}")
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
        finish()
    }
}
