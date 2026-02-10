package com.example.fitnesstracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.databinding.FragmentLRSGoalBinding


class L_R_S_Goal_Fragment : Fragment() {

    private lateinit var binding: FragmentLRSGoalBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLRSGoalBinding.inflate(inflater, container, false)

        binding.backBtn.setOnClickListener {
            val action = L_R_S_Goal_FragmentDirections.actionLRSGoalFragmentToLRSHightFragment2()
            findNavController().navigate(action)
        }
        // Set the click listener for the "Continue" button
        binding.btnContinue.setOnClickListener {
            // Get the selected goal from the RadioGroup
            val selectedGoalId = binding.goalRadioGroup.checkedRadioButtonId
            val selectedGoal = when (selectedGoalId) {
                R.id.rbLoseWeight      -> "Lose Weight"
                R.id.rbGainWeight      -> "Gain Weight"
                R.id.rbMuscleMassGain  -> "Muscle Mass Gain"
                R.id.rbShapeBody       -> "Shape Body"
                else                   -> ""
            }

            UserRegisterData.goal = selectedGoal      // <--- save



            // You can save this goal into a ViewModel or pass it to another screen via navigation
            // For example, you can pass it as an argument to the next screen
            val action = L_R_S_Goal_FragmentDirections.actionLRSGoalFragmentToLRSPhyALFragment()
            findNavController().navigate(action)
        }

        // Inflate the layout for this fragment
        return binding.root
    }


}