package com.example.admindacs3

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.admindacs3.databinding.ActivityAdminProfileBinding
import com.example.admindacs3.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminProfileActivity : AppCompatActivity() {
    private val binding: ActivityAdminProfileBinding by lazy {
        ActivityAdminProfileBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var adminReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        adminReference = database.reference.child("user")

        binding.backButton.setOnClickListener {
            finish()
        }
        binding.saveInfoButton.setOnClickListener {
            updateUserData()
        }

        binding.name.isEnabled = false
        binding.address.isEnabled = false
        binding.email.isEnabled = false
        binding.phone.isEnabled = false
        binding.password.isEnabled = false
        binding.saveInfoButton.isEnabled = false

        var isEnable = false
        binding.editButton.setOnClickListener {
            isEnable = !isEnable
            binding.name.isEnabled = isEnable
            binding.address.isEnabled = isEnable
            binding.email.isEnabled = isEnable
            binding.phone.isEnabled = isEnable
            binding.password.isEnabled = isEnable




            if (isEnable) {
                binding.name.requestFocus()
            }
            binding.saveInfoButton.isEnabled = isEnable

        }
        retrieveUserData()

    }


    private fun retrieveUserData() {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null) {
            val userReference = adminReference.child(currentUserUid)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var name = snapshot.child("name").getValue()
                        var nameOfRestaurant = snapshot.child("nameOfRestaurant").getValue()
                        var email = snapshot.child("email").getValue()
                        var password = snapshot.child("password").getValue()
                        var phone = snapshot.child("phone").getValue()
                        var address = snapshot.child("address").getValue()
                        Log.d("TAG", "onDataChange: $name")
                        setDataToTextView(name, nameOfRestaurant, email, password, phone, address)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    private fun setDataToTextView(
        name: Any?,
        nameOfRestaurant: Any?,
        email: Any?,
        password: Any?,
        phone: Any?,
        address: Any?
    ) {
        binding.name.setText(name.toString())
        binding.address.setText(address?.toString() ?: "")
        binding.email.setText(email.toString())
        binding.phone.setText(phone.toString())
        binding.password.setText(password.toString())
    }

    private fun updateUserData() {
        var updateName = binding.name.text.toString()
        var updateAddress = binding.address.text.toString()
        var updateEmail = binding.email.text.toString()
        var updatePhone = binding.phone.text.toString()
        var updatePassword = binding.password.text.toString()

        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != null) {
            val userRef = adminReference.child(currentUserUid)
            userRef.child("name").setValue(updateName)
            userRef.child("address").setValue(updateAddress)
            userRef.child("email").setValue(updateEmail)
            userRef.child("phone").setValue(updatePhone)
            userRef.child("password").setValue(updatePassword)

            Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
            auth.currentUser?.updateEmail(updateEmail)
            auth.currentUser?.updatePassword(updatePassword)
        } else {
            Toast.makeText(this, "Profile Update Failed", Toast.LENGTH_SHORT).show()
        }
    }
}