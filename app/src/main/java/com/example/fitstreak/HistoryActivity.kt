package com.example.fitstreak

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitstreak.data.HistoryItem
import com.example.fitstreak.data.RetrofitClient
import com.example.fitstreak.util.GameLogic
import com.example.fitstreak.util.Session
import kotlinx.coroutines.launch

/** PART 3 - USER DEFINED FEATURE 4: Workout History (last 50 workouts from the API). */
class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rvHistory)
        rv.layoutManager = LinearLayoutManager(this)
        val adapter = HistoryAdapter(emptyList())
        rv.adapter = adapter
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getHistory(Session.bearer(this@HistoryActivity))
                when {
                    res.isSuccessful -> {
                        val items = res.body() ?: emptyList()
                        adapter.updateList(items)
                        tvEmpty.setText(R.string.msg_no_history)
                        tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                    }
                    res.code() == 401 -> Session.expire(this@HistoryActivity)
                    else -> { tvEmpty.setText(R.string.err_server); tvEmpty.visibility = View.VISIBLE }
                }
            } catch (e: Exception) {
                Log.w(TAG, "History not loaded (offline?)", e)
                tvEmpty.setText(R.string.msg_offline_cached)
                tvEmpty.visibility = View.VISIBLE
            }
        }
    }

    private companion object {
        const val TAG = "HistoryActivity"
    }
}

class HistoryAdapter(private var items: List<HistoryItem>) : RecyclerView.Adapter<HistoryAdapter.VH>() {
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvHistName)
        val tvDetails: TextView = view.findViewById(R.id.tvHistDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val h = items[position]
        holder.tvName.text = h.name
        holder.tvDetails.text = holder.itemView.context.getString(
            R.string.fmt_history_details, h.created_at.take(10), GameLogic.formatTimer(h.duration_seconds), h.xp_gained
        )
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<HistoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}