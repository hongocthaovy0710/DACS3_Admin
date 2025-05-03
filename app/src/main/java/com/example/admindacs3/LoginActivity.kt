package com.example.admindacs3

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.admindacs3.databinding.ActivityLoginBinding
import com.example.admindacs3.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {


    private var userName: String? = null
    private var nameOfRestaurant: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var database: DatabaseReference


    @Suppress("unused")
    private lateinit var googleSignInClient: GoogleSignInClient


    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)



        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        //auth
        auth = FirebaseAuth.getInstance()
        //database
        database = Firebase.database.reference

        //goole sign in
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)




        binding.loginButton.setOnClickListener {


            email = binding.email.text.toString().trim()
            password = binding.password.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                createUserAccount(email, password)
            }
        }
        binding.GoogleButton.setOnClickListener {
            val signIntent = googleSignInClient.signInIntent
            launcher.launch(signIntent)

        }


        binding.dontHaveAccountButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

    }

    private fun createUserAccount(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user: FirebaseUser? = auth.currentUser
                Toast.makeText(this, "Login successfully", Toast.LENGTH_SHORT).show()

                updateUi(user)
            } else {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user: FirebaseUser? = auth.currentUser
                        Toast.makeText(this, "Create User & Login Successfully", Toast.LENGTH_SHORT)
                            .show()
                        saveUserData()
                        updateUi(user)

                    } else {
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                        Log.d(
                            "Account",
                            "createUserAccount: Authentication Failure",
                            task.exception
                        )
                    }

                }

            }
        }
    }

    private fun saveUserData() {
        email = binding.email.text.toString().trim()
        password = binding.password.text.toString().trim()

        val user = UserModel(userName, nameOfRestaurant, email, password)
        val userId: String = FirebaseAuth.getInstance().currentUser!!.uid
        userId?.let {

            database.child("user").child(it).setValue(user)

        }

    }


// launcher for google sign in
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task= GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful){
                val account : GoogleSignInAccount = task.result
                val credential = GoogleAuthProvider.getCredential(account.idToken,null)
                auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful){
                        Toast.makeText(this,"Successfully sign-in with Google",Toast.LENGTH_SHORT).show()
                        updateUi(authTask.result?.user)
                        finish()

                    }else{
                        Toast.makeText(this,"Google sign-in failed",Toast.LENGTH_SHORT).show()

                    }

                }
            }
            else
            {
                Toast.makeText(this,"Google sign-in failed",Toast.LENGTH_SHORT).show()

            }
        }

    }
    //check if user is already logged in
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {

                startActivity(Intent(this, MainActivity::class.java))
                finish()

        }
    }
    private fun updateUi(user: FirebaseUser?) {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }


}