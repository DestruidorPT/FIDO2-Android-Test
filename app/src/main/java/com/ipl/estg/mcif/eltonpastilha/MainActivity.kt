package com.ipl.estg.mcif.eltonpastilha

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ipl.estg.mcif.eltonpastilha.view.LoginActivity
import com.ipl.estg.mcif.eltonpastilha.view.RegisterActivity


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // Send to the login activity
    fun onClickButtonLogin(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    // Send to the Register activity
    fun onClickButtonRegister(view: View) {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}