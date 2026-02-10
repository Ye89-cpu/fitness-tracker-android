package com.example.fitnesstracker

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.databinding.FragmentLRSGenderBinding


class L_R_S_Gender_Fragment : Fragment() {

    private lateinit var binding: FragmentLRSGenderBinding
    private var selectedGender: String? = null   // Male / Female

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLRSGenderBinding.inflate(inflater,container,false)

        setupUI()
        setupClicks()

        // Inflate the layout for this fragment
        return binding.root
    }
    private fun setupUI() {
        binding.btnContinue.isEnabled = false
        binding.btnContinue.alpha = 0.4f
        binding.backBtn.setOnClickListener {
            val action = L_R_S_Gender_FragmentDirections.actionLRSGenderFragmentToLRSFragment()
            findNavController().navigate(action)
        }
    }
    private fun setupClicks() = with(binding) {

        maleOption.setOnClickListener {
            selectGender("Male")
        }

        femaleOption.setOnClickListener {
            selectGender("Female")
        }

        btnContinue.setOnClickListener {
            selectedGender?.let { gender ->
                val action = L_R_S_Gender_FragmentDirections.actionLRSGenderFragmentToLRSAgeFragment3()
                findNavController().navigate(action)
                // GLOBAL SAVE
                // GenderStore.selectedGender = gender

                // TODO: Navigate or move to next screen
                // e.g. findNavController().navigate(R.id.action_gender_to_next)
            }
        }
    }
    private fun selectGender(gender: String) = with(binding) {
        selectedGender = gender

        val highlightColor = Color.parseColor("#E2F163")
        val normalTextColor = Color.WHITE
        val selectedIconColor = Color.BLACK
        val normalIconColor = Color.WHITE

        val isMale = gender == "Male"

        // Background selector activation
        maleOption.isSelected = isMale
        femaleOption.isSelected = !isMale

        // Icon tint
        maleIcon.imageTintList =
            ColorStateList.valueOf(if (isMale) selectedIconColor else normalIconColor)

        femaleIcon.imageTintList =
            ColorStateList.valueOf(if (!isMale) selectedIconColor else normalIconColor)

        // Text color swap
        maleText.setTextColor(if (isMale) highlightColor else normalTextColor)
        femaleText.setTextColor(if (!isMale) highlightColor else normalTextColor)

        // Enable continue button
        btnContinue.isEnabled = true
        btnContinue.alpha = 1f

        selectedGender = gender
        UserRegisterData.gender = gender
    }


}