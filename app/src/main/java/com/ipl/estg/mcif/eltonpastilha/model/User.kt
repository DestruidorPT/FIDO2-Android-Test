package com.ipl.estg.mcif.eltonpastilha.model

import com.google.gson.Gson

// User information
data class User(val username : String ? = null, val id : String ? = null) {
    // transform  the user information to json
    fun toJSON() : String {
        return Gson().toJson(this)
    }

    // recover from JSON the user information
    fun fromJSON(user : String) : User {
        return Gson().fromJson<User>(user, User::class.java)
    }
}