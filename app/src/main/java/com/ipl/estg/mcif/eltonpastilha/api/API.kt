package com.ipl.estg.mcif.eltonpastilha.api

import android.util.JsonWriter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.fido.fido2.api.common.*
import com.ipl.estg.mcif.eltonpastilha.model.User
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.StringWriter
import java.util.concurrent.TimeUnit


/**
 * Request and Responses to the server API.
 */

class API(application: AppCompatActivity) {
    private val base_url = "https://mcif.glitch.me/api/auth"
    private val client = OkHttpClient.Builder()
        .addInterceptor(AddHeaderInterceptor())
        .retryOnConnectionFailure(true)
        .setCookieStore(application)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .connectTimeout(180, TimeUnit.SECONDS)
        .build()


    /**
     * Request to sign in and return the keys and the challenge
     */
    fun signinRequest(
            user: User
    ): ResponseBody {
        val call = client.newCall(
                Request.Builder()
                        .url(
                                buildString {
                                    append("$base_url/signinUserRequest")
                                }
                        )
                        .method("POST", jsonRequestBody {
                            name("username").value(user.username)
                        })
                        .build()
        )
        val response = call.execute()
        if (!response.isSuccessful) {
            ApiException("Error calling /signinUserRequest")
        }
        return response.body ?: throw ApiException("Empty response from /signinUserRequest")
    }

    /**
     * Send the signed challenge to auth
     */
    fun signinResponse(
            user: User,
            response: AuthenticatorAssertionResponse
    ): ResponseBody {
        val rawId = response.keyHandle.toBase64()
        val call = client.newCall(
                Request.Builder()
                        .url("$base_url/signinUserResponse")
                        .method("POST", jsonRequestBody {
                            name("username").value(user.username)
                            name("id").value(rawId)
                            name("type").value(PublicKeyCredentialType.PUBLIC_KEY.toString())
                            name("rawId").value(rawId)
                            name("response").objectValue {
                                name("clientDataJSON").value(
                                        response.clientDataJSON.toBase64()
                                )
                                name("authenticatorData").value(
                                        response.authenticatorData.toBase64()
                                )
                                name("signature").value(
                                        response.signature.toBase64()
                                )
                                name("userHandle").value(
                                        response.userHandle?.toBase64() ?: ""
                                )
                            }
                        })
                        .build()
        )
        val apiResponse = call.execute()
        if (!apiResponse.isSuccessful) {
            ApiException("Error calling /signinUserResponse")
        }
        apiResponse.request.headers
        return apiResponse.body ?: throw ApiException("Empty response from /signinUserResponse")
    }


    /**
     * Request to register a new user, return the keys and the challenge
     */
    fun registerRequest(user: User, authenticatorAttachment: String): ResponseBody {
        val call = client.newCall(
                Request.Builder()
                        .url("$base_url/registerNewUserRequest")
                        .method("POST", jsonRequestBody {
                            name("username").value(user.username)
                            name("attestation").value("none")
                            name("authenticatorSelection").objectValue {
                                name("authenticatorAttachment").value(authenticatorAttachment)
                                name("userVerification").value("required")
                            }
                        })
                        .build()
        )
        val response = call.execute()
        if (!response.isSuccessful) {
            ApiException("Error calling /registerNewUserRequest")
        }
        return response.body ?: throw ApiException("Empty response from /registerNewUserRequest")
    }



    /**
     * Send the signed challenge and the credential, to registry the user with key
     */
    fun registerResponse(
        user: User,
        authenticatorAttachmentSelected: String,
        response: AuthenticatorAttestationResponse
    ): String {
        val rawId = response.keyHandle.toBase64()
        val call = client.newCall(
            Request.Builder()
                .url("$base_url/registerNewUserResponse")
                .method("POST", jsonRequestBody {
                    name("userid").value(user.id)
                    name("username").value(user.username)
                    name("transports").value(authenticatorAttachmentSelected)
                    name("id").value(rawId)
                    name("type").value(PublicKeyCredentialType.PUBLIC_KEY.toString())
                    name("rawId").value(rawId)
                    name("response").objectValue {
                        name("clientDataJSON").value(
                            response.clientDataJSON.toBase64()
                        )
                        name("attestationObject").value(
                            response.attestationObject.toBase64()
                        )
                    }
                })
                .build()
        )
        val apiResponse = call.execute()
        if (!apiResponse.isSuccessful) {
            ApiException("Error calling /registerNewUserResponse")
        }
        val body = apiResponse.body ?: throw ApiException("Empty response from /registerNewUserResponse")
        return body.string()
    }

    /**
     * Request to register a new key, return the challenge to sign
     */
    fun registerNewCredentialRequest(user: User, authenticatorAttachment: String): ResponseBody {
        val call = client.newCall(
            Request.Builder()
                .url("$base_url/registerRequest")
                .method("POST", jsonRequestBody {
                    name("username").value(user.username)
                    name("attestation").value("none")
                    name("authenticatorSelection").objectValue {
                        name("authenticatorAttachment").value(authenticatorAttachment)
                        name("userVerification").value("required")
                    }
                })
                .build()
        )
        val apiResponse = call.execute()
        if (!apiResponse.isSuccessful) {
            ApiException("Error calling /registerRequest")
        }
        return apiResponse.body ?: throw ApiException("Empty response from /registerRequest")
    }

    /**
     * Send the signed challenge and the credential, to registry a new key to the user
     */
    fun registerNewCredentialResponse(
            user: User,
            authenticatorAttachmentSelected: String,
            response: AuthenticatorAttestationResponse
    ): String {
        val rawId = response.keyHandle.toBase64()
        val call = client.newCall(
                Request.Builder()
                        .url("$base_url/registerResponse")
                        .method("POST", jsonRequestBody {
                            name("userid").value(user.id)
                            name("username").value(user.username)
                            name("transports").value(authenticatorAttachmentSelected)
                            name("id").value(rawId)
                            name("type").value(PublicKeyCredentialType.PUBLIC_KEY.toString())
                            name("rawId").value(rawId)
                            name("response").objectValue {
                                name("clientDataJSON").value(
                                        response.clientDataJSON.toBase64()
                                )
                                name("attestationObject").value(
                                        response.attestationObject.toBase64()
                                )
                            }
                        })
                        .build()
        )
        val apiResponse = call.execute()
        if (!apiResponse.isSuccessful) {
            ApiException("Error calling /registerResponse")
        }
        val body = apiResponse.body ?: throw ApiException("Empty response from /registerResponse")
        return body.string()
    }

    /**
     * Request the keys user has
     */
    fun getCredentials(): ResponseBody {
        val call = client.newCall(
                Request.Builder()
                        .url("$base_url/getKeys")
                        .method("POST", jsonRequestBody {})
                        .build()
        )
        val response = call.execute()
        if (!response.isSuccessful) {
            ApiException("Error calling /getKeys")
        }
        return response.body ?: throw ApiException("Empty response from /getKeys")
    }

    /**
     * Remove one key from the user account
     */
    fun removeCredential(credentialId: String) {
        val call = client.newCall(
                Request.Builder()
                        .url("$base_url/removeKey?credId=$credentialId")
                        .method("POST", jsonRequestBody {})
                        .build()
        )
        val response = call.execute()
        if (!response.isSuccessful) {
            ApiException("Error calling /removeKey")
        }
    }

    /**
     * Get the body from the response
     */
    private fun jsonRequestBody(body: JsonWriter.() -> Unit): RequestBody {
        val output = StringWriter()
        JsonWriter(output).use { writer ->
            writer.beginObject()
            writer.body()
            writer.endObject()
        }
        return output.toString().toRequestBody("application/json".toMediaTypeOrNull())
    }

    /**
     * Get the values in one key of json
     */
    private fun JsonWriter.objectValue(body: JsonWriter.() -> Unit) {
        beginObject()
        body()
        endObject()
    }
}
