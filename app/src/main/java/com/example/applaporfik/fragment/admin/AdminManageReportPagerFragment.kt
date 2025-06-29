package com.example.applaporfik.fragment.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider // ✅ INI YANG KAMU LUPA
import com.example.applaporfik.adapter.AdminManageReportPagerAdapter
import com.example.applaporfik.databinding.FragmentAdminManageReportPagerBinding
import com.google.android.material.tabs.TabLayoutMediator

class AdminManageReportPagerFragment : Fragment() {

    private var _binding: FragmentAdminManageReportPagerBinding? = null
    private val binding get() = _binding!!

    private val tabTitles = listOf("Pending", "In Progress", "Resolved", "Rejected")
    private val statuses = listOf("pending", "in_progress", "resolved", "rejected")

    private lateinit var adminReportViewModel: AdminReportViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminManageReportPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Tambahkan import ViewModelProvider agar tidak merah
        adminReportViewModel = ViewModelProvider(requireActivity())[AdminReportViewModel::class.java]

        val pagerAdapter = AdminManageReportPagerAdapter(this, statuses)
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
