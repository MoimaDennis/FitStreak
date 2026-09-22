package com.example.fitstreak

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitstreak.data.LeaderboardEntry

class LeaderboardAdapter(private var items: List<LeaderboardEntry>) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvResult: TextView = view.findViewById(R.id.tvResult)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvUsername.text = items[position].username
        holder.tvResult.text = items[position].result.toString()
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<LeaderboardEntry>) {
        items = newItems
        notifyDataSetChanged()
    }
}
