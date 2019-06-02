package com.calpolycsai.nimbus

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class TabPageAdapter(fm : FragmentManager, private var tabCount : Int):
        FragmentPagerAdapter(fm){
    override fun getItem(position: Int): Fragment {
        when(position) {
            0 -> return WakeWordRecord()
            1 -> return WakeWordRecordings()
        }
        return WakeWordRecord()
    }

    override fun getCount(): Int {
        return tabCount
    }
}