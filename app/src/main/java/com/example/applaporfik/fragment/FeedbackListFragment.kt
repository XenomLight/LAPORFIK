package com.example.applaporfik.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.example.applaporfik.databinding.FragmentFeedbackListBinding
import com.example.applaporfik.adapter.FeedbackListAdapter
import com.example.applaporfik.model.FeedbackItem
import com.example.applaporfik.model.FeedbackCategory
import com.example.applaporfik.model.FeedbackStatus
import java.text.SimpleDateFormat
import java.util.Locale

class FeedbackListFragment : Fragment() {

    private var _binding: FragmentFeedbackListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedbackListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize your views and set up the adapter
        val adapter = FeedbackListAdapter()
        binding.recyclerViewFeedbackList.adapter = adapter
        binding.recyclerViewFeedbackList.layoutManager = LinearLayoutManager(context)

        // Fetch data and populate the adapter
        fetchData()
    }

    private fun fetchData() {
        // Implement your data fetching logic here
        // For example, you can use a coroutine to fetch data from a remote server
        // or use a local data source
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 