package com.example.fitstreak

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitstreak.data.Badge

class BadgeAdapter(private var items: List<Badge>) : RecyclerView.Adapter<BadgeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvBadgeName)
        val tvCriteria: TextView = view.findViewById(R.id.tvBadgeCriteria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_badge, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvName.text = items[position].badge_name
        holder.tvCriteria.text = items[position].criteria
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<Badge>) {
        items = newItems
        notifyDataSetChanged()
    }
}

