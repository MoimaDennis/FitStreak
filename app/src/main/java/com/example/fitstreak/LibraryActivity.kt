package com.example.fitstreak

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitstreak.data.AppDatabase
import com.example.fitstreak.data.RetrofitClient
import com.example.fitstreak.data.toEntity
import com.example.fitstreak.data.toWorkout
import com.example.fitstreak.util.WorkoutFilter
import com.example.fitstreak.util.localizedTerm
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LibraryActivity : BaseActivity() {
    override fun selectedNavId() = R.id.nav_library

    private lateinit var adapter: WorkoutAdapter
    private var loadJob: Job? = null

    // English values are what the API understands; the user sees them translated.
    private val bodyParts = listOf("All", "Full body", "Core", "Upper", "Lower")
    private val intensities = listOf("All", "Low", "Medium", "High")
    private val equipment = listOf("All", "None", "Dumbbells", "Resistance band")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)
        setupBottomNav(findViewById<BottomNavigationView>(R.id.bottomNav))

        val rv = findViewById<RecyclerView>(R.id.rvWorkouts)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = WorkoutAdapter(emptyList()) { workout ->
            startActivity(Intent(this, WorkoutPlayerActivity::class.java).apply {
                putExtra("workout_id", workout.workout_id)
                putExtra("workout_name", workout.name)
                putExtra("duration_minutes", workout.duration_minutes)
            })
        }
        rv.adapter = adapter

        setupSpinner(R.id.spinnerBodyPart, bodyParts)
        setupSpinner(R.id.spinnerIntensity, intensities)
        setupSpinner(R.id.spinnerEquipment, equipment)
    }

    private fun setupSpinner(id: Int, keys: List<String>) {
        val spinner = findViewById<Spinner>(id)
        val labels = keys.map { if (it == "All") getString(R.string.lbl_all) else localizedTerm(it) }
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, rowId: Long) = loadWorkouts()
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    private fun selected(id: Int, keys: List<String>): String? =
        keys[findViewById<Spinner>(id).selectedItemPosition].let { if (it == "All") null else it }

    private fun loadWorkouts() {
        val bodyPart = selected(R.id.spinnerBodyPart, bodyParts)
        val intensity = selected(R.id.spinnerIntensity, intensities)
        val equip = selected(R.id.spinnerEquipment, equipment)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val dao = AppDatabase.getInstance(this).workoutDao()

        loadJob?.cancel()   // only the newest filter change matters
        loadJob = lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getWorkouts(bodyPart, intensity, equip)
                if (!res.isSuccessful) throw IllegalStateException("HTTP ${res.code()}")
                val workouts = res.body() ?: emptyList()
                adapter.updateList(workouts)
                dao.insertAll(workouts.map { it.toEntity() })     // keep an offline copy
                showStatus(tvStatus, if (workouts.isEmpty()) getString(R.string.msg_no_workouts) else null)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Offline - filtering the cached workouts", e)
                val cached = WorkoutFilter.apply(dao.getAll().map { it.toWorkout() }, bodyPart, intensity, equip)
                adapter.updateList(cached)
                showStatus(tvStatus, getString(R.string.msg_offline_cached))
            }
        }
    }

    private fun showStatus(tv: TextView, text: String?) {
        tv.visibility = if (text == null) View.GONE else View.VISIBLE
        tv.text = text
    }

    private companion object {
        const val TAG = "LibraryActivity"
    }
}