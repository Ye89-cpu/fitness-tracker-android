package com.example.fitnesstracker

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitnesstracker.databinding.ActivityLoginAndRegisterBinding

class LoginAndRegister_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginAndRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginAndRegisterBinding.inflate(layoutInflater)


        setContentView(binding.root)



    }
}