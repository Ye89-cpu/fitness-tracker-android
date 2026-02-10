package com.example.fitnesstracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.databinding.FragmentLRSWeightBinding
import kotlin.math.abs
import kotlin.math.min


class L_R_S_Weight_Fragment : Fragment() {

    private lateinit var binding: FragmentLRSWeightBinding
    private lateinit var adapter: WeightAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var snapHelper: LinearSnapHelper

    private val weightList = (67..169).toList() // Weight range
    private var selectedWeight: Int = 75 // Default weight


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLRSWeightBinding.inflate(inflater, container, false)

        binding.backBtn.setOnClickListener {
            val action = L_R_S_Weight_FragmentDirections.actionLRSWeightFragmentToLRSAgeFragment3()
            findNavController().navigate(action)
        }

        setupWeightWheel()
        setupContinueButton()

        return binding.root
    }

    private fun setupWeightWheel() {
        adapter = WeightAdapter(weightList)
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.weightRecycler.layoutManager = layoutManager
        binding.weightRecycler.adapter = adapter

        // Snap to center
        snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.weightRecycler)

        // Scroll to default weight = 75
        val defaultPos = weightList.indexOf(selectedWeight)
        binding.weightRecycler.scrollToPosition(defaultPos)

        // Initial UI update
        binding.weightRecycler.post {
            updateWeightFromCenter()
            applyWheelTransform()
        }

        // Handle scrolling
        binding.weightRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                applyWheelTransform()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateWeightFromCenter()
                    applyWheelTransform()
                }
            }
        })
    }

    private fun applyWheelTransform() {
        val centerX = binding.weightRecycler.width / 2f

        for (i in 0 until binding.weightRecycler.childCount) {
            val child = binding.weightRecycler.getChildAt(i) ?: continue

            val childCenterX = (child.left + child.right) / 2f
            val distance = abs(childCenterX - centerX)

            val maxDistance = centerX * 1.2f
            val ratio = min(1f, distance / maxDistance)

            // Scale effect (center big, sides small)
            val scale = 0.6f + (1f - ratio) * 0.6f
            child.scaleX = scale
            child.scaleY = scale

            // Fade effect (center bright, sides dim)
            val alpha = 0.3f + (1f - ratio) * 0.7f
            child.alpha = alpha

            // Wheel curve (slightly down the further away)
            child.translationY = ratio * 35f
        }
    }

    private fun updateWeightFromCenter() {
        val centerView = snapHelper.findSnapView(layoutManager) ?: return
        val pos = layoutManager.getPosition(centerView)

        if (pos != RecyclerView.NO_POSITION) {
            selectedWeight = weightList[pos]
            binding.tvWeightValue.text = selectedWeight.toString()
            UserRegisterData.weightKg = selectedWeight      // <--- save
        }
    }


    private fun setupContinueButton() {
        binding.btnContinue.setOnClickListener {
            // WeightStore.selectedWeight = selectedWeight // Save globally
            // TODO: Navigate to next screen
            val action = L_R_S_Weight_FragmentDirections.actionLRSWeightFragmentToLRSHightFragment2() // Adjust the navigation direction
            findNavController().navigate(action)
        }
    }



}