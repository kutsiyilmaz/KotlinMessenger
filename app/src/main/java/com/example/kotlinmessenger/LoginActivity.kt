package com.example.kotlinmessenger

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "MainActivity"//

class LoginActivity:AppCompatActivity() {

    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        loginButton = findViewById(R.id.login_button_login)


        val back_to_register = findViewById(R.id.back_to_register_textview) as TextView


        loginButton.setOnClickListener{
            performLogin()
        }

        back_to_register.setOnClickListener {
            finish()
        }

    }

    private fun performLogin(){

        val email = findViewById(R.id.email_edittext_login) as EditText
        val password = findViewById(R.id.password_edittext_login) as EditText

        Log.d(TAG, "Email is: " + email.text.toString())
        Log.d(TAG, "Password is: ${password.text.toString()}")

        if(email.text.toString().isEmpty() || password.text.toString().isEmpty()){
            Toast.makeText(this, "Please enter your email/password!", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email.text.toString(),password.text.toString())
            .addOnCompleteListener {
                if(!it.isSuccessful)return@addOnCompleteListener
                //else if success
                Log.d(TAG, "Successfully loged in user with uid: ${it.result.user?.uid}")
                Toast.makeText(this, "Successfully logged in!!!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, LatestMessagesActivity::class.java)
                //yeni activitye gecince eskisini yok et. geri rusuyla donemeyiz.
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }.addOnFailureListener {
                Log.d(TAG, "User failed to log in: ${it.message}")
                Toast.makeText(this, "User failed to log in: ${it.message}", Toast.LENGTH_SHORT).show()
            }

    }


}