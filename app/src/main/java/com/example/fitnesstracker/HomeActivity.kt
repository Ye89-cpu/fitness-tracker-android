package com.example.fitnesstracker

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.fitnesstracker.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
    var userId: Int = -1
    var username: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        userId = intent.getIntExtra("user_id", -1)
        username = intent.getStringExtra("username")

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentView) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavView.setupWithNavController(navController)

        // If you want to show home by default (optional):
        // navController.navigate(R.id.home_welcome)

        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.itemhome -> {
                    navController.navigate(R.id.home_welcome_Fragment)
                    true
                }
                R.id.itemactivity -> {
                    navController.navigate(R.id.home_Activity_Fragment)
                    true
                }
                R.id.itemWorkout -> {
                    navController.navigate(R.id.home_Workout_Fragment)
                    true
                }
                R.id.itemhistory -> {
                    navController.navigate(R.id.home_History_Fragment)
                    true
                }
                R.id.itemProfile -> {          // NOTE: capital P because your menu id is Profile
                    navController.navigate(R.id.home_Profile_Fragment)
                    true
                }
                else -> false
            }
        }
    }
}