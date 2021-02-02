package com.jccsisc.loginfirebase.data.model

import android.os.Bundle


object Model {
    fun userError(flow: String, error_message: String) = Bundle().apply {
        putString("flow", flow)
        putString("error_message", error_message)
    }

    fun operation_success(flow: String) = Bundle().apply {
        putString("flow", flow)
    }

    fun save(screen_name: String, previous_screen: String, flow: String, label: String) = Bundle().apply {
        putString("screen_name", screen_name)
        putString("previous_screen", previous_screen)
        putString("flow", flow)
        putString("label", label)
    }
}
