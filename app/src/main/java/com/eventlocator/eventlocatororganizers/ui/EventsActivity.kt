package com.eventlocator.eventlocatororganizers.ui

import com.eventlocator.eventlocatororganizers.adapters.EventPagerAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.databinding.ActivityEventsBinding
import com.google.android.material.tabs.TabLayout

class EventsActivity : AppCompatActivity(){
    lateinit var binding: ActivityEventsBinding
    var filterFragment: FilterPreviousEventsFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tlEvents.addTab(binding.tlEvents.newTab().setText(R.string.upcoming_events))
        binding.tlEvents.addTab(binding.tlEvents.newTab().setText(R.string.previous_events))
        binding.tlEvents.addTab(binding.tlEvents.newTab().setText(R.string.canceled_events))
        binding.tlEvents.tabGravity = TabLayout.GRAVITY_FILL

        val pagerAdapter = EventPagerAdapter(this, supportFragmentManager, binding.tlEvents.tabCount)
        binding.pagerEvents.adapter = pagerAdapter
        binding.pagerEvents.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.tlEvents))
        binding.tlEvents.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    binding.pagerEvents.currentItem = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        menu?.add(1,1, Menu.NONE, "Filter").also { item ->
            item?.icon = ContextCompat.getDrawable(this,R.drawable.ic_temp)
            item?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            1-> {
                if (filterFragment==null) {
                    filterFragment = FilterPreviousEventsFragment(ArrayList())
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        add(R.id.fvFilter, filterFragment!!)
                    }
                }
                else{
                    supportFragmentManager.commit {
                        remove(filterFragment!!)
                        filterFragment = null
                    }
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}