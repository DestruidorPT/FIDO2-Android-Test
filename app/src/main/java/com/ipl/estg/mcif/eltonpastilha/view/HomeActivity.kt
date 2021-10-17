package com.ipl.estg.mcif.eltonpastilha.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ipl.estg.mcif.eltonpastilha.R
import com.ipl.estg.mcif.eltonpastilha.controller.AuthController
import com.ipl.estg.mcif.eltonpastilha.controller.fido2.Fido2Service
import com.ipl.estg.mcif.eltonpastilha.db.Repository
import com.ipl.estg.mcif.eltonpastilha.model.Fido2Credential
import com.ipl.estg.mcif.eltonpastilha.model.User


class HomeActivity : AppCompatActivity() {
    var credentials = ArrayList<Fido2Credential>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        this.findViewById<View>(R.id.action_sign_out).setOnClickListener{
            onClickButtonSignOut()
        }
        val user = AuthController(this).getCurrentUser()
        this.findViewById<TextView>(R.id.welcome).text = "Welcome, " + user.username

        val credentials_view = this.findViewById<RecyclerView>(R.id.credentials_view)
        credentials_view.setHasFixedSize(true)
        credentials_view.layoutManager = LinearLayoutManager(this);
        AuthController(this).getCredentials(this, credentials_view)


        val button_refresh = this.findViewById<Button>(R.id.buttonRefresh)
        button_refresh.setOnClickListener {onClickButtonRefresh()}
        val add_credentials = this.findViewById<FloatingActionButton>(R.id.add)
        add_credentials.setOnClickListener {onClickButtonAddCredential()}

        Fido2Service.start(this) // Start FIDO2 on this activity
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Send the information to the FIDO2 service to be check if is for him
        if(!Fido2Service.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // Refresh the credential shown on the screen
    fun onClickButtonRefresh() {
        val credentials_view = this.findViewById<RecyclerView>(R.id.credentials_view)
        credentials_view.setHasFixedSize(true)
        credentials_view.layoutManager = LinearLayoutManager(this);
        AuthController(this).getCredentials(this, credentials_view)
    }

    // Sign out the user and clear all information in the app
    fun onClickButtonSignOut() {
        AuthController(this).signOut()
    }

    // Add new credential to the user
    fun onClickButtonAddCredential() {
        val user : User = Repository.getInstance(application).getUser()
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Want Platform (fingerprint) or you want Cross-Platform (NFC, USB, Bluetooth)?")
            .setCancelable(false)
            .setPositiveButton("Platform") { dialog, id ->
                Fido2Service.registerNewCredentialRequest(user, "platform")
            }
            .setNegativeButton("Cross-Platform") { dialog, id ->
                Fido2Service.registerNewCredentialRequest(user, "cross-platform")
            }
        val alert = builder.create()
        alert.show()
    }

    // Remove a credential from user account
    fun onClickButtonRemoveKey(credentialId: String, position: Int) {
        if(this.credentials.size > 1) {
            AuthController(this).removeKey(credentialId)
        } else {
            Toast.makeText(this, "You need to have at least one key!", Toast.LENGTH_SHORT).show()
        }
    }
}