package com.ipl.estg.mcif.eltonpastilha.controller

import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ipl.estg.mcif.eltonpastilha.MainActivity
import com.ipl.estg.mcif.eltonpastilha.api.API
import com.ipl.estg.mcif.eltonpastilha.api.ApiException
import com.ipl.estg.mcif.eltonpastilha.controller.fido2.Fido2Builder
import com.ipl.estg.mcif.eltonpastilha.db.Repository
import com.ipl.estg.mcif.eltonpastilha.model.CredentialAdapter
import com.ipl.estg.mcif.eltonpastilha.model.User
import com.ipl.estg.mcif.eltonpastilha.view.HomeActivity
import java.util.concurrent.Executor

class AuthController(private val application: AppCompatActivity) {
    private val executor: Executor = java.util.concurrent.Executors.newFixedThreadPool(64);

    // Get user from repository
    fun getCurrentUser() : User {
        return Repository.getInstance(application).getUser()
    }

    // Get credentials from the user account
    fun getCredentials(homeActivity: HomeActivity, credentials_view: RecyclerView) {
        executor.execute {
            try {
                val response = API(application).getCredentials()
                val credentials = Fido2Builder().parseUserCredentials(response)
                homeActivity.credentials = credentials
                credentials_view.post {
                    credentials_view.adapter = CredentialAdapter(homeActivity, credentials)
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Cannot call getCredentials", e)
            } finally {
                Log.d(TAG, "getCredentials()  -> finally()")
            }
        }
    }

    // Sign out and remove any saved information
    fun signOut() {
        Repository.getInstance(application).clean()
        application.startActivity(Intent(application, MainActivity::class.java))
    }

    // Remove a credential form the user account
    fun removeKey(
        credentialId: String,
    ) {
        executor.execute {
            try {
                API(application).removeCredential(credentialId)
                application.runOnUiThread { Toast.makeText(application, "Credential was deleted!", Toast.LENGTH_SHORT).show() }
                application.runOnUiThread { (application as (HomeActivity)).onClickButtonRefresh() }
            } catch (e: ApiException) {
                Log.e(TAG, "Cannot call removeKey", e)
            } finally {
                Log.d(TAG, "removeKey()  -> finally()")
            }
        }
    }
}