package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.adapters.OnSessionCheckChangeListener
import com.eventlocator.eventlocatororganizers.adapters.SessionInputAdapter
import com.eventlocator.eventlocatororganizers.adapters.SessionStatisticsAdapter
import com.eventlocator.eventlocatororganizers.data.Session
import com.eventlocator.eventlocatororganizers.databinding.ActivityEventStatisticsBinding
import com.eventlocator.eventlocatororganizers.retrofit.EventService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormat
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormatterFactory
import com.eventlocator.eventlocatororganizers.utilities.SharedPreferenceManager
import com.eventlocator.eventlocatororganizers.utilities.Utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.*

class EventStatisticsActivity : AppCompatActivity(), OnSessionCheckChangeListener {
    lateinit var binding: ActivityEventStatisticsBinding
    var eventID: Long = -1
    lateinit var eventName: String
    lateinit var sessions: ArrayList<Session>
    val daysAndDates = ArrayList<String>()
    val numbersOfParticipants = ArrayList<Int>()
    val averageArrivalTimes = ArrayList<String>()
    val zonedDateTimeArrivalTimes = ArrayList<ZonedDateTime>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getData()

        getAndPrepareStatistics()
    }

    private fun getAndPrepareStatistics(){
        val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
        RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token!!)
                .getAttendanceStatisticsForAnEvent(eventID).enqueue(object: Callback<JsonObject>{
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        if (response.code() == 200){
                            val responseAsJSON = JSONObject(Gson().toJson(response.body()))
                            val resultArray = responseAsJSON.getJSONArray("sessions")

                            for(i in 0 until resultArray.length()){
                                val date = LocalDate.parse(sessions[i].date, DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
                                val day = DayOfWeek.of(sessions[i].dayOfWeek).name
                                daysAndDates.add(day+","+ DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY)
                                        .format(date))
                                numbersOfParticipants.add(resultArray.getJSONObject(i).getInt("total"))
                                val dt = LocalDateTime.parse(resultArray.getJSONObject(i).getString("avgArrivalTime"),
                                        DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DEFAULT))
                                zonedDateTimeArrivalTimes.add(dt.atZone(ZoneId.systemDefault()))
                                averageArrivalTimes.add(DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DISPLAY).format(dt))
                            }
                            val layoutManager = LinearLayoutManager(this@EventStatisticsActivity)
                            val adapter = SessionStatisticsAdapter(daysAndDates,numbersOfParticipants, averageArrivalTimes)
                            binding.rvSessions.layoutManager = layoutManager
                            binding.rvSessions.adapter = adapter

                            binding.tvEventName.text = eventName
                            var sum = 0
                            for(i in 0 until numbersOfParticipants.size){
                                sum+=numbersOfParticipants[i]
                            }
                            val res = if(numbersOfParticipants.size == 0) 0 else sum/numbersOfParticipants.size
                            binding.tvAvgParticipants.text = res.toString()

                            var dateTimeSum: Long = 0

                            for(i in 0 until zonedDateTimeArrivalTimes.size){
                                dateTimeSum += zonedDateTimeArrivalTimes[i].toInstant().toEpochMilli()
                            }

                            val dateTimeRes: ZonedDateTime? = if (zonedDateTimeArrivalTimes.size == 0) null
                                else ZonedDateTime.ofInstant(Instant.ofEpochMilli(dateTimeSum/zonedDateTimeArrivalTimes.size),
                                    ZoneId.systemDefault())
                            if (dateTimeRes!= null){
                                binding.tvAvgArrivalTime.text = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DISPLAY)
                                        .format(dateTimeRes)
                            }




                        }
                        else if (response.code() == 500){
                            Utils.instance.displayInformationalDialog(this@EventStatisticsActivity,
                                    "Error", "Server issue, please try again later", true)
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        Utils.instance.displayInformationalDialog(this@EventStatisticsActivity,
                                "Error", "Can't connect to server", true)
                    }

                })
    }


    private fun getData(){
        eventID = intent.getLongExtra("eventID", -1)
        eventName = intent.getStringExtra("eventName")!!
        sessions = intent.getSerializableExtra("sessions") as ArrayList<Session>

    }

    override fun onCheckChange() {
        if (binding.rvSessions.adapter == null) return
        var participantSum = 0
        var arrivalTimeSum: Long = 0
        var count = 0
        for(i in 0 until binding.rvSessions.adapter!!.itemCount){
            val holder = binding.rvSessions.findViewHolderForAdapterPosition(i) as SessionStatisticsAdapter.SessionStatisticsHolder
            if (holder.binding.cbIncludeSession.isChecked){
                participantSum += numbersOfParticipants[i]
                arrivalTimeSum += zonedDateTimeArrivalTimes[i].toInstant().toEpochMilli()
                count++
            }
        }
        if (count > 0) {
            binding.tvAvgParticipants.text = (participantSum/count).toString()
            val dt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(arrivalTimeSum/zonedDateTimeArrivalTimes.size),
                    ZoneId.systemDefault())
            binding.tvAvgArrivalTime.text = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DISPLAY)
                    .format(dt)
        }
        else{
            binding.tvAvgArrivalTime.text = getString(R.string.no_sessions_selected_error)
            binding.tvAvgArrivalTime.text = getString(R.string.no_sessions_selected_error)
        }
    }
}