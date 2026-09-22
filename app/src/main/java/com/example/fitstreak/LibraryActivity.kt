package com.example.fitstreak

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitstreak.data.AppDatabase
import com.example.fitstreak.data.RetrofitClient
import com.example.fitstreak.data.toEntity
import com.example.fitstreak.data.toWorkout
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class LibraryActivity : BaseActivity() {
    override fun selectedNavId() = R.id.nav_library

    private lateinit var adapter: WorkoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)
        setupBottomNav(findViewById<BottomNavigationView>(R.id.bottomNav))

        val rv = findViewById<RecyclerView>(R.id.rvWorkouts)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = WorkoutAdapter(emptyList()) { workout ->
            val intent = Intent(this, WorkoutPlayerActivity::class.java)
            intent.putExtra("workout_id", workout.workout_id)
            intent.putExtra("workout_name", workout.name)
            intent.putExtra("duration_minutes", workout.duration_minutes)
            startActivity(intent)
        }
        rv.adapter = adapter

        val bodyPartSpinner = findViewById<Spinner>(R.id.spinnerBodyPart)
        val intensitySpinner = findViewById<Spinner>(R.id.spinnerIntensity)
        val equipmentSpinner = findViewById<Spinner>(R.id.spinnerEquipment)

        val bodyParts = listOf("All", "Full body", "Core", "Upper", "Lower")
        val intensities = listOf("All", "Low", "Medium", "High")
        val equipment = listOf("All", "None", "Dumbbells", "Resistance band")

        bodyPartSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, bodyParts)
        intensitySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, intensities)
        equipmentSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, equipment)

        val listener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                loadWorkouts()
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
        bodyPartSpinner.onItemSelectedListener = listener
        intensitySpinner.onItemSelectedListener = listener
        equipmentSpinner.onItemSelectedListener = listener

        loadWorkouts()
    }

    private fun loadWorkouts() {
        val bodyPart = findViewById<Spinner>(R.id.spinnerBodyPart).selectedItem.toString().let { if (it == "All") null else it }
        val intensity = findViewById<Spinner>(R.id.spinnerIntensity).selectedItem.toString().let { if (it == "All") null else it }
        val equipment = findViewById<Spinner>(R.id.spinnerEquipment).selectedItem.toString().let { if (it == "All") null else it }

        val db = AppDatabase.getInstance(this)

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getWorkouts(bodyPart, intensity, equipment)
                if (res.isSuccessful) {
                    val workouts = res.body() ?: emptyList()
                    adapter.updateList(workouts)
                    db.workoutDao().insertAll(workouts.map { it.toEntity() })
                }
            } catch (e: Exception) {
                val cached = db.workoutDao().getAll().map { it.toWorkout() }
                adapter.updateList(cached)
            }
        }
    }
}