package com.example.traditionalarchitecturemaintain


import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var  auth: FirebaseAuth
    private var sharedPreferences: SharedPreferences? = null
    companion object {
        const val EMAIL_KEY  = "EMAIL_KEY"
        const val PASSWORD_KEY = "PASSWORD_KEY"
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        var etEmail = findViewById<EditText>(R.id.editTextEmailAddress)
        var etPassword = findViewById<EditText>(R.id.editTextPassword)
        sharedPreferences   = getSharedPreferences("MySharedPreMain", MODE_PRIVATE)
        if (sharedPreferences!!.contains(EMAIL_KEY)) {
            etEmail!!.setText(sharedPreferences!!.getString(EMAIL_KEY, ""))
        }
        if (sharedPreferences!!.contains(PASSWORD_KEY)) {
            etPassword!!.setText(sharedPreferences!!.getString(PASSWORD_KEY, ""))
        }

        auth= FirebaseAuth.getInstance()




    }
    fun login(view: View){
        val editTextEmailAddress = findViewById<EditText>(R.id.editTextEmailAddress)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val email=editTextEmailAddress.text.toString()
        val password=editTextPassword.text.toString()


        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->

            if(task.isSuccessful){
                val user = auth.currentUser
                Statics.userId = user!!.uid

                val editor = sharedPreferences!!.edit()
                editor.putString(EMAIL_KEY, email)
                editor.putString(PASSWORD_KEY, password)
                editor.commit()


                val intent= Intent(this, MainActivity::class.java)

                val db = Firebase.firestore
                GlobalScope.launch(Dispatchers.IO) {
                    val userDocuments = db.collection(Statics.FIREBASE_USER)
                        .whereEqualTo("userId", user!!.uid)
                        .get().await()

                    withContext(Dispatchers.Main){
                        if(!userDocuments.isEmpty && userDocuments.documents.isNotEmpty()){
                            var user = userDocuments.documents[0].toObject<User>()
                            Statics.isAdmin = user!!.isAdmin
                            Statics.userName = "${user!!.firstName.toString()} ${user!!.lastName.toString()}"
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }

        }.addOnFailureListener { exception ->

            Toast.makeText(applicationContext,exception.localizedMessage, Toast.LENGTH_LONG).show()

        }

    }

    fun goToRegister(view: View){
        val intent= Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

}
