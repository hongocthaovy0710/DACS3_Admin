package com.example.admindacs3

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.admindacs3.adapter.MenuItemAdapter
import com.example.admindacs3.databinding.ActivityAllItemBinding
import com.example.admindacs3.model.AllMenu
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class AllItemActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private var menuItems: ArrayList<AllMenu> = ArrayList()

    private val binding: ActivityAllItemBinding by lazy {
        ActivityAllItemBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        databaseReference = FirebaseDatabase.getInstance().reference
        retrieveMenuItem()



        binding.backButton.setOnClickListener {
            finish()
        }



    }

    private fun retrieveMenuItem() {
        database = FirebaseDatabase.getInstance()
        val foodRef: DatabaseReference = database.reference.child("menu")

        //fetch data from database
        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                //clear existing data before populating
                menuItems.clear()
                // loop for through each food item
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(AllMenu::class.java)
                    menuItem?.let {
                        menuItems.add(it)

                    }
                }
                setAdapter()

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", "Error: ${error.message}")
            }
        })
    }
    private fun setAdapter() {
        val adapter = MenuItemAdapter(this@AllItemActivity, menuItems, databaseReference,
            onDeleteClickListener = { position ->
                deleteMenuItems(position)
            },
            //edit
            onEditClickListener = { menuItem ->
                showEditDialog(menuItem)
            }
        )
        binding.MenuRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.MenuRecyclerView.adapter = adapter
    }


    private fun deleteMenuItems(position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Xác nhận xóa")
        builder.setMessage("Bạn có chắc muốn xóa món này không?")

        builder.setPositiveButton("Xóa") { dialog, _ ->
            val menuItemToDelete = menuItems[position]
            val menuItemKey = menuItemToDelete.key
            val foodMenuReference = database.reference.child("menu").child(menuItemKey!!)

            foodMenuReference.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    menuItems.removeAt(position)
                    binding.MenuRecyclerView.adapter?.notifyItemRemoved(position)
                } else {
                    Toast.makeText(this, "Xóa không thành công", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Hủy") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }


    //edit
    private fun showEditDialog(position: Int) {
        val menuItem = menuItems[position]
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_item, null)
        val editFoodName = dialogView.findViewById<EditText>(R.id.editFoodName)
        val editFoodPrice = dialogView.findViewById<EditText>(R.id.editFoodPrice)
        val editFoodDescription = dialogView.findViewById<EditText>(R.id.editFoodDescription)
        val editFoodIngredient = dialogView.findViewById<EditText>(R.id.editFoodIngredient)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)

        // Gán dữ liệu cũ vào EditText
        editFoodName.setText(menuItem.foodName)
        editFoodPrice.setText(menuItem.foodPrice)
        editFoodDescription.setText(menuItem.foodDescription ?: "")
        editFoodIngredient.setText(menuItem.foodIngredient ?: "")

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val newName = editFoodName.text.toString().trim()
            val newPrice = editFoodPrice.text.toString().trim()
            val newDesc = editFoodDescription.text.toString().trim()
            val newIngre = editFoodIngredient.text.toString().trim()

            if (newName.isNotEmpty() && newPrice.isNotEmpty()) {
                val updateMap = mapOf(
                    "foodName" to newName,
                    "foodPrice" to newPrice,
                    "foodDescription" to newDesc,
                    "foodIngredient" to newIngre
                )
                val key = menuItem.key ?: return@setOnClickListener

                database.reference.child("menu").child(key)
                    .updateChildren(updateMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Đã cập nhật!", Toast.LENGTH_SHORT).show()
                        menuItem.foodName = newName
                        menuItem.foodPrice = newPrice
                        binding.MenuRecyclerView.adapter?.notifyItemChanged(position)
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Không để trống tên và giá!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}