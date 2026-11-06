package com.example.fitstreak

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitstreak.data.Workout
import com.example.fitstreak.util.localizedTerm

class WorkoutAdapter(
    private var items: List<Workout>,
    private val onClick: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvDetails: TextView = view.findViewById(R.id.tvDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = items[position]
        holder.tvName.text = workout.name
        val ctx = holder.itemView.context
        holder.tvDetails.text = ctx.getString(
            R.string.fmt_workout_details,
            workout.duration_minutes,
            ctx.localizedTerm(workout.intensity),
            ctx.localizedTerm(workout.body_part),
            ctx.localizedTerm(workout.equipment)
        )
        holder.itemView.setOnClickListener { onClick(workout) }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<Workout>) {
        items = newItems
        notifyDataSetChanged()
    }
}