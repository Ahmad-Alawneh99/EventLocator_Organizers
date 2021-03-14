package com.eventlocator.eventlocatororganizers.ui

import com.eventlocator.eventlocatororganizers.adapters.EventPagerAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.viewpager2.widget.ViewPager2
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.databinding.ActivityEventsBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class EventsActivity : AppCompatActivity(){
    lateinit var binding: ActivityEventsBinding
    var filterFragment: FilterPreviousEventsFragment? = null
    lateinit var pagerAdapter: EventPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pagerAdapter = EventPagerAdapter(this, 3)
        binding.pagerEvents.adapter = pagerAdapter
        TabLayoutMediator(binding.tlEvents, binding.pagerEvents){ tab, position ->
            when (position){
                0 -> tab.text = getString(R.string.upcoming_events)
                1 -> tab.text = getString(R.string.previous_events)
                2 -> tab.text = getString(R.string.canceled_events)
            }

        }.attach()

        binding.pagerEvents.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Toast.makeText(applicationContext, "text", Toast.LENGTH_SHORT).show()
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


    private fun getAndLoadEvents(){

    }
}