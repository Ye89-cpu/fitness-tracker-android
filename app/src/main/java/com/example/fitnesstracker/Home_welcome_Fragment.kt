package com.example.fitnesstracker

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.fitnesstracker.databinding.FragmentHomeWelcomeBinding
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Home_welcome_Fragment : Fragment() {

    private var _binding: FragmentHomeWelcomeBinding? = null
    private val binding get() = _binding!!

    private val BASE_URL = "http://10.0.2.2/fitness_tracker/"
    private val GET_GOALS_URL = BASE_URL + "get_goals.php"
    private val SET_GOALS_URL = BASE_URL + "set_goals.php"
    private val GET_SUMMARY_URL = BASE_URL + "get_summary.php"
    private val RESET_GOALS_URL = BASE_URL + "reset_goals.php"

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private var userId = -1
    private var activityValue = 0
    private var caloriesValue = 0
    private val CAL_STEP = 200

    private val MAX_CALORIES = 6000

    private var lat: Double? = null
    private var lng: Double? = null
    private var city: String = ""
    private var country: String = ""
    private var addressLine: String = ""

    private var saveAfterLocation = false

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private val locationPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) fetchAndFillLocation()
            else {
                saveAfterLocation = false
                toast("Location permission denied.")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sp = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        userId = sp.getInt("user_id", -1)

        val displayName =
            sp.getString("full_name", null)?.takeIf { it.isNotBlank() }
                ?: sp.getString("username", "") ?: "User"

        binding.tvUserName.text = displayName

        if (userId <= 0) {
            toast("Please login again")
            return
        }

        // Date pickers
        binding.etFromDate.setOnClickListener {
            if (binding.etFromDate.isEnabled) pickDate { binding.etFromDate.setText(it) }
        }
        binding.etToDate.setOnClickListener {
            if (binding.etToDate.isEnabled) pickDate { binding.etToDate.setText(it) }
        }

        // FAB location
        binding.fabSetLocation.setOnClickListener {
            toast("Getting location...")
            requestLocationOrFetch()
        }

        // +/- activity
        binding.btnDailyPlus.setOnClickListener {
            activityValue++
            binding.tvDailyActivityValue.text = activityValue.toString()
        }
        binding.btnDailyMinus.setOnClickListener {
            if (activityValue > 0) activityValue--
            binding.tvDailyActivityValue.text = activityValue.toString()
        }

        // +/- calories


        binding.btnCaloriesPlus.setOnClickListener {
            caloriesValue = (caloriesValue + CAL_STEP).coerceAtMost(MAX_CALORIES)
            binding.tvEditableCaloriesValue.text = caloriesValue.toString()
        }

        binding.btnCaloriesMinus.setOnClickListener {
            caloriesValue = (caloriesValue - CAL_STEP).coerceAtLeast(0)
            binding.tvEditableCaloriesValue.text = caloriesValue.toString()
        }

        binding.btnsavegoal.setOnClickListener { onSaveGoalClicked() }
        binding.btngoalreset.setOnClickListener { resetGoalOnServer() }

        // Default UI state: show inputs until we know server state
        showEditMode()
        loadGoalsFromServer()
    }

    private fun pickDate(onPicked: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                val picked = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0) }
                onPicked(dateFmt.format(picked.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun onSaveGoalClicked() {
        val startDate = binding.etFromDate.text.toString().trim()
        val endDate = binding.etToDate.text.toString().trim()

        if (startDate.isBlank() || endDate.isBlank()) {
            toast("Please select dates")
            return
        }
        if (activityValue <= 0 || caloriesValue <= 0) {
            toast("Set activity + calories target")
            return
        }

        if (lat == null || lng == null) {
            toast("Please set location first (tap FAB)")
            saveAfterLocation = true
            requestLocationOrFetch()
            return
        }

        saveGoalsToServer()
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    private fun requestLocationOrFetch() {
        if (hasLocationPermission()) fetchAndFillLocation()
        else locationPermLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // ALWAYS FRESH LOCATION (no lastLocation cache)
    @SuppressLint("MissingPermission")
    private fun fetchAndFillLocation() {
        if (!hasLocationPermission()) {
            saveAfterLocation = false
            toast("Location permission not granted")
            return
        }

        val lm = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!enabled) {
            saveAfterLocation = false
            toast("Turn ON Location (GPS)")
            return
        }

        toast("Getting fresh GPS... (Emulator: press SET LOCATION)")

        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500L)
            .setMinUpdateIntervalMillis(200L)
            .setMaxUpdates(1)
            .build()

        val cb = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)
                val loc = result.lastLocation
                if (loc != null) {
                    lat = loc.latitude
                    lng = loc.longitude
                    toast("GPS: $lat, $lng")
                    reverseGeocode(lat!!, lng!!)
                } else {
                    saveAfterLocation = false
                    toast("No GPS fix yet. Try again after SET LOCATION.")
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(req, cb, Looper.getMainLooper())

        // fallback after 2 seconds (in case emulator delays GPS)
        binding.root.postDelayed({
            if (lat == null || lng == null) {
                fusedLocationClient.lastLocation.addOnSuccessListener { last ->
                    if (last != null) {
                        lat = last.latitude
                        lng = last.longitude
                        toast("Using last location: $lat, $lng")
                        reverseGeocode(lat!!, lng!!)
                    }
                }
            }
        }, 2000)
    }

    private fun reverseGeocode(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        runCatching {
            if (Build.VERSION.SDK_INT >= 33) {
                geocoder.getFromLocation(latitude, longitude, 1) { list ->
                    if (!isAdded) return@getFromLocation
                    applyAddress(list.firstOrNull(), latitude, longitude)
                }
            } else {
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocation(latitude, longitude, 1)
                if (!isAdded) return
                applyAddress(list?.firstOrNull(), latitude, longitude)
            }
        }.onFailure {
            if (!isAdded) return@onFailure
            applyAddress(null, latitude, longitude)
        }
    }

    private fun applyAddress(addr: Address?, latitude: Double, longitude: Double) {
        val newCity = addr?.locality ?: addr?.subAdminArea ?: ""
        val newCountry = addr?.countryName ?: ""
        val newAddressLine = addr?.getAddressLine(0) ?: ""

        val showText = when {
            newAddressLine.isNotBlank() -> newAddressLine
            newCity.isNotBlank() || newCountry.isNotBlank() ->
                listOf(newCity, newCountry).filter { it.isNotBlank() }.joinToString(", ")
            else -> "Lat: $latitude, Lng: $longitude"
        }

        requireActivity().runOnUiThread {
            city = newCity
            country = newCountry
            addressLine = newAddressLine

            binding.tvLocation.text = showText
            toast("Location set ✅")

            if (saveAfterLocation) {
                saveAfterLocation = false
                saveGoalsToServer()
            }
        }
    }

    /**
     * UI STATES
     * Edit Mode: show goal inputs + Save button, hide Reset
     * View Mode: hide goal inputs + Save, show Reset
     */
    private fun showEditMode() {
        // show containers (better than hiding only editTexts)
        binding.tilFromDate.visibility = View.VISIBLE
        binding.tilToDate.visibility = View.VISIBLE
        binding.layoutDailyActivityTarget.visibility = View.VISIBLE
        binding.layoutEditableCaloriesTarget.visibility = View.VISIBLE
        binding.cardLocation.visibility = View.VISIBLE
        binding.btnsavegoal.visibility = View.VISIBLE

        binding.btngoalreset.visibility = View.GONE

        // enable interactions
        binding.etFromDate.isEnabled = true
        binding.etToDate.isEnabled = true
        binding.btnDailyPlus.isEnabled = true
        binding.btnDailyMinus.isEnabled = true
        binding.btnCaloriesPlus.isEnabled = true
        binding.btnCaloriesMinus.isEnabled = true

        binding.fabSetLocation.isEnabled = true
        binding.fabSetLocation.alpha = 1f
    }

    private fun showViewMode() {
        binding.tilFromDate.visibility = View.GONE
        binding.tilToDate.visibility = View.GONE
        binding.layoutDailyActivityTarget.visibility = View.GONE
        binding.layoutEditableCaloriesTarget.visibility = View.GONE
        binding.cardLocation.visibility = View.GONE
        binding.btnsavegoal.visibility = View.GONE

        binding.btngoalreset.visibility = View.VISIBLE

        // optional: disable to avoid accidental edits
        binding.etFromDate.isEnabled = false
        binding.etToDate.isEnabled = false
        binding.btnDailyPlus.isEnabled = false
        binding.btnDailyMinus.isEnabled = false
        binding.btnCaloriesPlus.isEnabled = false
        binding.btnCaloriesMinus.isEnabled = false

        binding.fabSetLocation.isEnabled = false
        binding.fabSetLocation.alpha = 0.4f
    }

    private fun loadGoalsFromServer() {
        val req = object : StringRequest(
            Request.Method.POST,
            GET_GOALS_URL,
            StringRequest@{ response ->
                try {
                    val obj = JSONObject(response)
                    val ok = obj.optString("status").equals("success", true)

                    if (!ok) {
                        resetGoalUI()
                        showEditMode()
                        return@StringRequest
                    }

                    val dailyActivity = obj.optInt("daily_activity_target", 0)
                    val dailyCalories = obj.optInt("daily_calories_target", 0)
                    val startDate = obj.optString("start_date", "")
                    val endDate = obj.optString("end_date", "")

                    lat = obj.optString("latitude", "").toDoubleOrNull()
                    lng = obj.optString("longitude", "").toDoubleOrNull()
                    city = obj.optString("city", "")
                    country = obj.optString("country", "")
                    addressLine = obj.optString("address_line", "")

                    binding.tvLocation.text =
                        addressLine.ifBlank {
                            listOf(city, country).filter { it.isNotBlank() }.joinToString(", ")
                                .ifBlank {
                                    if (lat != null && lng != null) "Lat: $lat, Lng: $lng" else "Not set"
                                }
                        }

                    binding.tvDailyWorkoutTarget.text = dailyActivity.toString()
                    binding.tvTargetCaloriesDay.text = dailyCalories.toString()

                    activityValue = dailyActivity
                    caloriesValue = dailyCalories
                    binding.tvDailyActivityValue.text = activityValue.toString()
                    binding.tvEditableCaloriesValue.text = caloriesValue.toString()

                    binding.etFromDate.setText(startDate)
                    binding.etToDate.setText(endDate)

                    // ✅ Goal exists => view mode (hide inputs, show reset)
                    showViewMode()

                    loadSummaryAndApplyLogic(startDate, endDate)
                } catch (_: Exception) {
                    toast("JSON error")
                    // on error, keep edit mode so user can try again
                    showEditMode()
                }
            },
            { error ->
                toast(volleyErrorText(error))
                // network error: keep edit mode
                showEditMode()
            }
        ) {
            override fun getParams(): MutableMap<String, String> =
                hashMapOf("user_id" to userId.toString())
        }
        Volley.newRequestQueue(requireContext()).add(req)
    }

    private fun loadSummaryAndApplyLogic(from: String, to: String) {
        val req = object : StringRequest(
            Request.Method.POST,
            GET_SUMMARY_URL,
            { response ->
                runCatching {
                    val obj = JSONObject(response)
                    if (!obj.optString("status").equals("success", true)) return@runCatching

                    binding.tvBurnedCalories.text = obj.optInt("burned_calories", 0).toString()
                    binding.tvFinishedActivity.text = obj.optInt("finished_activity", 0).toString()
                }
            },
            { }
        ) {
            override fun getParams(): MutableMap<String, String> =
                hashMapOf(
                    "user_id" to userId.toString(),
                    "start_date" to from,
                    "end_date" to to
                )
        }
        Volley.newRequestQueue(requireContext()).add(req)
    }

    private fun saveGoalsToServer() {
        val startDate = binding.etFromDate.text.toString().trim()
        val endDate = binding.etToDate.text.toString().trim()

        if (lat == null || lng == null) {
            toast("Location is required")
            return
        }

        val req = object : StringRequest(
            Request.Method.POST,
            SET_GOALS_URL,
            {
                toast("Goal saved ✅")
                // ✅ Immediately switch UI to view mode
                showViewMode()
                // ✅ Refresh from server to ensure correct state
                loadGoalsFromServer()
            },
            { error -> toast(volleyErrorText(error)) }
        ) {
            override fun getParams(): MutableMap<String, String> =
                hashMapOf(
                    "user_id" to userId.toString(),
                    "daily_activity_target" to activityValue.toString(),
                    "daily_calories_target" to caloriesValue.toString(),
                    "start_date" to startDate,
                    "end_date" to endDate,
                    "latitude" to lat!!.toString(),
                    "longitude" to lng!!.toString(),
                    "city" to city,
                    "country" to country,
                    "address_line" to addressLine
                )
        }
        Volley.newRequestQueue(requireContext()).add(req)
    }

    private fun resetGoalOnServer() {
        val req = object : StringRequest(
            Request.Method.POST,
            RESET_GOALS_URL,
            {
                resetGoalUI()
                toast("Goal reset ✅")
                // ✅ back to edit mode
                showEditMode()
            },
            { error -> toast(volleyErrorText(error)) }
        ) {
            override fun getParams(): MutableMap<String, String> =
                hashMapOf("user_id" to userId.toString())
        }
        Volley.newRequestQueue(requireContext()).add(req)
    }

    private fun resetGoalUI() {
        lat = null; lng = null; city = ""; country = ""; addressLine = ""
        binding.tvLocation.text = "Not set"

        // optional: clear form values too
        binding.etFromDate.setText("")
        binding.etToDate.setText("")
        activityValue = 0
        caloriesValue = 0
        binding.tvDailyActivityValue.text = "0"
        binding.tvEditableCaloriesValue.text = "0"
    }

    private fun volleyErrorText(error: com.android.volley.VolleyError): String {
        val code = error.networkResponse?.statusCode
        return code?.let { "Server error $it" } ?: "Network error"
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
