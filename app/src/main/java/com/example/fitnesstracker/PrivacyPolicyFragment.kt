package com.example.fitnesstracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.databinding.FragmentPrivacyPolicyBinding

class PrivacyPolicyFragment : Fragment() {

    private var _binding: FragmentPrivacyPolicyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Toolbar back arrow
        // Make sure your XML has: app:navigationIcon="@drawable/ic_arrow_back_24"
        binding.toolbarPrivacy.setNavigationOnClickListener {
            // Use the action you already declared in nav graph
            findNavController().navigate(R.id.action_privacyPolicyFragment_to_home_Profile_Fragment)
        }

        // 2) System back button handling (gesture/back key)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // If you want to use the same explicit action:
                    findNavController().navigate(R.id.action_privacyPolicyFragment_to_home_Profile_Fragment)

                    // Alternative (simpler): findNavController().popBackStack()
                }
            }
        )

        // 3) Optional: Accept/Decline buttons (if you added them in your layout)
        // If you don't have these buttons, remove this section.
        binding.btnAccept?.setOnClickListener {
            // Example: save acceptance and go back
            // requireContext().getSharedPreferences("prefs", 0).edit()
            //     .putBoolean("privacyAccepted", true).apply()
            findNavController().popBackStack()
        }

        binding.btnDecline?.setOnClickListener {
            // Example: just go back (or logout, close app, etc.)
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
