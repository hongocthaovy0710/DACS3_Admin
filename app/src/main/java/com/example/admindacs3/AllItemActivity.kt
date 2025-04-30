package com.example.admindacs3

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.admindacs3.adapter.AddItemAdapter
import com.example.admindacs3.databinding.ActivityAllItemBinding
import java.util.ArrayList

class AllItemActivity : AppCompatActivity() {
    private val binding : ActivityAllItemBinding by lazy {
        ActivityAllItemBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val menuFoodName = listOf("Burger", "sandwich", "momo","item","sandwich","momo")
        val menuItemPrice = listOf("$5", "$6", "$8","$9","$10","$10")
        val menuImage = listOf(
            R.drawable.menu1,
            R.drawable.menu2,
            R.drawable.menu3,
            R.drawable.menu4,
            R.drawable.menu5,
            R.drawable.menu4
        )
        binding.backButton.setOnClickListener {
            finish()
        }

        val adapter  = AddItemAdapter(ArrayList(menuFoodName),ArrayList(menuItemPrice),ArrayList(menuImage))
        binding.MenuRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.MenuRecyclerView.adapter = adapter

    }
}