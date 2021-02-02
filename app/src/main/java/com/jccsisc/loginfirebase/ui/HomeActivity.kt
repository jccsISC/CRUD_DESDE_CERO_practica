package com.jccsisc.loginfirebase.ui

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.jccsisc.loginfirebase.R
import com.jccsisc.loginfirebase.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var update: Boolean = false
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")
        showData(email ?: "", provider ?: "")

        //guardando datos
        val prefer: SharedPreferences.Editor? =
            getSharedPreferences(getString(R.string.auth), MODE_PRIVATE).edit()
        prefer?.putString("email", email)
        prefer?.putString("provider", provider)
        prefer?.apply()

        getData(email.toString())

        //Remote
        getRemoteConfig()
    }

    private fun getData(email: String) {
        binding.apply {
            db.collection("users").document(email).get().addOnSuccessListener {
                tieName.setText(it.get("name") as String?)
                tiePhone.setText(it.get("phone") as String?)
                tieCity.setText(it.get("city") as String?)
            }
        }
    }

    private fun getRemoteConfig() {
        //RemoteConfig
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val showLogOutButton = Firebase.remoteConfig.getBoolean("show_button_log_out")
                val txtBtnLogOut = Firebase.remoteConfig.getString("cerrar_sesion")
                val showText = Firebase.remoteConfig.getString("show_text")

                binding.apply {
                    if (showLogOutButton) {
                        btnLogOut.visibility = View.VISIBLE
                    } else {
                        btnLogOut.visibility = View.INVISIBLE
                    }

                    btnLogOut.text = txtBtnLogOut
                    tvRemoteConf.text = showText
                }
            }
        }
    }

    private fun isUpdate() {
        binding.apply {
            btnUpdate.text = "Actualizar"
            btnUpdate.setBackgroundColor(getColor(R.color.colorPrimaryDark))
            tilName.isEnabled = true
            tieName.setTextColor(getColor(R.color.purple))
            tilPhone.isEnabled = true
            tiePhone.setTextColor(getColor(R.color.purple))
            tilCity.isEnabled = true
            tieCity.setTextColor(getColor(R.color.purple))
        }
    }

    private fun isUpdateFinish() {
        binding.apply {
            btnUpdate.text = "Editar"
            btnUpdate.setBackgroundColor(getColor(R.color.colorAccent))
            tilName.isEnabled = false
            tieName.setTextColor(getColor(R.color.gray))
            tilPhone.isEnabled = false
            tiePhone.setTextColor(getColor(R.color.gray))
            tilCity.isEnabled = false
            tieCity.setTextColor(getColor(R.color.gray))
        }
    }

    private fun showData(email: String, provider: String) {
        binding.apply {
            title = email
            tvProvider.text = provider

            btnUpdate.setOnClickListener {
                update = !update

                if (update) { isUpdate() }

                if (!update) {
                    db.collection("users").document(email).set(
                        hashMapOf(
                            "name" to tieName.text.toString(),
                            "phone" to tiePhone.text.toString(),
                            "city" to tieCity.text.toString(),
                            "provider" to provider
                        )
                    ).addOnCompleteListener {
                        isUpdateFinish()
                        Toast.makeText(
                            this@HomeActivity,
                            "Se actualizó correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    isUpdateFinish()
                }
            }

            btnLogOut.setOnClickListener {
                val prefer: SharedPreferences.Editor? =
                    getSharedPreferences(getString(R.string.auth), MODE_PRIVATE).edit()
                prefer?.clear()
                prefer?.apply()

                RegisterActivity.authEmail?.signOut()

                if (RegisterActivity.authEmail != null) {
                    FirebaseAuth.getInstance().signOut()
                    onBackPressed()
                }

                binding.progressBar.visibility = View.VISIBLE
                binding.btnLogOut.isEnabled = false
                AuthUI.getInstance().signOut(this@HomeActivity).addOnSuccessListener {
                    startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                    finish()
                }.addOnFailureListener {
                    binding.btnLogOut.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        baseContext, "Ocurrió un error ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}