package com.ipl.estg.mcif.eltonpastilha.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import com.ipl.estg.mcif.eltonpastilha.R
import com.ipl.estg.mcif.eltonpastilha.controller.fido2.Fido2Service
import com.ipl.estg.mcif.eltonpastilha.model.User

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        Fido2Service.start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Send the information to the FIDO2 service to be check if is for him
        if(!Fido2Service.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // Submit the information to the server to register the new user
    fun onClickButtonRegister(view: View) {
        var editTextUsername = this.findViewById<EditText>(R.id.editTextUsername)
        var selectedId = this.findViewById<RadioGroup>(R.id.radioGroup).checkedRadioButtonId;
        var authenticatorAttachment = when(selectedId) {
            R.id.radioButtonPlatform -> "platform"
            R.id.radioButtonCrossPlatform -> "cross-platform"
            else -> ""
        }
        if(editTextUsername.text.isNullOrEmpty()) {
            Toast.makeText(this, "Username cannot be empty!", Toast.LENGTH_SHORT).show()
        } else if (authenticatorAttachment.isNullOrEmpty()) {
            Toast.makeText(this, "You need to choice the way you want authenticate!", Toast.LENGTH_SHORT).show()
        }else {
            Fido2Service.registerNewUserRequest(User(editTextUsername.text.toString()), authenticatorAttachment)
        }
    }

    // Go to the previous page
    fun onClickButtonGoBack(view: View) {
        finish()
    }
}