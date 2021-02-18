package com.jccsisc.loginfirebase.data.repo

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jccsisc.loginfirebase.data.model.UserModel
import com.jccsisc.loginfirebase.ui.RegisterActivity

class FirebaseRepo {

    private val db = FirebaseFirestore.getInstance()

    //logica para insertar datos
    fun setUserData(userModel: UserModel) {
        val dataUser = hashMapOf(
            "name" to userModel.name,
            "phone" to userModel.phone,
            "city" to userModel.city,
            "provider" to userModel.provider
        )

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(userModel.email, userModel.password).addOnCompleteListener {
                if (it.isSuccessful) {
                    RegisterActivity.authEmail?.currentUser
                    db.collection("usuarios").document(userModel.email).set(dataUser).addOnCompleteListener {
                        if (it.isSuccessful) {
                            //mensaje ok
                        } else {
                            //ocurrió un error
                        }
                    }
                } else {
//                    showAlert()
                }
            }
    }

    fun getUserDataRepo(email: String) {

        db.collection("usuarios").document(email).get().addOnSuccessListener {
            if (it.exists()) {
                val name = it.getString("name")
                val phone = it.getString("phone")
                val city = it.getString("city")
                val provider = it.getString("provider")
                Log.d("DATOS ", "Datos: $name $phone $city $provider")
            }else {
                //No existe el documento x en la colección de usuarios
            }
        }
    }

    fun getAllUserDocuments() {

        val listUsers = mutableListOf<UserModel>()

        db.collection("usuarios").get().addOnSuccessListener { resultado ->
            for (documento in resultado) {
                val user = documento.toObject(UserModel::class.java) //mapea a modelo
                listUsers.add(user)
                Log.d("DOCUMENTS:", "$documento.id ${documento.data}")
            }

            Log.d("USUARIOS: ", "$listUsers")
        }
    }
}