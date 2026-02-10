package com.example.fitnesstracker

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fitnesstracker.databinding.FragmentIntro3Binding
import com.example.fitnesstracker.databinding.FragmentIntro4Binding


class intro_4Fragment : Fragment() {

    private lateinit var binding: FragmentIntro4Binding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentIntro4Binding.inflate(inflater,container,false)

        binding.btnstart.setOnClickListener {
            val intent = Intent(context, LoginAndRegister_Activity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }


        // Inflate the layout for this fragment
        return binding.root
    }

}