package com.jccsisc.loginfirebase.viewmodel

import androidx.lifecycle.ViewModel
import com.jccsisc.loginfirebase.data.model.UserModel
import com.jccsisc.loginfirebase.domain.FirestoreUseCaseInteractor

class FirestoreViewModel : ViewModel() {

    /*setearDatos necesita de FirestoreUseCaseInteractor pero en este caso no lo vamos a inyectar
      directamente en el constructor del FirestoreViewModel lo vamos a instanciar
    * */

    val firestoreUseCaseInterantor = FirestoreUseCaseInteractor()

    fun setearDatos(userModel: UserModel) {

        firestoreUseCaseInterantor.setearUsuarioFirestore(userModel)
    }

    fun getUserDataViewModel(email: String) {
        firestoreUseCaseInterantor.getUserDataInteractor(email)
    }

    fun getAllDocumentsViewModel() {
        firestoreUseCaseInterantor.getALLDocumentsInteractor()
    }
}