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
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.databinding.ActivityEventsBinding
import com.eventlocator.eventlocatororganizers.retrofit.EventService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.SharedPreferenceManager
import com.eventlocator.eventlocatororganizers.utilities.Utils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventsActivity : AppCompatActivity(), OnEventsFiltered{
    lateinit var binding: ActivityEventsBinding
    var filterFragment: FilterPreviousEventsFragment? = null
    lateinit var pagerAdapter: EventPagerAdapter
    var currentPosition = 0
    lateinit var onPreviousEventsReadyListener: OnPreviousEventsReadyListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //TODO: enable when done with testing
        //getAndLoadEvents()

        //For testing:
        /*pagerAdapter = EventPagerAdapter(that, 3, ArrayList())
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
                //TODO: make the color of the filter button blurred
                currentPosition = position
                if (currentPosition!= 1 && filterFragment!=null){
                    supportFragmentManager.commit {
                        remove(filterFragment!!)
                        filterFragment = null
                    }
                }
            }
        })
    */
    }

    override fun onResume() {
        super.onResume()
        getAndLoadEvents()
    }



    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (!this::pagerAdapter.isInitialized || currentPosition!=1)return false
        menu?.add(1,1, Menu.NONE, "Filter").also { item ->
            item?.icon = ContextCompat.getDrawable(this,R.drawable.ic_temp)
            item?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            1-> {
                if (currentPosition == 1) {
                    if (filterFragment == null) {
                        filterFragment = FilterPreviousEventsFragment(pagerAdapter.previousEvents)
                        supportFragmentManager.commit {
                            setReorderingAllowed(true)
                            add(R.id.fvFilter, filterFragment!!)
                        }
                    } else {
                        supportFragmentManager.commit {
                            remove(filterFragment!!)
                            filterFragment = null
                        }
                    }
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun getAndLoadEvents(){
        val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")!!
        RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token)
                .getEvents().enqueue(object: Callback<ArrayList<Event>>{
                    override fun onResponse(call: Call<ArrayList<Event>>, response: Response<ArrayList<Event>>) {
                        if (response.code()==200) {
                            pagerAdapter = EventPagerAdapter(this@EventsActivity, 3, response.body()!!)
                            binding.pagerEvents.adapter = pagerAdapter
                            TabLayoutMediator(binding.tlEvents, binding.pagerEvents) { tab, position ->
                                when (position) {
                                    0 -> tab.text = getString(R.string.upcoming_events)
                                    1 -> tab.text = getString(R.string.previous_events)
                                    2 -> tab.text = getString(R.string.canceled_events)
                                }

                            }.attach()

                            binding.pagerEvents.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                                override fun onPageSelected(position: Int) {
                                    super.onPageSelected(position)
                                    currentPosition = position
                                    invalidateOptionsMenu()
                                    if (currentPosition != 1 && filterFragment != null) {
                                        supportFragmentManager.commit {
                                            remove(filterFragment!!)
                                            filterFragment = null
                                        }
                                    }
                                }
                            })
                        }
                        else if (response.code()==401){
                            Utils.instance.displayInformationalDialog(this@EventsActivity, "Error",
                                    "401: Unauthorized access",true)
                        }
                        else if (response.code() == 404){
                            Utils.instance.displayInformationalDialog(this@EventsActivity,
                                    "Error", "No events found", false)
                        }
                        else if (response.code() == 500){
                            Utils.instance.displayInformationalDialog(this@EventsActivity,
                                    "Error", "Server issue, please try again later", false)
                        }
                    }

                    override fun onFailure(call: Call<ArrayList<Event>>, t: Throwable) {
                        Utils.instance.displayInformationalDialog(this@EventsActivity,
                                "Error", "Can't connect to server", false)
                    }

                })
    }

    override fun getFilteredResult(events: ArrayList<Event>) {
        onPreviousEventsReadyListener.getResult(events)
        supportFragmentManager.commit {
            remove(filterFragment!!)
            filterFragment = null
        }
    }

}

interface OnPreviousEventsReadyListener{
    fun getResult(events: ArrayList<Event>)
}