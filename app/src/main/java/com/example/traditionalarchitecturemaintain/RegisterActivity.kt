package com.example.traditionalarchitecturemaintain

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var  auth: FirebaseAuth




    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)




        auth= FirebaseAuth.getInstance()

    }

    fun register(view: View){
        val editTextEmailAddress = findViewById<EditText>(R.id.editTextEmailAddress)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val editTextFirstName = findViewById<EditText>(R.id.editTextFirstName)
        val editTextLastName = findViewById<EditText>(R.id.editTextLastName)

        val email=editTextEmailAddress.text.toString()
        val password=editTextPassword.text.toString()
        val firstName=editTextFirstName.text.toString()
        val lastName=editTextLastName.text.toString()




        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->

            if(task.isSuccessful){
                val user = auth.currentUser
                Toast.makeText(this,"${user!!.email}",Toast.LENGTH_LONG).show()

                val intent= Intent(this,MainActivity::class.java)

                startActivity(intent)

                finish()

            }

        }.addOnFailureListener { exception ->

            Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()

        }

    }

    fun goToLogin(view: View){

        val intent= Intent(this, LoginActivity::class.java)

        startActivity(intent)

    }



}