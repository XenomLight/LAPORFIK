package com.example.applaporfik.fragment.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.applaporfik.adapter.UserReportPagerAdapter
import com.example.applaporfik.databinding.FragmentUserReportPagerBinding
import com.google.android.material.tabs.TabLayoutMediator

class UserReportPagerFragment : Fragment() {

    private var _binding: FragmentUserReportPagerBinding? = null
    private val binding get() = _binding!!

    private val tabTitles = listOf("All", "Pending", "In Progress", "Resolved", "Rejected")
    private val statuses = listOf<String?>(
        null,
        "pending",
        "in_progress",
        "resolved",
        "rejected"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserReportPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagerAdapter = UserReportPagerAdapter(this, statuses)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
