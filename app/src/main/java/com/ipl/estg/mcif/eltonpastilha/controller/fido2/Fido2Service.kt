package com.ipl.estg.mcif.eltonpastilha.controller.fido2

import android.content.ContentValues
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.fido.Fido
import com.google.android.gms.fido.fido2.Fido2ApiClient
import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse
import com.google.android.gms.fido.fido2.api.common.ErrorCode
import com.ipl.estg.mcif.eltonpastilha.model.Fido2Codes
import com.ipl.estg.mcif.eltonpastilha.model.User


object  Fido2Service {
    private lateinit var application: AppCompatActivity
    private lateinit var fido2ApiClient: Fido2ApiClient

    /// To initiate the FIDO2 Service with the activity given
    fun start(application: AppCompatActivity) {
        this.application = application
        this.fido2ApiClient = Fido.getFido2ApiClient(application) // Start FIDO2 Client from the Android
        Fido2Communication.start(this.application, this.fido2ApiClient)
    }

    /// Start the process of login
    fun signInUserRequest(user: User) {
        Fido2Communication.signInUserRequest(user)
    }

    /// Start the process of register new user
    fun registerNewUserRequest(user: User, authenticatorAttachment: String) {
        Fido2Communication.registerNewUserRequest(user, authenticatorAttachment)
    }

    /// Start the process of register new credential to the user
    fun registerNewCredentialRequest(user: User, authenticatorAttachment: String) {
        Fido2Communication.registerNewCredentialRequest(user, authenticatorAttachment)
    }

    /// To receive the result of FIDO2 API
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) : Boolean {
        when (requestCode) {
            Fido2Codes.REQUEST_LOGIN_USER.ordinal -> {
                val errorExtra = data?.getByteArrayExtra(Fido.FIDO2_KEY_ERROR_EXTRA)
                if (errorExtra != null) {
                    handleErrorCode(errorExtra)
                } else if (resultCode != AppCompatActivity.RESULT_OK) {
                    Toast.makeText(application, "cancelled", Toast.LENGTH_SHORT).show()
                } else {
                    if (data != null) {
                        Fido2Communication.signInUserResponse(data)
                    }
                }
            }
            Fido2Codes.REQUEST_REGISTER_USER.ordinal -> {
                val errorExtra = data?.getByteArrayExtra(Fido.FIDO2_KEY_ERROR_EXTRA)
                if (errorExtra != null) {
                    handleErrorCode(errorExtra)
                } else if (resultCode != AppCompatActivity.RESULT_OK) {
                    Toast.makeText(application, "cancelled", Toast.LENGTH_SHORT).show()
                } else {
                    if (data != null) {
                        Fido2Communication.registerNewUserResponse(data)
                    }
                }
            }
            Fido2Codes.REQUEST_REGISTER_NEW_KEY.ordinal -> {
                val errorExtra = data?.getByteArrayExtra(Fido.FIDO2_KEY_ERROR_EXTRA)
                if (errorExtra != null) {
                    handleErrorCode(errorExtra)
                } else if (resultCode != AppCompatActivity.RESULT_OK) {
                    Toast.makeText(application, "cancelled", Toast.LENGTH_SHORT).show()
                } else {
                    if (data != null) {
                        Fido2Communication.registerNewCredentialResponse(data)
                    }
                }
            }
            else -> {return false}
        }
        return true
    }

    /// Handle Android FIDO2 error and display the message associated with the error
    private fun handleErrorCode(errorExtra: ByteArray) {
        val error : AuthenticatorErrorResponse = AuthenticatorErrorResponse.deserializeFromBytes(errorExtra)

        var errorMessage: String
        if (error.errorCode === ErrorCode.ABORT_ERR) {
            // The operation was aborted.
            errorMessage = "Fido2 Abort Error"
        } else if (error.errorCode === ErrorCode.TIMEOUT_ERR) {
            // The operation timed out.
            errorMessage = "Fido2 Timeout Error"
        } else if (error.errorCode === ErrorCode.ATTESTATION_NOT_PRIVATE_ERR) {
            // The authenticator violates the privacy requirements of the AttestationStatementType it is using.
            errorMessage = "Fido2 Privacy Error"
        } else if (error.errorCode === ErrorCode.CONSTRAINT_ERR) {
            // A mutation operation in a transaction failed because a constraint was not satisfied.
            errorMessage = "Fido2 Something Went Wrong"
        } else if (error.errorCode === ErrorCode.DATA_ERR) {
            // Provided data is inadequate.
            errorMessage = "Fido2 Server Data Fail"
        } else if (error.errorCode === ErrorCode.ENCODING_ERR) {
            // The encoding operation (either encoded or decoding) failed.
            errorMessage = "Fido2 Something WentWrong"
        } else if (error.errorCode === ErrorCode.INVALID_STATE_ERR) {
            // The object is in an invalid state.
            errorMessage = "Fido2 Something Went Wrong"
        } else if (error.errorCode === ErrorCode.NETWORK_ERR) {
            // A network error occurred.
            errorMessage = "Fido2 Network Fail"
        } else if (error.errorCode === ErrorCode.NOT_ALLOWED_ERR) {
            // The request is not allowed by the user agent or the platform in the current context, possibly because the user denied permission.
            errorMessage = "Fido2 No  Permission"
        } else if (error.errorCode === ErrorCode.NOT_SUPPORTED_ERR) {
            // The operation is not supported.
            errorMessage = "Fido2 Not Supported Error"
        } else if (error.errorCode === ErrorCode.SECURITY_ERR) {
            // The operation is insecure.
            errorMessage = "Fido2 Security Error"
        } else if (error.errorCode === ErrorCode.UNKNOWN_ERR) {
            // The operation failed for an unknown transient reason.
            errorMessage = "Fido2 Something Went Wrong"
        } else {
            // Other future errors
            errorMessage = "Fido2 Something Went Wrong"
        }
        Toast.makeText(application, errorMessage, Toast.LENGTH_LONG).show()
        Log.e(ContentValues.TAG, errorMessage)
    }
}