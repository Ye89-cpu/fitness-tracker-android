package com.example.fitnesstracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.databinding.FragmentLRSBinding


class L_R_S_Fragment : Fragment() {

    private lateinit var binding: FragmentLRSBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLRSBinding.inflate(inflater,container,false)

        binding.btnLogin.setOnClickListener {
            val action = L_R_S_FragmentDirections.actionLRSFragmentToLRSLoginFragment()
            findNavController().navigate(action)
        }
        binding.btnRegister.setOnClickListener {
            val action = L_R_S_FragmentDirections.actionLRSFragmentToLRSGenderFragment()
            findNavController().navigate(action)
        }

        // Inflate the layout for this fragment
        return binding.root
    }


}