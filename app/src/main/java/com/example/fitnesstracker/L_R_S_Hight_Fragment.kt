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
import com.example.fitnesstracker.databinding.FragmentLRSHightBinding
import kotlin.math.abs
import kotlin.math.min


class L_R_S_Hight_Fragment : Fragment() {
    private lateinit var binding: FragmentLRSHightBinding
    private lateinit var adapter: HeightAdapter
    private lateinit var layoutManagerHeight: LinearLayoutManager
    private lateinit var snapHelper: LinearSnapHelper

    private val heightList = (100..250).toList()  // Heights in cm
    private var selectedHeight: Int = 165  // default


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLRSHightBinding.inflate(inflater, container, false)

        binding.backBtn.setOnClickListener {
            val action = L_R_S_Hight_FragmentDirections.actionLRSHightFragment2ToLRSWeightFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeightWheel()
        setupContinueButton()
    }

    private fun setupHeightWheel() = with(binding) {

        adapter = HeightAdapter(heightList)
        layoutManagerHeight = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        heightRecycler.layoutManager = layoutManagerHeight
        heightRecycler.adapter = adapter

        // Snap to center
        snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(heightRecycler)

        // Scroll to default height = 165 cm
        val defaultPos = heightList.indexOf(selectedHeight)
        heightRecycler.scrollToPosition(defaultPos)

        // Initial UI update
        heightRecycler.post {
            updateHeightFromCenter()
            applyWheelTransform()
        }

        // Handle scrolling
        heightRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                applyWheelTransform()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateHeightFromCenter()
                    applyWheelTransform()
                }
            }
        })
    }

    private fun applyWheelTransform() = with(binding) {

        val centerX = heightRecycler.width / 2f

        for (i in 0 until heightRecycler.childCount) {
            val child = heightRecycler.getChildAt(i) ?: continue

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

    private fun updateHeightFromCenter() = with(binding) {
        val centerView = snapHelper.findSnapView(layoutManagerHeight) ?: return
        val pos = layoutManagerHeight.getPosition(centerView)

        if (pos != RecyclerView.NO_POSITION) {
            selectedHeight = heightList[pos]
            tvHeightValue.text = selectedHeight.toString()
            UserRegisterData.heightCm = selectedHeight      // <--- save
        }
    }


    private fun setupContinueButton() = with(binding) {

        btnContinue.setOnClickListener {
            // Save height and navigate to the next page
            // TODO: Pass height to the next fragment or activity
            val action = L_R_S_Hight_FragmentDirections.actionLRSHightFragment2ToLRSGoalFragment()
            findNavController().navigate(action)
        }
    }


}