package com.example.fitnesstracker

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.fitnesstracker.databinding.FragmentHomeActivityBinding
import com.google.android.material.chip.Chip
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class Home_Activity_Fragment : Fragment() {

    private var _binding: FragmentHomeActivityBinding? = null
    private val binding get() = _binding!!

    private val BASE_URL = "http://10.0.2.2/fitness_tracker/"
    private val GET_TYPES_URL = BASE_URL + "get_activity_types.php"
    private val ADD_TYPE_URL = BASE_URL + "add_activity_type.php"
    private val SAVE_LOG_URL = BASE_URL + "save_activity_log.php"
    private val GET_PROFILE_URL = BASE_URL + "get_profile.php"

    private val timeFmt = SimpleDateFormat("HH:mm", Locale.US)

    private var userId: Int = -1
    private var weightKg: Double = 70.0
    private var goalText: String = ""

    data class ActivityType(
        val id: Int,
        val name: String,
        val defaultMet: Double,
        val fieldKeys: Set<String>,
        val isFeatured: Boolean,
        val isSystem: Boolean
    )

    private val allTypes = mutableListOf<ActivityType>()
    private var selectedType: ActivityType? = null

    private val ALL_FIELD_KEYS = listOf(
        "distance_km", "avg_speed_kmh",
        "steps", "laps", "jumps",
        "avg_bpm", "breaks", "resistance"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sp = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        userId = sp.getInt("user_id", -1)

        if (userId <= 0) {
            toast("Missing user_id, login again")
            binding.btnRecord.isEnabled = false
            return
        }

        val username = sp.getString("username", "User") ?: "User"
        binding.tvWelcome.text = "Welcome $username"

        // disable record until weight/goal loaded
        binding.btnRecord.isEnabled = false

        binding.etStartTime.setOnClickListener { pickTime { binding.etStartTime.setText(it) } }
        binding.etEndTime.setOnClickListener { pickTime { binding.etEndTime.setText(it) } }

        binding.btnMoreActivities.setOnClickListener { showAddActivityDialog() }
        binding.btnRecord.setOnClickListener { recordAndSave() }

        loadUserMetaThenInit()
    }

    // ------------------ USER META (weight/goal) ------------------

    private fun loadUserMetaThenInit() {
        val sp = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)

        val cachedWeight = sp.getFloat("weight_kg", -1f)
        val cachedGoal = sp.getString("goal_text", "") ?: ""

        if (cachedWeight > 0f) weightKg = cachedWeight.toDouble()
        if (cachedGoal.isNotBlank()) goalText = cachedGoal



        // if missing -> fetch from API
        if (cachedWeight <= 0f || cachedGoal.isBlank()) {
            fetchUserMetaFromApi {
                binding.btnRecord.isEnabled = true
                loadActivityTypes()
            }
        } else {
            binding.btnRecord.isEnabled = true
            loadActivityTypes()
        }
    }



    private fun fetchUserMetaFromApi(onDone: () -> Unit) {
        val req = object : StringRequest(
            Request.Method.POST,
            GET_PROFILE_URL,
            StringRequest@{ resp ->
                try {
                    val obj = JSONObject(resp)
                    if (!obj.optString("status").equals("success", true)) {
                        toast(obj.optString("message", "Failed to load profile"))
                        onDone()
                        return@StringRequest
                    }

                    val p = obj.getJSONObject("profile")

                    val w = p.optDouble("weight_kg", 70.0)
                    val g = p.optString("goal", "") // if DB doesn't have it -> ""

                    if (w > 0) weightKg = w
                    goalText = g

                    val sp = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
                    sp.edit()
                        .putFloat("weight_kg", weightKg.toFloat())
                        .putString("goal_text", goalText)
                        .apply()


                    onDone()

                } catch (e: Exception) {
                    toast("Bad JSON: ${e.message}")
                    onDone()
                }
            },
            { err ->
                toast("Network error: ${err.message}")
                onDone()
            }
        ) {
            override fun getParams(): MutableMap<String, String> =
                hashMapOf("user_id" to userId.toString())
        }

        Volley.newRequestQueue(requireContext()).add(req)
    }

    // ------------------ TIME PICKER ------------------

    private fun pickTime(onPicked: (String) -> Unit) {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val c = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }
                onPicked(timeFmt.format(c.time))
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    // ------------------ LOAD TYPES ------------------

    private fun loadActivityTypes() {
        val url = "$GET_TYPES_URL?user_id=$userId&featured_only=0"

        val req = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                try {
                    val obj = JSONObject(response)
                    val ok = obj.optBoolean("success", obj.optString("status").equals("success", true))
                    if (!ok) {
                        toast(obj.optString("message", "Server error"))
                        return@StringRequest
                    }

                    val arr = obj.optJSONArray("types") ?: JSONArray()
                    allTypes.clear()

                    for (i in 0 until arr.length()) {
                        val t = arr.getJSONObject(i)

                        val fieldKeysJson = t.optString("field_keys_json", "[]")
                        val keys = parseKeys(fieldKeysJson)

                        // ✅ detect system type: created_by_user_id is null
                        val isSystem =
                            t.isNull("created_by_user_id") ||
                                    t.optString("created_by_user_id", "null").equals("null", true)

                        allTypes.add(
                            ActivityType(
                                id = t.optInt("id"),
                                name = t.optString("name"),
                                defaultMet = t.optDouble("default_met", 6.0),
                                fieldKeys = keys,
                                isFeatured = t.optInt("is_featured", 0) == 1,
                                isSystem = isSystem
                            )
                        )
                    }

                    setupDropdown()
                    setupQuickChips()
                    clearForm()

                } catch (e: Exception) {
                    Log.e("TYPES", "Bad JSON: $response", e)
                    toast("Bad JSON from server")
                }
            },
            { error -> showVolleyError("types", error) }
        )

        Volley.newRequestQueue(requireContext()).add(req)
    }

    private fun setupDropdown() {
        val names = allTypes.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names)
        binding.actActivityType.setAdapter(adapter)

        binding.actActivityType.setOnItemClickListener { _, _, position, _ ->
            selectType(allTypes[position])
        }
    }

    private fun setupQuickChips() {
        binding.chipGroupQuick.removeAllViews()

        val featured = allTypes.filter { it.isFeatured }.take(5)
        for (type in featured) {
            val chip = Chip(requireContext()).apply {
                text = type.name
                isCheckable = true
                setOnClickListener {
                    binding.actActivityType.setText(type.name, false)
                    selectType(type)
                }
            }
            binding.chipGroupQuick.addView(chip)
        }
    }

    private fun parseKeys(fieldKeysJson: String): Set<String> {
        return try {
            val arr = JSONArray(fieldKeysJson)
            buildSet { for (i in 0 until arr.length()) add(arr.getString(i)) }
        } catch (_: Exception) {
            emptySet()
        }
    }

    // ✅ system activities always use default keys
    private fun keysFor(type: ActivityType): Set<String> {
        if (type.isSystem) return defaultKeysFor(type.name)
        return if (type.fieldKeys.isNotEmpty()) type.fieldKeys else defaultKeysFor(type.name)
    }

    private fun defaultKeysFor(name: String): Set<String> {
        return when (name.lowercase(Locale.US)) {
            "running" -> setOf("distance_km", "avg_speed_kmh", "avg_bpm", "steps")
            "walking" -> setOf("distance_km", "steps", "avg_bpm")
            "cycling" -> setOf("distance_km", "avg_speed_kmh", "avg_bpm", "resistance")
            "jump rope" -> setOf("jumps", "avg_bpm")
            "swimming" -> setOf("distance_km", "laps", "avg_bpm")
            else -> setOf("avg_bpm")
        }
    }

    private fun selectType(type: ActivityType) {
        selectedType = type
        applyFieldVisibility(type)
    }

    private fun applyFieldVisibility(type: ActivityType) {
        val keys = keysFor(type)

        val showSpecific = keys.contains("distance_km") || keys.contains("avg_speed_kmh")
        binding.layoutRunningFields.visibility = if (showSpecific) View.VISIBLE else View.GONE
        if (showSpecific) binding.tvSpecificInputsTitle.text = "${type.name} Inputs"

        binding.tilDistanceKm.visibility = if (keys.contains("distance_km")) View.VISIBLE else View.GONE
        binding.tilAvgSpeed.visibility = if (keys.contains("avg_speed_kmh")) View.VISIBLE else View.GONE
        binding.tilSteps.visibility = if (keys.contains("steps")) View.VISIBLE else View.GONE
        binding.tilLaps.visibility = if (keys.contains("laps")) View.VISIBLE else View.GONE
        binding.tilJumps.visibility = if (keys.contains("jumps")) View.VISIBLE else View.GONE
        binding.tilBpm.visibility = if (keys.contains("avg_bpm")) View.VISIBLE else View.GONE
        binding.tilBreaks.visibility = if (keys.contains("breaks")) View.VISIBLE else View.GONE
        binding.tilResistance.visibility = if (keys.contains("resistance")) View.VISIBLE else View.GONE

        // clear hidden
        if (!keys.contains("distance_km")) binding.etDistanceKm.setText("")
        if (!keys.contains("avg_speed_kmh")) binding.etAvgSpeed.setText("")
        if (!keys.contains("steps")) binding.etSteps.setText("")
        if (!keys.contains("laps")) binding.etLaps.setText("")
        if (!keys.contains("jumps")) binding.etJumps.setText("")
        if (!keys.contains("avg_bpm")) binding.etBpm.setText("")
        if (!keys.contains("breaks")) binding.etBreaks.setText("")
        if (!keys.contains("resistance")) binding.etResistance.setText("")
    }

    // ------------------ VALIDATION ------------------

    private fun validateHumanInputs(type: ActivityType, durationMin: Int): Boolean {
        if (durationMin <= 0 || durationMin > 24 * 60) {
            toast("Duration invalid")
            return false
        }

        fun readDouble(et: EditText): Double? {
            val s = et.text?.toString()?.trim().orEmpty()
            return s.toDoubleOrNull()
        }

        if (binding.tilDistanceKm.visibility == View.VISIBLE) {
            val v = readDouble(binding.etDistanceKm) ?: run { toast("Enter distance"); return false }
            if (v < 0.1 || v > 200) { toast("Distance must be 0.1 - 200 km"); return false }
        }

        if (binding.tilAvgSpeed.visibility == View.VISIBLE) {
            val v = readDouble(binding.etAvgSpeed) ?: run { toast("Enter avg speed"); return false }
            if (v < 1 || v > 60) { toast("Avg speed must be 1 - 60 km/h"); return false }
        }

        if (binding.tilSteps.visibility == View.VISIBLE) {
            val v = readDouble(binding.etSteps) ?: 0.0
            if (v < 0 || v > 200000) { toast("Steps too large"); return false }
        }

        if (binding.tilLaps.visibility == View.VISIBLE) {
            val v = readDouble(binding.etLaps) ?: 0.0
            if (v < 0 || v > 5000) { toast("Laps too large"); return false }
        }

        if (binding.tilJumps.visibility == View.VISIBLE) {
            val v = readDouble(binding.etJumps) ?: 0.0
            if (v < 0 || v > 50000) { toast("Jumps too large"); return false }
        }

        if (binding.tilBpm.visibility == View.VISIBLE) {
            val v = readDouble(binding.etBpm) ?: run { toast("Enter bpm"); return false }
            if (v < 40 || v > 220) { toast("BPM must be 40 - 220"); return false }
        }

        if (binding.tilBreaks.visibility == View.VISIBLE) {
            val v = readDouble(binding.etBreaks) ?: 0.0
            if (v < 0 || v > 500) { toast("Breaks too large"); return false }
        }

        if (binding.tilResistance.visibility == View.VISIBLE) {
            val v = readDouble(binding.etResistance) ?: 0.0
            if (v < 0 || v > 20) { toast("Resistance must be 0 - 20"); return false }
        }

        return true
    }

    // ------------------ RECORD + SAVE ------------------

    private fun recordAndSave() {
        val type = selectedType ?: run {
            toast("Select activity type first")
            return
        }

        val startStr = binding.etStartTime.text?.toString()?.trim().orEmpty()
        val endStr = binding.etEndTime.text?.toString()?.trim().orEmpty()

        if (startStr.isBlank() || endStr.isBlank()) {
            toast("Select start time and end time")
            return
        }

        val start = timeFmt.parse(startStr)
        val end = timeFmt.parse(endStr)
        if (start == null || end == null) {
            toast("Invalid time")
            return
        }

        val durationMin = ((end.time - start.time) / 60000.0).roundToInt()
        if (durationMin <= 0) {
            toast("End time must be after start time")
            return
        }

        // ✅ validate before save
        if (!validateHumanInputs(type, durationMin)) return

        val durationHours = durationMin / 60.0
        val calories = type.defaultMet * weightKg * durationHours

        val detailsJson = buildDetailsJson(type)

        saveLogToServer(
            typeId = type.id,
            startTime = startStr,
            endTime = endStr,
            durationMin = durationMin,
            caloriesBurned = calories,
            detailsJson = detailsJson
        )
    }

    private fun buildDetailsJson(type: ActivityType): String {
        val keys = keysFor(type)

        fun add(arr: JSONArray, key: String, value: String, unit: String) {
            if (value.isBlank()) return
            val o = JSONObject()
            o.put("key", key)
            o.put("value", value)
            o.put("unit", unit)
            arr.put(o)
        }

        val arr = JSONArray()

        if (keys.contains("distance_km")) add(arr, "distance_km", binding.etDistanceKm.text?.toString()?.trim().orEmpty(), "km")
        if (keys.contains("avg_speed_kmh")) add(arr, "avg_speed_kmh", binding.etAvgSpeed.text?.toString()?.trim().orEmpty(), "km/h")
        if (keys.contains("steps")) add(arr, "steps", binding.etSteps.text?.toString()?.trim().orEmpty(), "count")
        if (keys.contains("laps")) add(arr, "laps", binding.etLaps.text?.toString()?.trim().orEmpty(), "count")
        if (keys.contains("jumps")) add(arr, "jumps", binding.etJumps.text?.toString()?.trim().orEmpty(), "count")
        if (keys.contains("avg_bpm")) add(arr, "avg_bpm", binding.etBpm.text?.toString()?.trim().orEmpty(), "bpm")
        if (keys.contains("breaks")) add(arr, "breaks", binding.etBreaks.text?.toString()?.trim().orEmpty(), "count")
        if (keys.contains("resistance")) add(arr, "resistance", binding.etResistance.text?.toString()?.trim().orEmpty(), "level")

        return arr.toString()
    }

    private fun saveLogToServer(
        typeId: Int,
        startTime: String,
        endTime: String,
        durationMin: Int,
        caloriesBurned: Double,
        detailsJson: String
    ) {
        val req = object : StringRequest(
            Request.Method.POST,
            SAVE_LOG_URL,
            StringRequest@{ response ->
                try {
                    val obj = JSONObject(response)
                    val ok = obj.optBoolean("success", obj.optString("status").equals("success", true))
                    if (!ok) {
                        toast(obj.optString("message", "Server error"))
                        return@StringRequest
                    }
                    toast("Saved ✅ Calories: ${"%.0f".format(caloriesBurned)}")
                    clearForm()
                } catch (e: Exception) {
                    Log.e("SAVE", "Bad JSON: $response", e)
                    toast("Bad JSON from server")
                }
            },
            { error -> showVolleyError("save", error) }
        ) {
            override fun getParams(): MutableMap<String, String> = hashMapOf(
                "user_id" to userId.toString(),
                "activity_type_id" to typeId.toString(),
                "log_date" to SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time),
                "start_time" to startTime,
                "end_time" to endTime,
                "duration_min" to durationMin.toString(),
                "calories_burned" to caloriesBurned.toString(),
                "details_json" to detailsJson
            )
        }

        Volley.newRequestQueue(requireContext()).add(req)
    }

    // ------------------ ADD TYPE ------------------

    private fun showAddActivityDialog() {
        val nameEt = EditText(requireContext()).apply { hint = "Activity name" }
        val metEt = EditText(requireContext()).apply {
            hint = "Default MET (e.g. 6.0)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val fields = ALL_FIELD_KEYS.toTypedArray()
        val checked = BooleanArray(fields.size) { false }

        val wrapper = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 0)
            addView(nameEt)
            addView(metEt)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Activity")
            .setView(wrapper)
            .setMultiChoiceItems(fields, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton("Save") { _, _ ->
                val name = nameEt.text.toString().trim()
                val met = metEt.text.toString().trim().toDoubleOrNull() ?: 6.0
                if (name.isBlank()) {
                    toast("Name required")
                    return@setPositiveButton
                }

                val selected = mutableListOf<String>()
                for (i in fields.indices) if (checked[i]) selected.add(fields[i])

                val keysJson = JSONArray(selected).toString()
                addTypeToServer(name, met, keysJson)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addTypeToServer(name: String, met: Double, fieldKeysJson: String) {
        val req = object : StringRequest(
            Request.Method.POST,
            ADD_TYPE_URL,
            StringRequest@{ response ->
                try {
                    val obj = JSONObject(response)
                    val ok = obj.optBoolean("success", obj.optString("status").equals("success", true))
                    if (!ok) {
                        toast(obj.optString("message", "Server error"))
                        return@StringRequest
                    }
                    toast("Added ✅")
                    loadActivityTypes()
                } catch (e: Exception) {
                    Log.e("ADD", "Bad JSON: $response", e)
                    toast("Bad JSON from server")
                }
            },
            { error -> showVolleyError("add_type", error) }
        ) {
            override fun getParams(): MutableMap<String, String> = hashMapOf(
                "user_id" to userId.toString(),
                "name" to name,
                "default_met" to met.toString(),
                "field_keys_json" to fieldKeysJson
            )
        }

        Volley.newRequestQueue(requireContext()).add(req)
    }

    // ------------------ HELPERS ------------------

    private fun showVolleyError(tag: String, error: com.android.volley.VolleyError) {
        val status = error.networkResponse?.statusCode
        val body = error.networkResponse?.data?.toString(Charsets.UTF_8)
        Log.e("VOLLEY", "error($tag) status=$status body=$body", error)

        val msg = if (status != null) "HTTP $status" else error.toString()
        toast("Server error: $msg")
    }

    private fun clearForm() {
        selectedType = null
        binding.actActivityType.setText("", false)

        binding.etStartTime.setText("")
        binding.etEndTime.setText("")

        binding.layoutRunningFields.visibility = View.GONE

        binding.etDistanceKm.setText("")
        binding.etAvgSpeed.setText("")
        binding.etSteps.setText("")
        binding.etLaps.setText("")
        binding.etJumps.setText("")
        binding.etBpm.setText("")
        binding.etBreaks.setText("")
        binding.etResistance.setText("")
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
