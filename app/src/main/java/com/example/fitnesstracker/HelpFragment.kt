package com.example.fitnesstracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.databinding.FragmentHelpBinding

class HelpFragment : Fragment() {

    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Toolbar back arrow
        binding.toolbarHelp.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // 2) Email support button
        binding.btnEmailSupport.setOnClickListener {
            openEmail(
                to = "support@yourapp.com",
                subject = "Fitness Tracker Support",
                body = "Hi Support,\n\nI need help with:\n\n(Describe your issue here)\n\nThanks."
            )
        }

        // 3) Open Privacy Policy
        binding.btnOpenPrivacyPolicy.setOnClickListener {
            // If privacy policy is in the SAME nav graph:
            // Make sure you have an action from HelpFragment -> PrivacyPolicyFragment
            findNavController().navigate(R.id.action_helpFragment_to_privacyPolicyFragment)
        }

        // 4) Optional quick actions
        binding.btnHowToLogWorkout.setOnClickListener {
            // Example: scroll to FAQ Q1
            binding.scrollHelp.smoothScrollTo(0, binding.tvFaqTitle.top)
        }

        binding.btnFixSync.setOnClickListener {
            // Example: open a website or show a simple in-app explanation
            openWeb("https://example.com/help/sync")
        }

        binding.btnPermissions.setOnClickListener {
            // Example: open Android App settings page for permissions
            openAppSettings()
        }
    }

    private fun openEmail(to: String, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        // Avoid crash if no email app
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun openWeb(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun openAppSettings() {
        val intent = Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireContext().packageName, null)
        )
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
