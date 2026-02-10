package com.example.fitnesstracker

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.fitnesstracker.databinding.FragmentHomeProfileEditBinding
import org.json.JSONObject

class Home_Profile_Edit_Fragment : Fragment() {

    private lateinit var binding: FragmentHomeProfileEditBinding

    private val BASE_URL = "http://10.0.2.2/fitness_tracker/"
    private val GET_PROFILE = BASE_URL + "get_profile.php"
    private val UPDATE_PROFILE = BASE_URL + "update_profile.php"
    private val CHANGE_PWD = BASE_URL + "change_password.php"
    private val UPLOAD_IMG = BASE_URL + "upload_profile_image.php"

    private var userId = 0

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                binding.ivProfile.setImageURI(uri) // preview
                uploadProfileImage(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeProfileEditBinding.inflate(inflater, container, false)

        val sp = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        userId = sp.getInt("user_id", 0)

        if (userId <= 0) {
            toast("Missing user_id. Login again.")
            return binding.root
        }

        setupClicks()
        fetchProfileToForm()

        return binding.root
    }

    private fun setupClicks() = with(binding) {
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        btnChangePhoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        ivProfile.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnSave.setOnClickListener {
            updateProfile()
        }

        btnChangePassword.setOnClickListener {
            changePassword()
        }
    }

    private fun fetchProfileToForm() {
        val req = object : StringRequest(
            Request.Method.POST,
            GET_PROFILE,
            StringRequest@{ resp ->
                Log.d("EDIT_PROFILE", resp)
                try {
                    val obj = JSONObject(resp)
                    if (!obj.optString("status").equals("success", true)) {
                        toast(obj.optString("message", "Failed"))
                        return@StringRequest
                    }

                    val p = obj.getJSONObject("profile")

                    binding.etFullName.setText(p.optString("full_name", ""))
                    binding.etPhone.setText(p.optString("phone", ""))
                    binding.etGender.setText(p.optString("gender", ""))
                    binding.etAge.setText(if (p.isNull("age")) "" else p.optString("age"))
                    binding.etWeight.setText(if (p.isNull("weight_kg")) "" else p.optString("weight_kg"))
                    binding.etHeight.setText(if (p.isNull("height_cm")) "" else p.optString("height_cm"))

                    val imgPath = p.optString("profile_image", "")
                    if (imgPath.isNotBlank() && imgPath != "null") {
                        Glide.with(this).load(BASE_URL + imgPath).into(binding.ivProfile)
                    }

                } catch (e: Exception) {
                    toast("Bad JSON: ${e.message}")
                }
            },
            { err -> toast("Network error: ${err.message}") }
        ) {
            override fun getParams(): MutableMap<String, String> =
                hashMapOf("user_id" to userId.toString())
        }

        Volley.newRequestQueue(requireContext()).add(req)
    }

    private fun updateProfile() {
        val fullName = binding.etFullName.text?.toString()?.trim().orEmpty()
        val phone = binding.etPhone.text?.toString()?.trim().orEmpty()
        val gender = binding.etGender.text?.toString()?.trim().orEmpty()
        val age = binding.etAge.text?.toString()?.trim().orEmpty()
        val weight = binding.etWeight.text?.toString()?.trim().orEmpty()
        val height = binding.etHeight.text?.toString()?.trim().orEmpty()

        if (fullName.isBlank()) {
            toast("Full name required")
            return
        }

        val req = object : StringRequest(
            Request.Method.POST,
            UPDATE_PROFILE,
            StringRequest@{ resp ->
                try {
                    val obj = JSONObject(resp)
                    if (!obj.optString("status").equals("success", true)) {
                        toast(obj.optString("message", "Update failed"))
                        return@StringRequest
                    }
                    toast("Profile updated ✅")
                    fetchProfileToForm()
                } catch (e: Exception) {
                    toast("Bad JSON: ${e.message}")
                }
            },
            { err -> toast("Network error: ${err.message}") }
        ) {
            override fun getParams(): MutableMap<String, String> = hashMapOf(
                "user_id" to userId.toString(),
                "full_name" to fullName,
                "phone" to phone,
                "gender" to gender,
                "age" to age,
                "weight_kg" to weight,
                "height_cm" to height
            )
        }

        Volley.newRequestQueue(requireContext()).add(req)
    }

    private fun changePassword() {
        val oldPass = binding.etOldPassword.text?.toString()?.trim().orEmpty()
        val newPass = binding.etNewPassword.text?.toString()?.trim().orEmpty()

        if (oldPass.isBlank() || newPass.isBlank()) {
            toast("Enter old and new password")
            return
        }
        if (newPass.length < 6) {
            toast("New password must be at least 6 characters")
            return
        }

        val req = object : StringRequest(
            Request.Method.POST,
            CHANGE_PWD,
            StringRequest@{ resp ->
                try {
                    val obj = JSONObject(resp)
                    if (!obj.optString("status").equals("success", true)) {
                        toast(obj.optString("message", "Failed"))
                        return@StringRequest
                    }
                    toast("Password changed ✅")
                    binding.etOldPassword.setText("")
                    binding.etNewPassword.setText("")
                } catch (e: Exception) {
                    toast("Bad JSON: ${e.message}")
                }
            },
            { err -> toast("Network error: ${err.message}") }
        ) {
            override fun getParams(): MutableMap<String, String> = hashMapOf(
                "user_id" to userId.toString(),
                "old_password" to oldPass,
                "new_password" to newPass
            )
        }

        Volley.newRequestQueue(requireContext()).add(req)
    }

    private fun uploadProfileImage(uri: Uri) {
        val bytes = requireContext().contentResolver.openInputStream(uri)?.readBytes()
        if (bytes == null) {
            toast("Can't read image")
            return
        }

        val req = object : VolleyMultipartRequest(
            Method.POST, UPLOAD_IMG,
            { networkResponse ->
                try {
                    val text = String(networkResponse.data)
                    val obj = JSONObject(text)

                    val ok = obj.optString("status").equals("success", true)
                    if (!ok) {
                        toast(obj.optString("message", "Upload failed"))
                    } else {
                        toast("Photo updated ✅")
                        fetchProfileToForm()
                    }

                } catch (e: Exception) {
                    toast("Bad JSON: ${e.message}")
                }
            },
            { err ->
                toast("Upload error: ${err.message}")
            }
        ) {
            override fun getParams(): MutableMap<String, String> =
                hashMapOf("user_id" to userId.toString())

            override fun getByteData(): Map<String, DataPart> {
                return mapOf("image" to DataPart("profile.jpg", bytes, "image/jpeg"))
            }
        }

        Volley.newRequestQueue(requireContext()).add(req)
    }


    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
