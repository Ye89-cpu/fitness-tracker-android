package com.example.fitnesstracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.databinding.FragmentLRSPhyALBinding


class L_R_S_PhyALFragment : Fragment() {
    private lateinit var binding: FragmentLRSPhyALBinding
    private var selectedLevel: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLRSPhyALBinding.inflate(inflater, container, false)

        binding.backBtn.setOnClickListener {
            findNavController().navigate(
                R.id.action_l_R_S_PhyALFragment_to_l_R_S_Goal_Fragment6
            )
        }


        setupButtons()
        setupContinueButton()

        return binding.root
    }
    private fun setupButtons() = with(binding) {

        val list = listOf(btnBeginner, btnIntermediate, btnAdvance)

        fun resetAll() {
            list.forEach {
                it.setBackgroundResource(R.drawable.bg_level_unselected)
                it.setTextColor(resources.getColor(R.color.icon_inactive))
            }
        }

        btnBeginner.setOnClickListener {
            resetAll()
            btnBeginner.setBackgroundResource(R.drawable.bg_level_selected)
            btnBeginner.setTextColor(resources.getColor(R.color.text_on_accent))
            selectedLevel = "Beginner"
            UserRegisterData.activityLevel = "Beginner"    // <---
        }

        btnIntermediate.setOnClickListener {
            resetAll()
            btnIntermediate.setBackgroundResource(R.drawable.bg_level_selected)
            btnIntermediate.setTextColor(resources.getColor(R.color.text_on_accent))
            selectedLevel = "Intermediate"
            UserRegisterData.activityLevel = "Intermediate"
        }

        btnAdvance.setOnClickListener {
            resetAll()
            btnAdvance.setBackgroundResource(R.drawable.bg_level_selected)
            btnAdvance.setTextColor(resources.getColor(R.color.text_on_accent))
            selectedLevel = "Advance"
            UserRegisterData.activityLevel = "Advance"
        }

    }

    private fun setupContinueButton() = with(binding) {
        btnContinue.setOnClickListener {

            if (selectedLevel.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please select your fitness goal",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            findNavController().navigate(
                R.id.action_l_R_S_PhyALFragment_to_l_R_S_UserRegister_Fragment2
            )
        }
    }




}