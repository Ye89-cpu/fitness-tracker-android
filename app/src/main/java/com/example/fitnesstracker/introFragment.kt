package com.example.fitnesstracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.databinding.FragmentIntroBinding

class introFragment : Fragment() {

    private lateinit var binding: FragmentIntroBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentIntroBinding.inflate(inflater,container,false)

        binding.btnstart.setOnClickListener {
            val action = introFragmentDirections.actionIntroFragmentToIntro2Fragment2()
            findNavController().navigate(action)
        }

        return binding.root
    }


}