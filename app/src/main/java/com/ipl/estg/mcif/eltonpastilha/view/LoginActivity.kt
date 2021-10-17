package com.ipl.estg.mcif.eltonpastilha.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.ipl.estg.mcif.eltonpastilha.R
import com.ipl.estg.mcif.eltonpastilha.controller.fido2.Fido2Service
import com.ipl.estg.mcif.eltonpastilha.model.User

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Fido2Service.start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Send the information to the FIDO2 service to be check if is for him
        if(!Fido2Service.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // Submit the information to the server to login the user
    fun onClickButtonLogin(view: View) {
        var editTextUsername = this.findViewById<EditText>(R.id.editTextUsername)
        if(editTextUsername.text.isNullOrEmpty()) {
            Toast.makeText(this, "Username cannot be empty!", Toast.LENGTH_SHORT).show()
        } else {
            Fido2Service.signInUserRequest(User(editTextUsername.text.toString()))
        }
    }

    // Go to the previous page
    fun onClickButtonGoBack(view: View) {
        finish()
    }
}