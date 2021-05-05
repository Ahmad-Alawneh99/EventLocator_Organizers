package com.eventlocator.eventlocatororganizers.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.util.Pair
import androidx.recyclerview.widget.LinearLayoutManager
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.adapters.SessionInputAdapter
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.data.LocatedEventData
import com.eventlocator.eventlocatororganizers.data.Session
import com.eventlocator.eventlocatororganizers.databinding.ActivityEditConfirmedEventBinding
import com.eventlocator.eventlocatororganizers.retrofit.EventService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.time.*
import java.time.format.TextStyle
import java.util.*

class EditConfirmedEventActivity : AppCompatActivity(), DateErrorUtil {
    lateinit var binding: ActivityEditConfirmedEventBinding

    var eventID: Long = 0
    lateinit var event: Event
    val DATE_PERIOD_LIMIT = 6
    var sessionCount = -1

    lateinit var startDate: LocalDate
    lateinit var endDate: LocalDate

    lateinit var registrationCloseDate: LocalDate
    var registrationCloseTime = TimeStamp(-1,-1)

    //for the first session
    var firstSessionStartTime = TimeStamp(-1,-1)
    var firstSessionEndTime = TimeStamp(-1,-1)
    var firstSessionCheckInTime = TimeStamp(-1,-1)

    //location for located events
    lateinit var locationLatLng: LatLng
    lateinit var locationName: String

    lateinit var locationActivityResult: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditConfirmedEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventID = intent.getLongExtra("eventID", -1)
        sessionCount = intent.getIntExtra("sessionCount", -1)
        prepareRecyclerView()
        getAndLoadEvent()
        setClickListenersForFirstSession()
        binding.btnSave.isEnabled = false

        locationActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if (result.resultCode == Activity.RESULT_OK) {
                locationLatLng = result.data?.getParcelableExtra("latlng")!!
                locationName = result.data?.getStringExtra("name")!!
                binding.tvSelectedLocation.text = locationName
            }
        }

        binding.btnSave.setOnClickListener {
            val dialogAlert = Utils.instance.createSimpleDialog(this, "Edit event",
                    "Are you sure that you want to save these changes?")

            dialogAlert.setPositiveButton("Yes") { di: DialogInterface, i: Int ->

                binding.btnSave.isEnabled = false
                binding.pbLoading.visibility = View.VISIBLE

                val sessions = ArrayList<Session>()
                sessions.add(Session(1,
                        DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT).format(startDate),
                        firstSessionStartTime.format24H(),
                        firstSessionEndTime.format24H(),
                        startDate.dayOfWeek.value,
                        if (event.isLimitedLocated()) if (firstSessionCheckInTime.hour >= 0) firstSessionCheckInTime.format24H() else
                            firstSessionStartTime.format24H() else ""))

                for (i in 0 until (binding.rvSessions.adapter?.itemCount!!)) {
                    val holder = binding.rvSessions.findViewHolderForLayoutPosition(i) as SessionInputAdapter.SessionInputHolder
                    if (!holder.binding.cbEnableSession.isChecked) continue
                    val sessionStartDate = LocalDate.parse(holder.binding.cbEnableSession.text.toString().split(',')[1].trim(),
                            DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY))
                    sessions.add(Session(i + 2,
                            DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT).format(sessionStartDate),
                            holder.startTime.format24H(),
                            holder.endTime.format24H(),
                            sessionStartDate.dayOfWeek.value,
                            if (event.isLimitedLocated()) if (holder.checkInTime.hour >= 0) holder.checkInTime.format24H() else
                                holder.startTime.format24H() else ""))
                }

                val temp = if (this::registrationCloseDate.isInitialized)
                    if (registrationCloseTime.hour == -1)
                        registrationCloseDate.atTime(firstSessionStartTime.hour, firstSessionStartTime.minute)
                    else registrationCloseDate.atTime(registrationCloseTime.hour, registrationCloseTime.minute)
                else
                    startDate.atTime(firstSessionStartTime.hour, firstSessionStartTime.minute)

                val eventBuilder = Event.EventBuilder(
                        "WILL NOT BE USED",
                        "WILL NOT BE USED",
                        ArrayList<Int>(),
                        DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT).format(startDate),
                        DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT).format(endDate),
                        sessions,
                        DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DEFAULT).format(temp)
                )

                val locatedEventData = if (event.locatedEventData != null) {
                    val location = ArrayList<Double>()
                    location.add(locationLatLng.latitude)
                    location.add(locationLatLng.longitude)
                    LocatedEventData(-1, location) //City will not be used
                } else null
                eventBuilder.setLocatedEventData(locatedEventData)
                val newEvent = eventBuilder.build()

                val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                        .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")

                RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token!!)
                        .editConfirmedEvent(event.id, newEvent)
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                if (response.code() == 201) {
                                    if (event.locatedEventData == null) {
                                        var startDateTime = startDate.atTime(firstSessionStartTime.hour, firstSessionStartTime.minute)
                                        startDateTime = startDateTime.minusHours(12)
                                        val message = "Your online event ${event.name} is starting in 12 hours, " +
                                                "please make sure to send the meeting link to the participants of this event"
                                        NotificationUtils.cancelNotification(this@EditConfirmedEventActivity, event.id)

                                        NotificationUtils.scheduleNotification(this@EditConfirmedEventActivity,
                                                startDateTime, event.id.toInt(), message, event.id)


                                        NotificationUtils.cancelNotification(this@EditConfirmedEventActivity, event.id)
                                    }
                                    NotificationUtils.cancelNotification(this@EditConfirmedEventActivity, event.id)
                                    Utils.instance.displayInformationalDialog(this@EditConfirmedEventActivity, "Success",
                                            "Changes saved", true)
                                } else if (response.code() == 401) {
                                    Utils.instance.displayInformationalDialog(this@EditConfirmedEventActivity, "Error",
                                            "401: Unauthorized access", true)
                                } else if (response.code() == 500) {
                                    Utils.instance.displayInformationalDialog(this@EditConfirmedEventActivity,
                                            "Error", "Server issue, please try again later", false)
                                }
                                binding.btnSave.isEnabled = true
                                binding.pbLoading.visibility = View.INVISIBLE
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                Utils.instance.displayInformationalDialog(this@EditConfirmedEventActivity,
                                        "Error", "Can't connect to server", false)
                                binding.btnSave.isEnabled = true
                                binding.pbLoading.visibility = View.INVISIBLE
                            }

                        })
            }
            dialogAlert.setNegativeButton("No"){di:DialogInterface, i:Int ->}
            dialogAlert.create().show()
        }



    }

    private fun getAndLoadEvent(){
        binding.pbLoading.visibility = View.VISIBLE
        val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
        RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token!!)
                .getEvent(eventID).enqueue(object: Callback<Event> {
                    override fun onResponse(call: Call<Event>, response: Response<Event>) {
                        if (response.code() == 200){
                            event = response.body()!!
                            loadEvent()
                            setClickListeners()
                            binding.btnSave.isEnabled = true

                        }
                        else if (response.code() == 401){
                            Utils.instance.displayInformationalDialog(this@EditConfirmedEventActivity,
                                    "Error", "401: Unauthorized access", true)
                        }
                        else if (response.code() == 404){
                            Utils.instance.displayInformationalDialog(this@EditConfirmedEventActivity,
                                    "Error", "Event not found", true)
                        }
                        else if (response.code() == 500){
                            Utils.instance.displayInformationalDialog(this@EditConfirmedEventActivity,
                                    "Error", "Server issue, please try again later", true)
                        }
                        binding.pbLoading.visibility = View.INVISIBLE
                    }

                    override fun onFailure(call: Call<Event>, t: Throwable) {
                        Utils.instance.displayInformationalDialog(this@EditConfirmedEventActivity,
                                "Error", "Can't connect to server", true)
                        binding.pbLoading.visibility = View.INVISIBLE
                    }

                })
    }


    private fun loadEvent(){
        startDate = LocalDate.parse(event.startDate, DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        endDate = LocalDate.parse(event.endDate, DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))

        binding.tvStartDate.text =
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY).format(startDate)

        binding.tvEndDate.text =
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY).format(endDate)

        loadSessions()

        if (event.locatedEventData!=null){
            locationLatLng = LatLng(event.locatedEventData!!.location[0],event.locatedEventData!!.location[0])
            val locationData = Geocoder(applicationContext).getFromLocation(locationLatLng.latitude,
                    locationLatLng.longitude, 1)
            val locationName = if (locationData.size == 0) "Unnamed location" else locationData[0].featureName
            binding.tvSelectedLocation.text = locationName
        }
        else{
            binding.llCityAndLocation.visibility = View.GONE
        }

        registrationCloseDate = LocalDateTime.parse(event.registrationCloseDateTime,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DEFAULT)).toLocalDate()
        val temp = LocalDateTime.parse(event.registrationCloseDateTime,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DEFAULT)).toLocalTime()
        registrationCloseTime = TimeStamp(temp.hour, temp.minute)

        binding.tvRegistrationCloseDate.text = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY).format(registrationCloseDate)
        binding.tvRegistrationCloseTime.text = registrationCloseTime.format12H()


    }


    private fun createRecyclerView(size: Int){
        val dateTimeFormatter = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY)
        val dates = ArrayList<String>()
        for(i in 1 until size){
            dates.add(startDate.plusDays(i.toLong()).dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    +",\n"+ dateTimeFormatter.format(startDate.plusDays(i.toLong())))
        }
        val adapter = SessionInputAdapter(dates, event.isLimitedLocated(), firstSessionStartTime, firstSessionEndTime, firstSessionCheckInTime)
        binding.rvSessions.adapter = adapter
        val layoutManager = object: LinearLayoutManager(this) {
            override fun canScrollVertically():Boolean =  false
        }
        binding.rvSessions.layoutManager = layoutManager
        binding.cvFirstSession.visibility = View.VISIBLE
        binding.cbEnableSession.text = startDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())+",\n"+dateTimeFormatter.format(startDate)
        binding.cbEnableSession.isEnabled = false

    }

    private fun prepareRecyclerView(){
        val dates = ArrayList<String>()
        for(i in 0 until sessionCount-1){
            dates.add("Loading")
        }
        val adapter = SessionInputAdapter(dates, false,
                TimeStamp(-1,-1),TimeStamp(-1,-1),TimeStamp(-1,-1))
        binding.rvSessions.adapter = adapter
        val layoutManager = object: LinearLayoutManager(this) {
            override fun canScrollVertically():Boolean =  false
        }
        binding.rvSessions.layoutManager = layoutManager
    }

    private fun loadSessions(){
        val dateTimeFormatterDateDisplay = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY)
        val dateTimeFormatterDateDefault = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT)
        val dateTimeFormatterTimeDefault = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)
        val firstSessionStartTimeTemp = LocalTime.parse(event.sessions[0].startTime, dateTimeFormatterTimeDefault)
        firstSessionStartTime = TimeStamp(firstSessionStartTimeTemp.hour, firstSessionStartTimeTemp.minute)
        val firstSessionEndTimeTemp = LocalTime.parse(event.sessions[0].endTime, dateTimeFormatterTimeDefault)
        firstSessionEndTime = TimeStamp(firstSessionEndTimeTemp.hour, firstSessionEndTimeTemp.minute)

        if (event.isLimitedLocated()) {
            val firstSessionCheckInTimeTemp = LocalTime.parse(event.sessions[0].checkInTime, dateTimeFormatterTimeDefault)
            firstSessionCheckInTime = TimeStamp(firstSessionCheckInTimeTemp.hour, firstSessionCheckInTimeTemp.minute)
        }
        binding.cbEnableSession.isEnabled = false
        binding.cbEnableSession.text = startDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())+",\n"+
                dateTimeFormatterDateDisplay.format(startDate)
        binding.tvStartTime.text = TimeStamp(firstSessionStartTime.hour, firstSessionStartTime.minute).format12H()
        binding.btnEndTime.isEnabled = true
        binding.tvEndTime.text = TimeStamp(firstSessionEndTime.hour, firstSessionEndTime.minute).format12H()

        if (event.isLimitedLocated()){
            binding.btnCheckInTime.isEnabled = true
            binding.tvCheckInTime.text = TimeStamp(firstSessionCheckInTime.hour, firstSessionCheckInTime.minute).format12H()
        }

        for (i in 0 until (binding.rvSessions.adapter?.itemCount!!)) {
            val holder = binding.rvSessions.findViewHolderForLayoutPosition(i) as SessionInputAdapter.SessionInputHolder
            val sTime = LocalTime.parse(event.sessions[i+1].startTime, dateTimeFormatterTimeDefault)
            val eTime = LocalTime.parse(event.sessions[i+1].endTime, dateTimeFormatterTimeDefault)
            holder.startTime = TimeStamp(sTime.hour, sTime.minute)
            holder.binding.tvStartTime.text = holder.startTime.format12H()
            holder.binding.btnEndTime.isEnabled = true

            holder.endTime = TimeStamp(eTime.hour, eTime.minute)
            holder.binding.tvEndTime.text = holder.endTime.format12H()

            if (event.isLimitedLocated()){
                val cTime = LocalTime.parse(event.sessions[i+1].checkInTime, dateTimeFormatterTimeDefault)
                holder.binding.btnCheckInTime.isEnabled = true
                holder.checkInTime = TimeStamp(cTime.hour, cTime.minute)
                holder.binding.tvCheckInTime.text = holder.checkInTime.format12H()
            }

            val date = LocalDate.parse(event.sessions[i+1].date,dateTimeFormatterDateDefault)
            holder.binding.cbEnableSession.text = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())+",\n"+
                    dateTimeFormatterDateDisplay.format(date)

            holder.binding.cbEnableSession.isEnabled = i != (binding.rvSessions.adapter?.itemCount!!) - 1
        }


    }

    private fun setClickListenersForFirstSession(){

        binding.cbEnableSession.isChecked = true
        binding.btnEndTime.isEnabled = false
        binding.btnCheckInTime.isEnabled = false
        binding.btnStartTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select start time")
                    .build()
            picker.addOnPositiveButtonClickListener {

                firstSessionStartTime = TimeStamp(picker.hour, picker.minute)
                binding.tvStartTime.text = firstSessionStartTime.format12H()
                binding.btnEndTime.isEnabled = true
                binding.tvEndTime.text = getString(R.string.select_time)
                binding.tvCheckInTime.text = getString(R.string.select_time)
                if (event.isLimitedLocated()){
                    if (binding.btnEndTime.isEnabled){
                        binding.btnCheckInTime.isEnabled = true
                    }
                }
                else{
                    binding.btnCheckInTime.isEnabled = false
                    binding.tvCheckInTime.text = getString(R.string.select_time)
                    firstSessionCheckInTime = TimeStamp(-1,-1)
                }
                applyStatusToAllSessions()
                setDateError()
            }

            picker.show(supportFragmentManager, "sessionStartTime")

        }

        binding.btnEndTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select end time")
                    .build()
            picker.addOnPositiveButtonClickListener {
                if (TimeStamp(picker.hour, picker.minute).minusInMinutes(firstSessionStartTime) > 12*60){
                    AlertDialog.Builder(this)
                            .setTitle(getString(R.string.time_error))
                            .setMessage(getString(R.string.session_time_limit_error))
                            .setPositiveButton(getString(R.string.ok)){ dialogInterface: DialogInterface, i: Int -> }
                            .create().show()
                }
                else if (TimeStamp(picker.hour, picker.minute).minusInMinutes(firstSessionStartTime)  < 0){
                    AlertDialog.Builder(this)
                            .setTitle(getString(R.string.time_error))
                            .setMessage(getString(R.string.end_time_before_start_time_error))
                            .setPositiveButton(getString(R.string.ok)){ dialogInterface: DialogInterface, i: Int -> }
                            .create().show()
                }
                else {
                    firstSessionEndTime = TimeStamp(picker.hour, picker.minute)
                    binding.tvEndTime.text = firstSessionEndTime.format12H()
                    applyStatusToAllSessions()
                    setDateError()

                }
            }

            picker.show(supportFragmentManager, "sessionEndTime")

        }


        binding.btnCheckInTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select check-in time")
                    .build()
            picker.addOnPositiveButtonClickListener {
                if (firstSessionStartTime.minusInMinutes(TimeStamp(picker.hour, picker.minute)) > 3 * 60){
                    AlertDialog.Builder(this)
                            .setTitle(getString(R.string.time_error))
                            .setMessage(getString(R.string.check_in_time_limit_error))
                            .setPositiveButton(getString(R.string.ok)){ dialogInterface: DialogInterface, i: Int -> }
                            .create().show()
                }else if (firstSessionStartTime.minusInMinutes(TimeStamp(picker.hour, picker.minute))  < 0){
                    AlertDialog.Builder(this)
                            .setTitle(getString(R.string.time_error))
                            .setMessage(getString(R.string.check_in_time_before_start_time_error))
                            .setPositiveButton(getString(R.string.ok)){ dialogInterface: DialogInterface, i: Int -> }
                            .create().show()
                }else{
                    firstSessionCheckInTime = TimeStamp(picker.hour, picker.minute)
                    binding.tvCheckInTime.text = firstSessionCheckInTime.format12H()
                    applyStatusToAllSessions()
                }

            }

            picker.show(supportFragmentManager, "sessionStartTime")
        }

        binding.cbEnableSession.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked){
                binding.btnStartTime.isEnabled = false
                binding.btnEndTime.isEnabled = false
                binding.btnCheckInTime.isEnabled = false
            }
            else{
                binding.btnStartTime.isEnabled = true
                if (binding.tvStartTime.text.toString() != getString(R.string.select_time)){
                    binding.btnEndTime.isEnabled = true
                    if (event.isLimitedLocated()){
                        if (binding.btnEndTime.isEnabled){
                            binding.btnCheckInTime.isEnabled = true
                        }
                    }
                    else{
                        binding.btnCheckInTime.isEnabled = false
                        binding.tvCheckInTime.text = getString(R.string.select_time)
                        firstSessionCheckInTime = TimeStamp(-1,-1)
                    }
                }
            }
            setDateError()
        }
    }

    private fun applyStatusToAllSessions() {
        if (binding.rvSessions.adapter != null) {
            for (i in 0 until (binding.rvSessions.adapter?.itemCount!!)) {
                val holder = binding.rvSessions.findViewHolderForLayoutPosition(i) as SessionInputAdapter.SessionInputHolder
                if (holder.binding.cbEnableSession.isChecked) {
                    holder.binding.tvStartTime.text = binding.tvStartTime.text
                    holder.binding.tvEndTime.text = binding.tvEndTime.text
                    holder.binding.tvCheckInTime.text = binding.tvCheckInTime.text
                    holder.binding.btnEndTime.isEnabled = binding.btnEndTime.isEnabled
                    holder.binding.btnCheckInTime.isEnabled = binding.btnCheckInTime.isEnabled
                    holder.startTime = TimeStamp(firstSessionStartTime.hour, firstSessionStartTime.minute)
                    holder.endTime = TimeStamp(firstSessionEndTime.hour, firstSessionEndTime.minute)
                    holder.checkInTime = TimeStamp(firstSessionCheckInTime.hour, firstSessionCheckInTime.minute)

                }
            }
        }
    }

    override fun setDateError(){
        if (binding.rvSessions.adapter==null){
            binding.tvDateError.text = getString(R.string.no_date_error)
        }
        else{
            var datesProvided = true
            for (i in 0 until (binding.rvSessions.adapter?.itemCount!!)) {
                val holder = binding.rvSessions.findViewHolderForLayoutPosition(i) as SessionInputAdapter.SessionInputHolder
                if (holder.binding.cbEnableSession.isChecked) {
                    if ((holder.binding.tvStartTime.text == getString(R.string.select_time) ||
                                    holder.binding.tvEndTime.text == getString(R.string.select_time))
                            && holder.binding.cbEnableSession.isEnabled) {
                        datesProvided = false
                        break
                    }
                }
            }

            if (binding.tvStartTime.text == getString(R.string.select_time) ||
                    binding.tvEndTime.text == getString(R.string.select_time)){
                datesProvided = false
            }

            if (datesProvided){
                binding.tvDateError.text = ""
            }
            else{
                binding.tvDateError.text = getString(R.string.session_times_error)
            }

        }
    }

    private fun setClickListeners(){
        binding.btnSelectDates.setOnClickListener {
            val builder: MaterialDatePicker.Builder<Pair<Long, Long>> = MaterialDatePicker.Builder.dateRangePicker()

            val calendarConstraints = CalendarConstraints.Builder()
            val startConstraint = startDate
            val min = startConstraint.plusDays(1).atStartOfDay(ZoneId.systemDefault())
                    .toInstant().toEpochMilli()
            calendarConstraints.setStart(min)
            calendarConstraints.setValidator(DateValidatorPointForward.from(min))
            builder.setCalendarConstraints(calendarConstraints.build())
            builder.setTitleText(getString(R.string.select_start_end_dates))
            val picker = builder.build()
            picker.addOnPositiveButtonClickListener {
                val from: LocalDate = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(it.first!!),
                        ZoneId.systemDefault()).toLocalDate()
                val to: LocalDate = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(it.second!!),
                        ZoneId.systemDefault()).toLocalDate()
                val diff: Int = Duration.between(from.atStartOfDay(), to.atStartOfDay()).toDays().toInt()
                if (diff > DATE_PERIOD_LIMIT){
                    AlertDialog.Builder(this)
                            .setTitle(getString(R.string.date_error))
                            .setMessage(getString(R.string.date_period_error_text))
                            .setPositiveButton(getString(R.string.ok)){ dialogInterface: DialogInterface, i: Int -> }
                            .create().show()
                }
                else {
                    binding.tvStartDate.text = from.toString()
                    binding.tvEndDate.text = to.toString()
                    startDate = from
                    endDate = to
                    registrationCloseDate = LocalDate.from(startDate)
                    createRecyclerView(diff + 1) //diff = number of days - 1
                    if (firstSessionStartTime.hour ==-1 && firstSessionEndTime.hour == -1)
                        binding.tvDateError.text = getString(R.string.session_times_error)
                    else binding.tvDateError.text = ""
                    binding.btnRegistrationCloseDate.isEnabled = true
                    binding.btnRegistrationCloseTime.isEnabled = false
                    binding.tvRegistrationCloseDate.text = getString(R.string.select_date)
                    binding.tvRegistrationCloseTime.text = getString(R.string.select_time)
                    registrationCloseTime = TimeStamp(-1,-1)
                }
            }
            picker.show(supportFragmentManager, builder.build().toString())
        }



        binding.btnRegistrationCloseDate.setOnClickListener {
            val builder: MaterialDatePicker.Builder<Long> = MaterialDatePicker.Builder.datePicker()
            val calendarConstraints = CalendarConstraints.Builder()
            val startConstraint = startDate.minusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endConstraint = startDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            calendarConstraints.setStart(startConstraint)
            calendarConstraints.setEnd(endConstraint)
            calendarConstraints.setValidator(object: CalendarConstraints.DateValidator{
                override fun describeContents(): Int {
                    TODO("not to be implemented")
                }

                override fun writeToParcel(dest: Parcel?, flags: Int) {
                    TODO("not to be implemented")
                }

                override fun isValid(date: Long): Boolean = !(startConstraint > date || endConstraint < date)

            })
            builder.setCalendarConstraints(calendarConstraints.build())
            builder.setTitleText(getString(R.string.select_registration_close_date))
            val picker = builder.build()

            picker.addOnPositiveButtonClickListener {
                registrationCloseDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(it!!), ZoneId.systemDefault()).toLocalDate()
                binding.tvRegistrationCloseDate.text = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY).format(registrationCloseDate)
                binding.btnRegistrationCloseTime.isEnabled = true
                registrationCloseTime = TimeStamp(firstSessionStartTime.hour,firstSessionStartTime.minute)
                binding.tvRegistrationCloseTime.text = registrationCloseTime.format12H()
            }

            picker.show(supportFragmentManager, builder.build().toString())


        }

        binding.btnRegistrationCloseTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select registration close time")
                    .build()

            picker.addOnPositiveButtonClickListener {
                if (startDate.minusDays(2).dayOfMonth == registrationCloseDate.dayOfMonth){
                    if (TimeStamp(picker.hour, picker.minute).minusInMinutes(firstSessionStartTime)<0){
                        AlertDialog.Builder(this)
                                .setTitle(getString(R.string.time_error))
                                .setMessage(getString(R.string.registration_close_time_error))
                                .setPositiveButton(getString(R.string.ok)){ dialogInterface: DialogInterface, i: Int -> }
                                .create().show()
                    }
                    else{
                        registrationCloseTime = TimeStamp(picker.hour, picker.minute)
                        binding.tvRegistrationCloseTime.text = registrationCloseTime.format12H()
                    }
                }
                else{
                    registrationCloseTime = TimeStamp(picker.hour, picker.minute)
                    binding.tvRegistrationCloseTime.text = registrationCloseTime.format12H()
                }
            }

            picker.show(supportFragmentManager, "registrationCloseTime")
        }

        binding.btnSelectLocation.setOnClickListener {
            locationActivityResult.launch(Intent(this, SelectLocationActivity::class.java))
        }
    }

}