package com.example.applaporfik.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.applaporfik.databinding.FragmentManageFeedbackBinding
import com.example.applaporfik.adapter.ManageFeedbackAdapter
import com.example.applaporfik.model.FeedbackItem
import com.example.applaporfik.model.FeedbackCategory
import com.example.applaporfik.model.FeedbackStatus

class ManageFeedbackFragment : Fragment() {

    private var _binding: FragmentManageFeedbackBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize your RecyclerView and other UI components here
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 