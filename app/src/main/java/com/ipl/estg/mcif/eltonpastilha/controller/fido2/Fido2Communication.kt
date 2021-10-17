package com.ipl.estg.mcif.eltonpastilha.controller.fido2

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.fido.Fido
import com.google.android.gms.fido.fido2.Fido2ApiClient
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAssertionResponse
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAttestationResponse
import com.ipl.estg.mcif.eltonpastilha.api.API
import com.ipl.estg.mcif.eltonpastilha.api.ApiException
import com.ipl.estg.mcif.eltonpastilha.api.toBase64
import com.ipl.estg.mcif.eltonpastilha.db.Repository
import com.ipl.estg.mcif.eltonpastilha.model.Fido2Codes
import com.ipl.estg.mcif.eltonpastilha.model.User
import com.ipl.estg.mcif.eltonpastilha.view.HomeActivity
import com.ipl.estg.mcif.eltonpastilha.view.LoginActivity
import java.util.concurrent.Executor

/// Request and responses to the API and to the local FIDO2 API
object Fido2Communication {
    private val executor: Executor = java.util.concurrent.Executors.newFixedThreadPool(64);
    private lateinit var application: AppCompatActivity
    private lateinit var fido2ApiClient: Fido2ApiClient

    /// To initiate the FIDO2 Service with the activity given
    fun start(application: AppCompatActivity, fido2ApiClient: Fido2ApiClient) {
        this.application = application
        this.fido2ApiClient = fido2ApiClient
    }

    /// Start the process of login
    fun signInUserRequest(user: User) {
        executor.execute {
            fido2ApiClient?.let { client ->
                try {
                    // Get information from repository
                    val instance : Repository = Repository.getInstance(application)
                    instance.setUser(user)
                    //Ask to the API for the challenge
                    val response =  API(application).signinRequest(user)
                    // Retrieve the information, using the builder and the class of FIDO2 .
                    val (options, challenge) = Fido2Builder().parsePublicKeyCredentialRequestOptions(response)
                    // Save the user and challenge string.
                    instance.setUserAndChallenge(user, challenge)
                    // Create an Intent to start the local FIDO2 API, for the user to sign the challenge.
                    val task = client.getSignPendingIntent(options)
                    task.addOnSuccessListener { pendingIntent: PendingIntent ->
                        try {
                            application.startIntentSenderForResult(
                                    pendingIntent.intentSender,
                                    Fido2Codes.REQUEST_LOGIN_USER.ordinal,
                                    null, // fillInIntent,
                                    0, // flagsMask,
                                    0, // flagsValue,
                                    0 //extraFlags
                            )
                        } catch (e: Exception) {
                            Log.e(ContentValues.TAG, "Fail to obtain the signature of the challenge", e)
                        }
                    }.addOnFailureListener { ex -> Log.e(ContentValues.TAG, "signinRequest()  -> onFailure()", ex) }
                    .addOnCompleteListener { Log.d(ContentValues.TAG, "signinRequest()  -> onComplete()") }
                }
                finally {
                    Log.d(ContentValues.TAG, "signinRequest()  -> finally()")
                }
            }
        }
    }

    /// Complete the process of login
    fun signInUserResponse(data: Intent) {
        executor.execute {
            try {
                // Get information from repository
                val instance : Repository = Repository.getInstance(application)
                val user = instance.getUser()

                // Extract the AuthenticatorAssertionResponse, where contains the response and the challenge signature.
                val response = AuthenticatorAssertionResponse.deserializeFromBytes(
                        data.getByteArrayExtra(Fido.FIDO2_KEY_RESPONSE_EXTRA)
                )
                // Send the information to the server to sign in
                API(application).signinResponse(user, response)
                // Show the result
                application.runOnUiThread { Toast.makeText(application, "Login User\"${user.username}\" is successful!", Toast.LENGTH_SHORT).show() }
                application.startActivity(Intent(application, HomeActivity::class.java))
            } catch (e: ApiException) {
                Log.e(ContentValues.TAG, "Cannot call signinResponse", e)
            } finally {
                Log.d(ContentValues.TAG, "signinResponse()  -> finally()")
            }
        }
    }

    /// Start the process of register new user
    fun registerNewUserRequest(user: User, authenticatorAttachment: String) {
        executor.execute {
            fido2ApiClient?.let { client ->
                try {
                    // Get information from repository
                    val instance : Repository = Repository.getInstance(application)
                    //Ask to the API for the challenge
                    val response = API(application).registerRequest(user, authenticatorAttachment)
                    // Retrieve the information, using the builder and the class of FIDO2 .
                    val (options, challenge) = Fido2Builder().parsePublicKeyCredentialCreationOptions(response)
                    // Save the user, challenge string and the option (Platform or Cross-Platform).
                    instance.setAll(User(user.username, options.user.id.toBase64()), challenge, authenticatorAttachment)
                    // Create an Intent to start the local FIDO2 API, for the user to sign the challenge.
                    val task = client.getRegisterPendingIntent(options)
                    task.addOnSuccessListener { pendingIntent: PendingIntent ->
                        try {
                            application.startIntentSenderForResult(
                                    pendingIntent.intentSender,
                                    Fido2Codes.REQUEST_REGISTER_USER.ordinal,
                                    null, // fillInIntent,
                                    0, // flagsMask,
                                    0, // flagsValue,
                                    0 //extraFlags
                            )
                        } catch (e: Exception) {
                            Log.e(ContentValues.TAG, "Fail to obtain the signature of the challenge", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "Cannot call registerRequest", e)
                } finally {
                    Log.d(ContentValues.TAG, "registerRequest()  -> finally()")
                }
            }
        }
    }

    /// Complete the process of register new user
    fun registerNewUserResponse(data: Intent) {
        println("registerNewUserResponse")
        executor.execute {
            try {
                // Get information from repository
                val instance : Repository = Repository.getInstance(application)
                val user = instance.getUser()
                val authenticatorAttachmentSelected = instance.getPublicKeyTransportSelected()

                // Extract the AuthenticatorAttestationResponse, where contains the response, credential and the challenge signature.
                val response = AuthenticatorAttestationResponse.deserializeFromBytes(
                        data.getByteArrayExtra(Fido.FIDO2_KEY_RESPONSE_EXTRA)!!
                )
                // Send the information to the server
                API(application).registerResponse(user, authenticatorAttachmentSelected, response)
                // Show the result
                application.runOnUiThread { Toast.makeText(application, "User\"${user.username}\" is registered!", Toast.LENGTH_SHORT).show() }
                application.startActivity(Intent(application, LoginActivity::class.java))
            } catch (e: ApiException) {
                Log.e(ContentValues.TAG, "Cannot call registerResponse", e)
            } finally {
                Log.d(ContentValues.TAG, "registerResponse()  -> finally()")
            }
        }
    }


    /// Start the process of register new credential to the user
    fun registerNewCredentialRequest(user: User, authenticatorAttachment: String) {
        executor.execute {
            fido2ApiClient?.let { client ->
                try {
                    // Get information from repository
                    val instance : Repository = Repository.getInstance(application)
                    //Ask to the API for the challenge
                    val response = API(application).registerNewCredentialRequest(user, authenticatorAttachment)
                    // Retrieve sign-in options from the server.
                    val (options, challenge) = Fido2Builder().parsePublicKeyCredentialCreationOptions(response)
                    // Save the option (Platform or Cross-Platform) and challenge string.
                    instance.setChallengeAndPublicKeyTransportSelected(challenge, authenticatorAttachment)
                    // Create an Intent to start the local FIDO2 API, for the user to sign the challenge.
                    val task = client.getRegisterPendingIntent(options)
                    task.addOnSuccessListener { pendingIntent: PendingIntent ->
                        try {
                            application.startIntentSenderForResult(
                                    pendingIntent.intentSender,
                                    Fido2Codes.REQUEST_REGISTER_NEW_KEY.ordinal,
                                    null, // fillInIntent,
                                    0, // flagsMask,
                                    0, // flagsValue,
                                    0 //extraFlags
                            )
                        } catch (e: Exception) {
                            Log.e(ContentValues.TAG, "Fail to obtain the signature of the challenge", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "Cannot call registerNewCredentialRequest", e)
                } finally {
                    Log.d(ContentValues.TAG, "registerNewCredentialRequest()  -> finally()")
                }
            }
        }
    }

    /// Complete the process of register new credential to the user
    fun registerNewCredentialResponse(data: Intent) {
        executor.execute {
            try {
                // Get information from repository
                val instance : Repository = Repository.getInstance(application)
                val user = instance.getUser()
                val authenticatorAttachmentSelected = instance.getPublicKeyTransportSelected()

                // Extract the AuthenticatorAttestationResponse, where contains the response, credential and the challenge signature.
                val response = AuthenticatorAttestationResponse.deserializeFromBytes(
                        data.getByteArrayExtra(Fido.FIDO2_KEY_RESPONSE_EXTRA)!!
                )
                // Send the information to the server
                API(application).registerNewCredentialResponse(user, authenticatorAttachmentSelected, response)
                // Show the result
                application.runOnUiThread { Toast.makeText(application, "New Credential is registered!", Toast.LENGTH_SHORT).show() }
                application.runOnUiThread { (application as (HomeActivity)).onClickButtonRefresh() }
            } catch (e: ApiException) {
                Log.e(ContentValues.TAG, "Cannot call registerResponse", e)
            } finally {
                Log.d(ContentValues.TAG, "registerResponse()  -> finally()")
            }
        }
    }
}