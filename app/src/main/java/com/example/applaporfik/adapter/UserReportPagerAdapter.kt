package com.example.applaporfik.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.applaporfik.fragment.user.UserReportListFragment

class UserReportPagerAdapter(
    fragment: Fragment,
    private val statuses: List<String?>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = statuses.size

    override fun createFragment(position: Int): Fragment {
        return UserReportListFragment.newInstance(statuses[position])
    }
}
