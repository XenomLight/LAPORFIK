package com.example.applaporfik.fragment.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.applaporfik.activity.LoginActivity
import com.example.applaporfik.adapter.RecentFeedbackAdapter
import com.example.applaporfik.databinding.FragmentHomeBinding
import com.example.applaporfik.model.FeedbackItem
import com.example.applaporfik.model.FeedbackCategory
import com.example.applaporfik.model.FeedbackStatus
import com.example.applaporfik.util.SessionManager

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var recentFeedbackAdapter: RecentFeedbackAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        
        setupRecyclerView()
        setupUI()
        loadRecentFeedback()
    }

    private fun setupRecyclerView() {
        recentFeedbackAdapter = RecentFeedbackAdapter()
        binding.recyclerViewRecentFeedback.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentFeedbackAdapter
        }
    }

    private fun setupUI() {
        if (sessionManager.isLoggedIn()) {
            // User is logged in - show "Lapor" button
            binding.cardLogin.visibility = View.GONE
            binding.cardLapor.visibility = View.VISIBLE
            
            binding.btnLapor.setOnClickListener {
                // Navigate to form fragment
                val navController = requireActivity().supportFragmentManager
                    .findFragmentById(com.example.applaporfik.R.id.nav_host_fragment_activity_main)
                    ?.let { fragment ->
                        androidx.navigation.fragment.NavHostFragment.findNavController(fragment)
                    }
                navController?.navigate(com.example.applaporfik.R.id.formFragment)
            }
        } else {
            // User is not logged in - show "Login" button
            binding.cardLogin.visibility = View.VISIBLE
            binding.cardLapor.visibility = View.GONE
            
            binding.btnLogin.setOnClickListener {
                // Navigate to login page
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun loadRecentFeedback() {
        // Show loading
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewRecentFeedback.visibility = View.GONE
        binding.textEmpty.visibility = View.GONE

        // TODO: Replace with actual API call to get recent feedback
        // For now, using dummy data
        val dummyFeedback = listOf(
            FeedbackItem("FB001", FeedbackCategory.FACILITY, "Broken Chair in Room 301", "2025-01-15", "One of the chairs near the window in Room 301 has a broken leg.", FeedbackStatus.REPORTED),
            FeedbackItem("FB002", FeedbackCategory.ACADEMIC, "More Calculus Practice Sessions", "2025-01-14", "Could we please have more optional practice sessions for the upcoming Calculus II exam?", FeedbackStatus.REPORTED),
            FeedbackItem("FB003", FeedbackCategory.FACILITY, "Water Fountain Leak", "2025-01-13", "The water fountain on the 2nd floor near the library is leaking constantly.", FeedbackStatus.FINISHED)
        )

        // Simulate network delay
        view?.postDelayed({
            binding.progressBar.visibility = View.GONE
            
            if (dummyFeedback.isEmpty()) {
                binding.textEmpty.visibility = View.VISIBLE
                binding.recyclerViewRecentFeedback.visibility = View.GONE
            } else {
                binding.textEmpty.visibility = View.GONE
                binding.recyclerViewRecentFeedback.visibility = View.VISIBLE
                recentFeedbackAdapter.submitList(dummyFeedback)
            }
        }, 1000)
    }

    override fun onResume() {
        super.onResume()
        // Update UI when returning from login
        setupUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 