package com.example.fitnesstracker

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.fitnesstracker.databinding.FragmentHomeHistoryBinding
import com.example.fitnesstracker.databinding.LayoutWeekSectionBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class Home_History_Fragment : Fragment() {

    private var _binding: FragmentHomeHistoryBinding? = null
    private val binding get() = _binding!!

    private val BASE_URL = "http://10.0.2.2/fitness_tracker/"
    private val HISTORY_URL = BASE_URL + "get_history_week.php"
    private val DELETE_URL = BASE_URL + "delete_history_item.php"
    private val UPDATE_URL = BASE_URL + "update_history_item.php"

    private lateinit var week1: LayoutWeekSectionBinding
    private lateinit var week2: LayoutWeekSectionBinding
    private lateinit var week3: LayoutWeekSectionBinding

    private val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private var userId: Int = -1

    // Keep last ranges so we can refresh after edit/delete
    private var week1Range: Pair<String, String> = "" to ""
    private var week2Range: Pair<String, String> = "" to ""
    private var week3Range: Pair<String, String> = "" to ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeHistoryBinding.inflate(inflater, container, false)

        week1 = LayoutWeekSectionBinding.bind(binding.week1.root)
        week2 = LayoutWeekSectionBinding.bind(binding.week2.root)
        week3 = LayoutWeekSectionBinding.bind(binding.week3.root)

        setupWeekStaticUi(week1)
        setupWeekStaticUi(week2)
        setupWeekStaticUi(week3)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sp = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        userId = sp.getInt("user_id", -1)
        if (userId == -1) {
            toast("User not logged in")
            return
        }

        week1Range = getWeekRange(0)
        week2Range = getWeekRange(-1)
        week3Range = getWeekRange(-2)

        loadWeekFromServer(week1, "This Week", week1Range.first, week1Range.second)
        loadWeekFromServer(week2, "Last Week", week2Range.first, week2Range.second)
        loadWeekFromServer(week3, "2 Weeks Ago", week3Range.first, week3Range.second)
    }

    private fun setupWeekStaticUi(w: LayoutWeekSectionBinding) {
        w.rvActivityWeek.layoutManager = LinearLayoutManager(requireContext())
        w.rvWorkoutWeek.layoutManager = LinearLayoutManager(requireContext())

        // Default visible
        w.layoutActivityHistoryWeek.visibility = View.VISIBLE
        w.layoutWorkoutHistoryWeek.visibility = View.GONE

        val options = listOf("Activity history", "Workout history")
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, options)
        w.actHistoryTypeWeek.setAdapter(adapter)

        w.actHistoryTypeWeek.setOnClickListener { w.actHistoryTypeWeek.showDropDown() }

        w.actHistoryTypeWeek.setOnItemClickListener { _, _, position, _ ->
            if (position == 0) {
                w.layoutActivityHistoryWeek.visibility = View.VISIBLE
                w.layoutWorkoutHistoryWeek.visibility = View.GONE
            } else {
                w.layoutActivityHistoryWeek.visibility = View.GONE
                w.layoutWorkoutHistoryWeek.visibility = View.VISIBLE
            }
        }

        w.actHistoryTypeWeek.setText(options[0], false)
    }

    private fun loadWeekFromServer(w: LayoutWeekSectionBinding, title: String, startDate: String, endDate: String) {
        w.tvWeekTitle.text = "$title ($startDate → $endDate)"

        val req = object : StringRequest(
            Request.Method.POST,
            HISTORY_URL,
            StringRequest@{ response ->
                try {
                    val obj = JSONObject(response)
                    if (!obj.optBoolean("success", false)) {
                        toast(obj.optString("message", "Failed"))
                        return@StringRequest
                    }

                    // Chart
                    val chartArr = obj.optJSONArray("chart") ?: JSONArray()
                    val chartData = mutableListOf<Pair<String, Float>>()
                    for (i in 0 until chartArr.length()) {
                        val c = chartArr.getJSONObject(i)
                        chartData.add(c.getString("label") to c.getInt("value").toFloat())
                    }
                    w.barchartWeek.animation.duration = 900L
                    w.barchartWeek.animate(chartData)

                    // Activities
                    val actArr = obj.optJSONArray("activities") ?: JSONArray()
                    val activityItems = parseHistoryItems(actArr)
                    val actAdapter = HistoryAdapter(
                        activityItems,
                        onEdit = { item -> showEditDialog(item, onDone = { refreshAllWeeks() }) },
                        onDelete = { item -> confirmDelete(item, onDone = { refreshAllWeeks() }) }
                    )
                    w.rvActivityWeek.adapter = actAdapter

                    // Workouts
                    val wArr = obj.optJSONArray("workouts") ?: JSONArray()
                    val workoutItems = parseHistoryItems(wArr)
                    val wkAdapter = HistoryAdapter(
                        workoutItems,
                        onEdit = { item -> showEditDialog(item, onDone = { refreshAllWeeks() }) },
                        onDelete = { item -> confirmDelete(item, onDone = { refreshAllWeeks() }) }
                    )
                    w.rvWorkoutWeek.adapter = wkAdapter

                    w.tvWeekSub.text = "Activities: ${activityItems.size} | Workouts: ${workoutItems.size}"

                } catch (e: Exception) {
                    toast("Bad JSON from server")
                }
            },
            { toast("Network error loading history") }
        ) {
            override fun getParams(): MutableMap<String, String> = hashMapOf(
                "user_id" to userId.toString(),
                "start_date" to startDate,
                "end_date" to endDate
            )
        }

        Volley.newRequestQueue(requireContext()).add(req)
    }

    private fun parseHistoryItems(arr: JSONArray): List<HistoryItem> {
        val list = mutableListOf<HistoryItem>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                HistoryItem(
                    id = o.optInt("id"),
                    type = o.optString("type"),
                    title = o.optString("title"),
                    subtitle = o.optString("subtitle"),
                    startTime = o.optString("start_time"),
                    endTime = o.optString("end_time"),
                    durationMin = o.optInt("duration_minutes", 0),
                    intensity = o.optInt("intensity", 0),
                    reps = o.optInt("reps", 0),
                    weightLifted = o.optString("weight_lifted", ""),
                    notes = o.optString("notes", "")
                )
            )
        }
        return list
    }

    // -------------------- Delete --------------------
    private fun confirmDelete(item: HistoryItem, onDone: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete")
            .setMessage("Delete this item?\n\n${item.title}\n${item.subtitle}")
            .setPositiveButton("Delete") { _, _ ->
                deleteItem(item, onDone)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteItem(item: HistoryItem, onDone: () -> Unit) {
        val req = object : StringRequest(
            Method.POST,
            DELETE_URL,
            { resp ->
                val obj = JSONObject(resp)
                toast(obj.optString("message", "Deleted"))
                if (obj.optBoolean("success", false)) onDone()
            },
            { toast("Delete failed") }
        ) {
            override fun getParams(): MutableMap<String, String> = hashMapOf(
                "user_id" to userId.toString(),
                "type" to item.type,
                "id" to item.id.toString()
            )
        }
        Volley.newRequestQueue(requireContext()).add(req)
    }

    // -------------------- Edit --------------------
    private fun showEditDialog(item: HistoryItem, onDone: () -> Unit) {
        val wrap = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etStart = EditText(requireContext()).apply {
            hint = "Start time (HH:mm)"
            setText(item.startTime)
            isFocusable = false
            isClickable = true
            setOnClickListener { pickTime { setText(it) } }
        }

        val etEnd = EditText(requireContext()).apply {
            hint = "End time (HH:mm)"
            setText(item.endTime)
            isFocusable = false
            isClickable = true
            setOnClickListener { pickTime { setText(it) } }
        }

        val etNotes = EditText(requireContext()).apply {
            hint = "Notes"
            setText(item.notes)
        }

        wrap.addView(etStart)
        wrap.addView(etEnd)

        // Only show workout extra fields
        val etIntensity = EditText(requireContext()).apply { hint = "Intensity (0-100)"; setText(item.intensity.toString()) }
        val etReps = EditText(requireContext()).apply { hint = "Reps"; setText(item.reps.toString()) }
        val etWeight = EditText(requireContext()).apply { hint = "Weight (kg)"; setText(item.weightLifted) }

        if (item.type == "workout") {
            wrap.addView(etIntensity)
            wrap.addView(etReps)
            wrap.addView(etWeight)
            wrap.addView(etNotes)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit ${item.type}")
            .setView(wrap)
            .setPositiveButton("Save") { _, _ ->
                val start = etStart.text.toString().trim()
                val end = etEnd.text.toString().trim()
                val duration = computeDurationMinutes(start, end)
                if (duration <= 0) {
                    toast("End time must be after start time")
                    return@setPositiveButton
                }

                val intensity = if (item.type == "workout") etIntensity.text.toString().trim() else ""
                val reps = if (item.type == "workout") etReps.text.toString().trim() else ""
                val weight = if (item.type == "workout") etWeight.text.toString().trim() else ""
                val notes = if (item.type == "workout") etNotes.text.toString().trim() else ""

                updateItem(item, start, end, duration, intensity, reps, weight, notes, onDone)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateItem(
        item: HistoryItem,
        start: String,
        end: String,
        duration: Int,
        intensity: String,
        reps: String,
        weight: String,
        notes: String,
        onDone: () -> Unit
    ) {
        val req = object : StringRequest(
            Method.POST,
            UPDATE_URL,
            { resp ->
                val obj = JSONObject(resp)
                toast(obj.optString("message", "Updated"))
                if (obj.optBoolean("success", false)) onDone()
            },
            { toast("Update failed") }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val p = hashMapOf(
                    "user_id" to userId.toString(),
                    "type" to item.type,
                    "id" to item.id.toString(),
                    "start_time" to start,
                    "end_time" to end,
                    "duration_minutes" to duration.toString()
                )

                if (item.type == "workout") {
                    p["intensity"] = intensity
                    p["reps"] = reps
                    p["weight_lifted"] = weight
                    p["notes"] = notes
                }

                return p
            }
        }
        Volley.newRequestQueue(requireContext()).add(req)
    }

    private fun pickTime(onPicked: (String) -> Unit) {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hour, minute -> onPicked(String.format(Locale.US, "%02d:%02d", hour, minute)) },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    // supports midnight crossing
    private fun computeDurationMinutes(start: String, end: String): Int {
        return try {
            val sf = SimpleDateFormat("HH:mm", Locale.US)
            val s = sf.parse(start) ?: return 0
            val e = sf.parse(end) ?: return 0
            var diff = e.time - s.time
            if (diff < 0) diff += 24L * 60L * 60L * 1000L
            (diff / 60000.0).roundToInt()
        } catch (_: Exception) {
            0
        }
    }

    // -------------------- Refresh --------------------
    private fun refreshAllWeeks() {
        loadWeekFromServer(week1, "This Week", week1Range.first, week1Range.second)
        loadWeekFromServer(week2, "Last Week", week2Range.first, week2Range.second)
        loadWeekFromServer(week3, "2 Weeks Ago", week3Range.first, week3Range.second)
    }

    /** offsetWeeks: 0=this week, -1=last week, -2=two weeks ago */
    private fun getWeekRange(offsetWeeks: Int): Pair<String, String> {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.add(Calendar.WEEK_OF_YEAR, offsetWeeks)

        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val start = df.format(cal.time)

        cal.add(Calendar.DAY_OF_WEEK, 6)
        val end = df.format(cal.time)

        return start to end
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
