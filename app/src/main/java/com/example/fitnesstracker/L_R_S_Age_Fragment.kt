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
import com.example.fitnesstracker.databinding.FragmentLRSAgeBinding
import kotlin.math.abs
import kotlin.math.min


class L_R_S_Age_Fragment : Fragment() {
    private lateinit var binding: FragmentLRSAgeBinding

    private lateinit var adapter: AgeAdapter
    private lateinit var layoutManagerAge: LinearLayoutManager
    private lateinit var snapHelper: LinearSnapHelper

    private val ageList = (9..80).toList()
    private var selectedAge: Int = 28  // default

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLRSAgeBinding.inflate(inflater, container, false)
        binding.backBtn.setOnClickListener {
            val action = L_R_S_Age_FragmentDirections.actionLRSAgeFragment3ToLRSGenderFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAgeWheel()
        setupContinueButton()
    }


    private fun setupAgeWheel() = with(binding) {

        adapter = AgeAdapter(ageList)
        layoutManagerAge = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        ageRecycler.layoutManager = layoutManagerAge
        ageRecycler.adapter = adapter

        // Snap to center
        snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(ageRecycler)

        // Scroll to default age = 28
        val defaultPos = ageList.indexOf(selectedAge)
        ageRecycler.scrollToPosition(defaultPos)

        // Initial UI update
        ageRecycler.post {
            updateAgeFromCenter()
            applyWheelTransform()
        }

        // Handle scrolling
        ageRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                applyWheelTransform()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateAgeFromCenter()
                    applyWheelTransform()
                }
            }
        })
    }
    private fun applyWheelTransform() = with(binding) {

        val centerX = ageRecycler.width / 2f

        for (i in 0 until ageRecycler.childCount) {
            val child = ageRecycler.getChildAt(i) ?: continue

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
        }//for
    }//applyWheelTransform

    private fun updateAgeFromCenter() = with(binding) {
        val centerView = snapHelper.findSnapView(layoutManagerAge) ?: return
        val pos = layoutManagerAge.getPosition(centerView)

        if (pos != RecyclerView.NO_POSITION) {
            selectedAge = ageList[pos]
            tvAgeValue.text = selectedAge.toString()
            UserRegisterData.age = selectedAge        // <--- save
        }
    }


    private fun setupContinueButton() = with(binding) {
        btnContinue.setOnClickListener {
            findNavController().navigate(
                R.id.action_l_R_S_Age_Fragment3_to_l_R_S_Weight_Fragment
            )
        }
    }



}