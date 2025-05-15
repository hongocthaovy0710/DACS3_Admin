package com.example.admindacs3.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.admindacs3.R
import com.example.admindacs3.model.SoldProduct

class SoldProductAdapter(
    private val context: Context,
    private val soldProducts: ArrayList<SoldProduct>
) : RecyclerView.Adapter<SoldProductAdapter.SoldProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoldProductViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.sold_product_item, parent, false)
        return SoldProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: SoldProductViewHolder, position: Int) {
        val product = soldProducts[position]

        // Set rank (position + 1)
        holder.rankTextView.text = (position + 1).toString()

        // Set product details
        holder.productNameTextView.text = product.name
        holder.productPriceTextView.text = "$${String.format("%.2f", product.price)}"
        holder.quantitySoldTextView.text = product.quantity.toString()
        holder.revenueTextView.text = "$${String.format("%.2f", product.totalRevenue)}"

        // Highlight top 3 products
        when (position) {
            0 -> {
                holder.rankTextView.setTextColor(context.getColor(android.R.color.holo_red_dark))
                holder.rankTextView.textSize = 20f
            }
            1 -> holder.rankTextView.setTextColor(context.getColor(android.R.color.holo_orange_dark))
            2 -> holder.rankTextView.setTextColor(context.getColor(android.R.color.holo_green_dark))
            else -> holder.rankTextView.setTextColor(context.getColor(android.R.color.darker_gray))
        }
    }

    override fun getItemCount(): Int {
        return soldProducts.size
    }

    class SoldProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankTextView: TextView = itemView.findViewById(R.id.rankTextView)
        val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)
        val productPriceTextView: TextView = itemView.findViewById(R.id.productPriceTextView)
        val quantitySoldTextView: TextView = itemView.findViewById(R.id.quantitySoldTextView)
        val revenueTextView: TextView = itemView.findViewById(R.id.revenueTextView)
    }
}