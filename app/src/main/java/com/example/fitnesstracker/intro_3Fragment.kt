package com.example.fitnesstracker

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.databinding.FragmentIntro3Binding


class intro_3Fragment : Fragment() {

    private lateinit var binding: FragmentIntro3Binding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentIntro3Binding.inflate(inflater,container,false)

        binding.nextBtn.setOnClickListener {
            val action = intro_3FragmentDirections.actionIntro3FragmentToIntro4Fragment()
            findNavController().navigate(action)
        }
        binding.skipText.setOnClickListener {
            val intent = Intent(context, LoginAndRegister_Activity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        // Inflate the layout for this fragment
        return binding.root
    }


}