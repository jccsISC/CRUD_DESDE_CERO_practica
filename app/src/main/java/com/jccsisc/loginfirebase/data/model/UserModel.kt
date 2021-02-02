package com.jccsisc.loginfirebase.data.model

import com.jccsisc.loginfirebase.utils.ProviderType

data class UserModel(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val phone: String = "",
    val city: String = "",
    val provider: String = ""
)
