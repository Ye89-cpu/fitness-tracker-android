package com.example.fitnesstracker

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.fitnesstracker.databinding.FragmentHomeWorkoutBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Home_Workout_Fragment : Fragment() {

    private lateinit var binding: FragmentHomeWorkoutBinding

    // ✅ Update folder name if different
    private val BASE_URL = "http://10.0.2.2/fitness_tracker/"
    private val GET_TYPES_URL = BASE_URL + "get_workout_types.php"
    private val SAVE_URL = BASE_URL + "save_workout_log.php"

    // workout type mapping
    private val typeIdByName = mutableMapOf<String, Int>()
    private var selectedTypeId: Int? = null

    private val timeFmt = SimpleDateFormat("HH:mm", Locale.US)
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeWorkoutBinding.inflate(inflater, container, false)

        // ✅ Use SAME prefs as your working fragment
        val sp = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        val userId = sp.getInt("user_id", -1)
        val username = sp.getString("username", "User") ?: "User"

        // UI header
        binding.tvWelcome.text = "Enter Your Workout Data"

        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return binding.root
        }

        setupIntensityLabel()
        setupTimePickers()
        fetchWorkoutTypes()

        binding.btnRecord.setOnClickListener {
            saveWorkout(userId)
        }

        return binding.root
    }

    // ------------------- Intensity -------------------
    private fun setupIntensityLabel() {
        updateIntensityText(binding.sbIntensity.progress)
        binding.sbIntensity.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                updateIntensityText(progress)
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }

    private fun updateIntensityText(value: Int) {
        val label = when (value) {
            in 0..33 -> "Low"
            in 34..66 -> "Medium"
            else -> "High"
        }
        binding.tvIntensityValue.text = "Intensity: $label"
    }

    // ------------------- Time Pickers -------------------
    private fun setupTimePickers() {
        binding.etStartTime.setOnClickListener {
            showTimePicker { t -> binding.etStartTime.setText(t) }
        }
        binding.etEndTime.setOnClickListener {
            showTimePicker { t -> binding.etEndTime.setText(t) }
        }
    }

    private fun showTimePicker(onPicked: (String) -> Unit) {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                onPicked(String.format(Locale.US, "%02d:%02d", hour, minute))
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    // ------------------- Load Workout Types -------------------
    private fun fetchWorkoutTypes() {
        val req = JsonArrayRequest(
            Request.Method.GET,
            GET_TYPES_URL,
            null,
            { response ->
                val names = parseWorkoutTypes(response)
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names)

                binding.actActivityType.setAdapter(adapter)

                // user picked from dropdown list
                binding.actActivityType.setOnItemClickListener { _, _, position, _ ->
                    val pickedName = adapter.getItem(position) ?: return@setOnItemClickListener
                    selectedTypeId = typeIdByName[pickedName]
                }
            },
            { error ->
                Log.e("WORKOUT_TYPES", "Volley error: $error")
                Toast.makeText(requireContext(), "Failed to load workout types", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(requireContext()).add(req)
    }

    private fun parseWorkoutTypes(response: JSONArray): List<String> {
        val names = mutableListOf<String>()
        typeIdByName.clear()

        for (i in 0 until response.length()) {
            val obj = response.getJSONObject(i)
            val id = obj.getInt("id")
            val name = obj.getString("name")
            names.add(name)
            typeIdByName[name] = id
        }
        return names
    }

    // ------------------- Save Workout -------------------
    private fun saveWorkout(userId: Int) {

        // If user typed text but didn't click a dropdown item, map it now
        val typedName = binding.actActivityType.text?.toString()?.trim().orEmpty()
        if (selectedTypeId == null && typedName.isNotEmpty()) {
            selectedTypeId = typeIdByName[typedName]
        }

        val typeId = selectedTypeId
        if (typeId == null) {
            Toast.makeText(requireContext(), "Please select workout type from list", Toast.LENGTH_SHORT).show()
            return
        }

        val start = binding.etStartTime.text?.toString()?.trim().orEmpty()
        val end = binding.etEndTime.text?.toString()?.trim().orEmpty()

        if (start.isBlank() || end.isBlank()) {
            Toast.makeText(requireContext(), "Select start and end time", Toast.LENGTH_SHORT).show()
            return
        }

        val durationMin = computeDurationMinutes(start, end)
        if (durationMin <= 0) {
            Toast.makeText(requireContext(), "End time must be after start time", Toast.LENGTH_SHORT).show()
            return
        }

        val intensity = binding.sbIntensity.progress
        val reps = binding.etReps.text?.toString()?.trim().orEmpty()
        val weight = binding.etWeightLifted.text?.toString()?.trim().orEmpty()
        val notes = binding.etNotes.text?.toString()?.trim().orEmpty()

        Log.d("WORKOUT_SAVE", "user_id=$userId workout_type_id=$typeId start=$start end=$end duration=$durationMin")

        val req = object : StringRequest(
            Method.POST,
            SAVE_URL,
            { response ->
                try {
                    val obj = JSONObject(response)
                    val ok = obj.optBoolean("success", false)
                    val msg = obj.optString("message", "Saved")
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    if (ok) clearForm()
                } catch (e: Exception) {
                    Log.e("WORKOUT_SAVE", "Bad JSON: $response", e)
                    Toast.makeText(requireContext(), "Bad server response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("WORKOUT_SAVE", "Volley error: $error")
                Toast.makeText(requireContext(), "Failed to save workout", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "user_id" to userId.toString(),
                    "workout_type_id" to typeId.toString(),
                    "start_time" to start,
                    "end_time" to end,
                    "duration_minutes" to durationMin.toString(),
                    "intensity" to intensity.toString(),
                    "reps" to reps,
                    "weight_lifted" to weight,
                    "notes" to notes
                )
            }
        }

        Volley.newRequestQueue(requireContext()).add(req)
    }

    // supports midnight crossing
    private fun computeDurationMinutes(start: String, end: String): Int {
        return try {
            val s = timeFmt.parse(start) ?: return 0
            val e = timeFmt.parse(end) ?: return 0
            var diff = e.time - s.time
            if (diff < 0) diff += 24L * 60L * 60L * 1000L
            (diff / (60L * 1000L)).toInt()
        } catch (_: Exception) {
            0
        }
    }

    private fun clearForm() {
        binding.actActivityType.setText("", false)
        selectedTypeId = null
        binding.etStartTime.setText("")
        binding.etEndTime.setText("")
        binding.etReps.setText("")
        binding.etWeightLifted.setText("")
        binding.etNotes.setText("")
        binding.sbIntensity.progress = 50
    }
}
