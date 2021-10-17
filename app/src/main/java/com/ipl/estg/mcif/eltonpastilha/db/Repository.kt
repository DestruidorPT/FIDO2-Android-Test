package com.ipl.estg.mcif.eltonpastilha.db

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.ipl.estg.mcif.eltonpastilha.model.User

class Repository(private val prefs: SharedPreferences) {

    companion object {
        private const val TAG = "AuthRepository"

        // Keys for SharedPreferences
        private const val PREFS_NAME = "DB_app_Fido2"
        private const val PREF_USER = "user"
        private const val PREF_CHALLENGE = "Challenge"
        private const val PREF_PUBLIC_TRANSPORT_SELECTED = "Authenticator Attachment Selected"
        private const val PREF_COOKIES = "set-cookie"
        private const val PREF_TOKEN = "token"
        private const val PREF_CREDENTIALS = "credentials"
        private const val PREF_LOCAL_CREDENTIAL_ID = "local_credential_id"

        private var instance: Repository? = null

        fun getInstance(context: Context): Repository {
            return instance ?: synchronized(this) {
                instance ?: Repository(
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                ).also { instance = it }
            }
        }
    }

    //Recover user information
    fun getUser(): User {
        return User().fromJSON(prefs.getString(PREF_USER, null)!!)
    }

    //Save user information
    fun setUser(user: User) {
        prefs.edit(commit = true) {
            putString(PREF_USER, user.toJSON())
        }
    }

    //Recover Challenge information
    fun getChallenge(): String {
        return prefs.getString(PREF_CHALLENGE, null)!!
    }

    //Save Challenge information
    fun setChallenge(challenge: String) {
        prefs.edit(commit = true) {
            putString(PREF_CHALLENGE, challenge)
        }
    }

    //Recover option selected (Platform or Cross-Platform) information
    fun getPublicKeyTransportSelected(): String {
        return prefs.getString(PREF_PUBLIC_TRANSPORT_SELECTED, null)!!
    }

    //Save option selected (Platform or Cross-Platform) information
    fun setPublicKeyTransportSelected(authenticatorAttachment: String) {
        prefs.edit(commit = true) {
            putString(PREF_PUBLIC_TRANSPORT_SELECTED, authenticatorAttachment)
        }
    }

    //Recover Cookies information
    fun getCookies(): HashSet<String> {
        return prefs.getStringSet(PREF_COOKIES, HashSet())!! as HashSet<String>
    }

    //Save Cookies information
    fun setCookies(cookies: HashSet<String>) {
        prefs.edit(commit = true) {
            putStringSet(PREF_COOKIES, cookies)
        }
    }

    //Save Challenge and option selected (Platform or Cross-Platform) information
    fun setChallengeAndPublicKeyTransportSelected(challenge: String, authenticatorAttachment: String) {
        prefs.edit(commit = true) {
            putString(PREF_PUBLIC_TRANSPORT_SELECTED, authenticatorAttachment)
            putString(PREF_CHALLENGE, challenge)
        }
    }

    //Save user and Challenge information
    fun setUserAndChallenge(user: User, challenge: String) {
        prefs.edit(commit = true) {
            putString(PREF_USER, user.toJSON())
            putString(PREF_CHALLENGE, challenge)
        }
    }

    //Save all information
    fun setAll(user: User, challenge: String, authenticatorAttachment: String) {
        prefs.edit(commit = true) {
            putString(PREF_USER, user.toJSON())
            putString(PREF_CHALLENGE, challenge)
            putString(PREF_PUBLIC_TRANSPORT_SELECTED, authenticatorAttachment)
        }
    }

    // Remove or clear any information storage
    fun clean() {
        prefs.edit(commit = true) {
            putString(PREF_USER, "")
            putString(PREF_CHALLENGE, "")
            putString(PREF_PUBLIC_TRANSPORT_SELECTED, "")
            putStringSet(PREF_COOKIES, HashSet())
        }
    }
}