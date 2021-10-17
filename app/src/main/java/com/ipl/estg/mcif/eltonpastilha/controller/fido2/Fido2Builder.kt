package com.ipl.estg.mcif.eltonpastilha.controller.fido2

import android.util.JsonReader
import com.google.android.gms.fido.fido2.api.common.*
import com.ipl.estg.mcif.eltonpastilha.api.ApiException
import com.ipl.estg.mcif.eltonpastilha.api.decodeBase64
import com.ipl.estg.mcif.eltonpastilha.model.Fido2Credential
import okhttp3.ResponseBody


/*
* Build the objects of FIDO2 to call the local FIDO2 API
*/
class Fido2Builder {

    /// Build the Information for Sign In using local FIDO2 API
    fun parsePublicKeyCredentialRequestOptions(
            body: ResponseBody
    ): Pair<PublicKeyCredentialRequestOptions, String> {
        val builder = PublicKeyCredentialRequestOptions.Builder()
        var challenge: String? = null
        JsonReader(body.byteStream().bufferedReader()).use { reader ->
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "challenge" -> {
                        val c = reader.nextString()
                        challenge = c
                        builder.setChallenge(c.decodeBase64())
                    }
                    "userVerification" -> reader.skipValue()
                    "allowCredentials" -> builder.setAllowList(parseCredentialDescriptors(reader))
                    "rpId" -> builder.setRpId(reader.nextString())
                    "timeout" -> builder.setTimeoutSeconds(reader.nextDouble())
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
        }
        return builder.build() to challenge!!
    }

    /// Build the information for Regist a New FIDO2 Key using local FIDO2 API
    fun parsePublicKeyCredentialCreationOptions(
            body: ResponseBody,
    ): Pair<PublicKeyCredentialCreationOptions, String> {
        val builder = PublicKeyCredentialCreationOptions.Builder()
        var challenge: String? = null
        JsonReader(body.byteStream().bufferedReader()).use { reader ->
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "user" -> builder.setUser(parseUser(reader))
                    "challenge" -> {
                        val c = reader.nextString()
                        builder.setChallenge(c.decodeBase64())
                        challenge = c
                    }
                    "pubKeyCredParams" -> builder.setParameters(parseParameters(reader))
                    "timeout" -> builder.setTimeoutSeconds(reader.nextDouble())
                    "attestation" -> reader.skipValue() // Unusedp
                    "excludeCredentials" -> builder.setExcludeList(
                            parseCredentialDescriptors(reader)
                    )
                    "authenticatorSelection" -> builder.setAuthenticatorSelection(
                            parseSelection(reader)
                    )
                    "rp" -> builder.setRp(parseRp(reader))
                }
            }
            reader.endObject()
        }
        return builder.build() to challenge!!
    }

    /// Build the part of the information where contains the server information
    fun parseRp(reader: JsonReader): PublicKeyCredentialRpEntity {
        var id: String? = null
        var name: String? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextString()
                "name" -> name = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return PublicKeyCredentialRpEntity(id!!, name!!, /* icon */ null)
    }

    /// Build the part of information where contains the option to require the FIDO2 to ask for confirmation
    /// Or for to select the type of key, Platform (Figerprint, PIN) or Cross-platform (NFC, USB, Yubikey)
    fun parseSelection(reader: JsonReader): AuthenticatorSelectionCriteria {
        val builder = AuthenticatorSelectionCriteria.Builder()
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "authenticatorAttachment" -> builder.setAttachment(
                        Attachment.fromString(reader.nextString())
                )
                "userVerification" -> reader.skipValue()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return builder.build()
    }

    /// Build the part of information where contains the FIDO2 Key information
    fun parseCredentialDescriptors(
            reader: JsonReader
    ): List<PublicKeyCredentialDescriptor> {
        val list = mutableListOf<PublicKeyCredentialDescriptor>()
        reader.beginArray()
        while (reader.hasNext()) {
            var id: String? = null
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "id" -> id = reader.nextString()
                    "type" -> reader.skipValue()
                    //"transports" -> reader.skipValue()
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            list.add(
                    PublicKeyCredentialDescriptor(
                            PublicKeyCredentialType.PUBLIC_KEY.toString(),
                            id!!.decodeBase64(),
                            /* transports */ null
                    )
            )
        }
        reader.endArray()
        return list
    }

    /// Build the part of information where contains the user information, example name and id
    fun parseUser(reader: JsonReader): PublicKeyCredentialUserEntity {
        reader.beginObject()
        var id: String? = null
        var username: String? = null
        var displayName = ""
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextString()
                "name" -> username = reader.nextString()
                "displayName" -> displayName = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return PublicKeyCredentialUserEntity(
                id!!.decodeBase64(),
                username!!,
                null, // icon
                displayName
        )
    }

    /// Build the part of information where contains the algorithm information
    fun parseParameters(reader: JsonReader): List<PublicKeyCredentialParameters> {
        val parameters = mutableListOf<PublicKeyCredentialParameters>()
        reader.beginArray()
        while (reader.hasNext()) {
            reader.beginObject()
            var type: String? = null
            var alg = 0
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "type" -> type = reader.nextString()
                    "alg" -> alg = reader.nextInt()
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            parameters.add(PublicKeyCredentialParameters(type!!, alg))
        }
        reader.endArray()
        return parameters
    }

    /// Build the part of information where contains the extra options
    fun ParseExtensions(reader: JsonReader): AuthenticationExtensions? {
        reader.beginObject()
        val builder = AuthenticationExtensions.Builder()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "appid" -> builder.setFido2Extension(FidoAppIdExtension(reader.nextString()))
                "uvm" -> builder.setUserVerificationMethodExtension(UserVerificationMethodExtension(reader.nextBoolean()))
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return builder.build()
    }

    /// Build the part of information where contains the user keys
    fun parseUserCredentials(body: ResponseBody): ArrayList<Fido2Credential> {
        fun readCredentials(reader: JsonReader): ArrayList<Fido2Credential> {
            val credentials = ArrayList<Fido2Credential>()
            reader.beginArray()
            while (reader.hasNext()) {
                reader.beginObject()
                var id: String? = null
                var publicKey: String? = null
                var transports = ArrayList<String>()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "credId" -> id = reader.nextString()
                        "publicKey" -> publicKey = reader.nextString()
                        "transports" -> {
                            reader.beginArray()
                            while (reader.hasNext()) {
                                transports.add(reader.nextString())
                            }
                            reader.endArray()
                        }
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                if (!id.isNullOrEmpty() && !publicKey.isNullOrEmpty() && !transports.isNullOrEmpty()) {
                    credentials.add(Fido2Credential(id, publicKey, transports))
                }
            }
            reader.endArray()
            return credentials
        }
        JsonReader(body.byteStream().bufferedReader()).use { reader ->
            reader.beginObject()
            while (reader.hasNext()) {
                val name = reader.nextName()
                if (name == "credentials") {
                    return readCredentials(reader)
                } else {
                    reader.skipValue()
                }
            }
            reader.endObject()
        }
        throw ApiException("Cannot parse credentials")
    }
}