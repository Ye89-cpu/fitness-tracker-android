package com.example.fitnesstracker

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.databinding.FragmentLRSUserRegisterBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class L_R_S_UserRegister_Fragment : Fragment() {

    private var _binding: FragmentLRSUserRegisterBinding? = null
    private val binding get() = _binding!!

    private val REGISTER_URL = "http://10.0.2.2/fitness_tracker/register_user.php"

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                binding.ivProfile.setImageURI(uri)
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLRSUserRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvBack.setOnClickListener {
            val action = L_R_S_UserRegister_FragmentDirections
                .actionLRSUserRegisterFragmentToLRSPhyALFragment()
            findNavController().navigate(action)
        }

        //  Photo upload only
        binding.btnChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnStart.setOnClickListener { submitRegister() }
    }

    private fun submitRegister() {
        val fullName = binding.etFullName.text.toString().trim()
        val username = binding.etNickname.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val phoneRaw = binding.etMobile.text.toString().trim()

// 1) Empty check
        if (fullName.isBlank() || username.isBlank() || email.isBlank() || password.isBlank() || phoneRaw.isBlank()) {
            toast("Please fill all fields")
            return
        }

// 2) Full name validations
        if (fullName.length < 3) {
            toast("Full name is too short")
            return
        }
        if (!fullName.contains(" ")) { // optional: require first + last name
            toast("Please enter your first and last name")
            return
        }
// optional strict: letters + space only (allows Myanmar letters too)
        if (!fullName.matches(Regex("^[\\p{L} .'-]{3,50}$"))) {
            toast("Full name contains invalid characters")
            return
        }

// 3) Username validations
        if (username.length !in 3..20) {
            toast("Username must be 3–20 characters")
            return
        }
        if (username.contains(" ")) {
            toast("Username must not contain spaces")
            return
        }
        if (!username.matches(Regex("^[a-zA-Z0-9_.]+$"))) {
            toast("Username can use letters, numbers, _ and . only")
            return
        }

// 4) Email validations
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Invalid email")
            return
        }

// 5) Password validations (stronger)
        if (password.length < 8) {
            toast("Password must be at least 8 characters")
            return
        }
        if (password.contains(" ")) {
            toast("Password must not contain spaces")
            return
        }
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        if (!(hasUpper && hasLower && hasDigit)) {
            toast("Password must include upper, lower, and number")
            return
        }
// optional: prevent weak password = username/email
        if (password.equals(username, ignoreCase = true) || password.equals(email, ignoreCase = true)) {
            toast("Password is too easy to guess")
            return
        }

// 6) Phone validations (Myanmar-friendly)
        val phone = phoneRaw
            .replace(" ", "")
            .replace("-", "")

        val digitsOnly = phone.replace("+", "")
        if (!digitsOnly.all { it.isDigit() }) {
            toast("Phone number must contain digits only")
            return
        }

        val normalizedPhone =
            when {
                phone.startsWith("09") -> "+959" + phone.drop(2)
                phone.startsWith("+959") -> phone
                phone.startsWith("959") -> "+$phone"
                else -> phone // keep as is for other countries
            }

        val normalizedDigits = normalizedPhone.replace("+", "")
        if (normalizedDigits.length !in 7..15) {
            toast("Invalid phone number length")
            return
        }

// ✅ If all good, continue register logic...


        // gender from object (no UI)
        if (UserRegisterData.gender.isNullOrBlank()) UserRegisterData.gender = "male"

        // Save holder (optional)
        UserRegisterData.fullName = fullName
        UserRegisterData.username = username
        UserRegisterData.email = email
        UserRegisterData.password = password
        UserRegisterData.phone = phone

        //  Call multipart upload
        uploadRegisterMultipart(
            fullName = fullName,
            username = username,
            email = email,
            password = password,
            phone = phone,
            gender = UserRegisterData.gender ?: "male",
            age = UserRegisterData.age,
            weightKg = UserRegisterData.weightKg,
            heightCm = UserRegisterData.heightCm,
            goal = UserRegisterData.goal,
            activityLevel = UserRegisterData.activityLevel,
            imageUri = selectedImageUri // can be null (no photo)
        )
    }

    private fun uploadRegisterMultipart(
        fullName: String,
        username: String,
        email: String,
        password: String,
        phone: String,
        gender: String,
        age: Int?,
        weightKg: Int?,
        heightCm: Int?,
        goal: String?,
        activityLevel: String?,
        imageUri: Uri?
    ) {
        binding.btnStart.isEnabled = false

        Thread {
            try {
                val client = OkHttpClient()

                val form = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("full_name", fullName)
                    .addFormDataPart("username", username)
                    .addFormDataPart("email", email)
                    .addFormDataPart("password", password)
                    .addFormDataPart("phone", phone)
                    .addFormDataPart("gender", gender)
                    .addFormDataPart("age", age?.toString() ?: "")
                    .addFormDataPart("weight_kg", weightKg?.toString() ?: "")
                    .addFormDataPart("height_cm", heightCm?.toString() ?: "")
                    .addFormDataPart("goal", goal ?: "")
                    .addFormDataPart("activity_level", activityLevel ?: "")

                //  attach file if selected
                if (imageUri != null) {
                    val file = copyUriToTempFile(imageUri)
                    val mime = guessMime(requireContext().contentResolver, imageUri) ?: "image/*"
                    val body = file.asRequestBody(mime.toMediaTypeOrNull())
                    form.addFormDataPart("profile_image", file.name, body)
                }

                val request = Request.Builder()
                    .url(REGISTER_URL)
                    .post(form.build())
                    .build()

                val response = client.newCall(request).execute()
                val raw = response.body?.string().orEmpty()

                requireActivity().runOnUiThread {
                    binding.btnStart.isEnabled = true
                    if (!response.isSuccessful) {
                        toast("Server error: ${response.code}")
                        return@runOnUiThread
                    }

                    try {
                        val obj = JSONObject(raw)
                        val success = obj.optBoolean("success", false)
                        val message = obj.optString("message", "No message")
                        if (success) {
                            val userId = obj.optInt("user_id", -1)
                            toast("Registered ✅ (id=$userId)")
                            val action = L_R_S_UserRegister_FragmentDirections
                                .actionLRSUserRegisterFragmentToLRSLoginFragment()
                            findNavController().navigate(action)
                        } else {
                            toast(message)
                        }
                    } catch (e: Exception) {
                        toast("Bad JSON: ${e.message}")
                    }
                }

            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    binding.btnStart.isEnabled = true
                    toast("Upload failed: ${e.message}")
                }
            }
        }.start()
    }

    private fun copyUriToTempFile(uri: Uri): File {
        val resolver = requireContext().contentResolver
        val ext = guessExtension(resolver, uri) ?: "jpg"
        val file = File(requireContext().cacheDir, "profile_${System.currentTimeMillis()}.$ext")

        resolver.openInputStream(uri).use { input ->
            FileOutputStream(file).use { output ->
                if (input != null) input.copyTo(output)
            }
        }
        return file
    }

    private fun guessExtension(resolver: ContentResolver, uri: Uri): String? {
        val type = resolver.getType(uri) ?: return null
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
    }

    private fun guessMime(resolver: ContentResolver, uri: Uri): String? {
        return resolver.getType(uri)
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
