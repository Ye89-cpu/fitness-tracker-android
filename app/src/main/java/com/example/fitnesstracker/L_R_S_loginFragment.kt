package com.example.fitnesstracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.fitnesstracker.databinding.FragmentLRSLoginBinding
import org.json.JSONObject

class L_R_S_loginFragment : Fragment() {

    private var _binding: FragmentLRSLoginBinding? = null
    private val binding get() = _binding!!

    private val LOGIN_URL = "http://10.0.2.2/fitness_tracker/login_user.php"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLRSLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener { submitLogin() }

        binding.backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_l_R_S_loginFragment_to_l_R_S_Fragment)
        }

        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_l_R_S_loginFragment_to_l_R_S_Gender_Fragment)
        }
    }

    private fun submitLogin() {

        val username = binding.etUname.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            toast("Enter username and password")
            return
        }

        doLoginRequest(username, password)
    }

    private fun doLoginRequest(username: String, password: String) {
        binding.btnLogin.isEnabled = false

        val req = object : StringRequest(
            Request.Method.POST,
            LOGIN_URL,
            listener@{ response ->   //  label the lambda

                binding.btnLogin.isEnabled = true
                Log.d("LOGIN", "Response: $response")

                try {
                    val obj = JSONObject(response)
                    val success = obj.optBoolean("success", false)
                    val message = obj.optString("message", "No message")

                    if (!success) {
                        toast(message)
                        return@listener  // return from THIS lambda
                    }

                    // If PHP returns: { success, message, user: {...} }
                    val userObj = obj.optJSONObject("user")

                    // If PHP returns: { success, message, data: { user: {...} } }
                    // val userObj = obj.optJSONObject("data")?.optJSONObject("user")

                    val userId = userObj?.optInt("id", -1) ?: -1
                    val fullName = userObj?.optString("full_name", "") ?: ""
                    val usernameResp = userObj?.optString("username", "") ?: ""
                    val email = userObj?.optString("email", "") ?: ""

                    val sp = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
                    sp.edit()
                        .putBoolean("logged_in", true)
                        .putInt("user_id", userId)
                        .putString("full_name", fullName)
                        .putString("username", usernameResp)
                        .putString("email", email)
                        .apply()

                    toast("Login ✅ Welcome $usernameResp")

                    // Go to HomeActivity with extras
                    val intent = Intent(requireContext(), HomeActivity::class.java).apply {
                        putExtra("user_id", userId)
                        putExtra("username", usernameResp)
                    }
                    startActivity(intent)
                    requireActivity().finish()

                } catch (e: Exception) {
                    toast("Bad JSON: ${e.message}")
                    Log.e("LOGIN", "JSON error", e)
                }
            },
            { error ->
                binding.btnLogin.isEnabled = true
                toast("Network error: ${error.message}")
                Log.e("LOGIN", "Volley error", error)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "username" to username,
                    "password" to password
                )
            }
        }

        Volley.newRequestQueue(requireContext()).add(req)
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
