package com.example.fitnesstracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.fitnesstracker.databinding.FragmentHomeProfileBinding
import org.json.JSONObject
import com.bumptech.glide.Glide
import java.util.Locale

class Home_Profile_Fragment : Fragment() {

    private lateinit var binding: FragmentHomeProfileBinding

    private val BASE_URL = "http://10.0.2.2/fitness_tracker/"
    private val GET_PROFILE = BASE_URL + "get_profile.php"

    private var userId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeProfileBinding.inflate(inflater, container, false)

        val sp = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        userId = sp.getInt("user_id", 0)

        if (userId <= 0) {
            toast("Missing user_id, login again")
            return binding.root
        }

        fetchProfile()
        setupClicks()

        binding.rowProfile.setOnClickListener {
            findNavController().navigate(R.id.action_home_Profile_Fragment_to_home_Profile_Edit_Fragment)
        }
        binding.rowPrivacy.setOnClickListener {
            findNavController().navigate(R.id.action_home_Profile_Fragment_to_privacyPolicyFragment)
        }
        binding.rowHelp.setOnClickListener {
            findNavController().navigate(R.id.action_home_Profile_Fragment_to_helpFragment)
        }

        return binding.root
    }

    private fun setupClicks() = with(binding) {
        rowLogout.setOnClickListener {
            val sp = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
            sp.edit().clear().apply()
            toast("Logged out")
            val intent = Intent(context, LoginAndRegister_Activity::class.java)
            startActivity(intent)
            requireActivity().finish()
            // TODO: navigate to login activity/fragment if you have
        }
    }

    private fun fetchProfile() {
        val req = object : StringRequest(
            Request.Method.POST,
            GET_PROFILE,
            StringRequest@{ resp ->
                Log.d("PROFILE", resp)
                try {
                    val obj = JSONObject(resp)
                    if (!obj.optString("status").equals("success", true)) {
                        toast(obj.optString("message", "Failed"))
                        return@StringRequest
                    }

                    val p = obj.getJSONObject("profile")

                    val name = p.optString("full_name", "User")
                    val email = p.optString("email", "")
                    val weight = p.optInt("weight_kg", 0)
                    val age = p.optInt("age", 0)
                    val heightCm = p.optInt("height_cm", 0)
                    val imgPath = p.optString("profile_image", "")

                    if (!imgPath.isNullOrBlank() && imgPath != "null") {
                        val fullUrl = if (imgPath.startsWith("http")) {
                            imgPath
                        } else {
                            BASE_URL + imgPath.trimStart('/')
                        }

                        Glide.with(this@Home_Profile_Fragment)
                            .load(fullUrl)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .circleCrop()
                            .into(binding.ivProfile)
                    } else {
                        binding.ivProfile.setImageResource(R.drawable.ic_profile)
                    }


                    binding.tvName.text = name
                    binding.tvEmail.text = email

                    binding.tvWeight.text = if (weight > 0) "$weight Kg" else "-"
                    binding.tvAge.text = if (age > 0) age.toString() else "-"
                    binding.tvHeight.text = if (heightCm > 0) "${heightCm} CM" else "-"

                } catch (e: Exception) {
                    toast("Bad JSON: ${e.message}")
                }
            },
            { err ->
                toast("Network error: ${err.message}")
            }
        ) {
            override fun getParams(): MutableMap<String, String> =
                hashMapOf("user_id" to userId.toString())
        }

        Volley.newRequestQueue(requireContext()).add(req)
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
