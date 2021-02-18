package com.jccsisc.loginfirebase.domain

import com.jccsisc.loginfirebase.data.model.UserModel
import com.jccsisc.loginfirebase.data.repo.FirebaseRepo

class FirestoreUseCaseInteractor {

    val repo = FirebaseRepo()

    fun setearUsuarioFirestore(userModel: UserModel) {

        repo.setUserData(userModel)
    }

    fun getUserDataInteractor(email: String) {
        repo.getUserDataRepo(email)
    }

    fun getALLDocumentsInteractor() {
        repo.getAllUserDocuments()
    }
}