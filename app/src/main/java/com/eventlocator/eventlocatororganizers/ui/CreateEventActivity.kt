package com.eventlocator.eventlocatororganizers.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.util.Pair
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.adapters.SessionInputAdapter
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.data.LocatedEventData
import com.eventlocator.eventlocatororganizers.data.Session
import com.eventlocator.eventlocatororganizers.databinding.ActivityCreateEventBinding
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
import retrofit2.Retrofit
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class CreateEventActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreateEventBinding
    val DATE_PERIOD_LIMIT = 6
    val INSTANCE_STATE_IMAGE = "Image"
    var image: Uri? = null

    lateinit var cities: List<String>
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            image = savedInstanceState.getParcelable(INSTANCE_STATE_IMAGE)
            if (image != null) {
                binding.ivImagePreview.setImageBitmap(Utils.instance.uriToBitmap(image!!, this))
                updateCreateEventButton()
            }
        }


        binding.btnRegistrationCloseDate.isEnabled = false
        binding.btnRegistrationCloseTime.isEnabled = false
        binding.loFirstSession.visibility = View.GONE
        setClickListenersForFirstSession()
        setDateError()
        alterCityAndLocationStatus(true)
        if (image == null)
            binding.btnRemoveImage.isEnabled = false

        binding.btnCreateEvent.setOnClickListener{
            val categories = ArrayList<Int>()
            if (binding.cbEducational.isChecked) categories.add(EventCategory.EDUCATIONAL.ordinal)
            if (binding.cbEntertainment.isChecked) categories.add(EventCategory.ENTERTAINMENT.ordinal)
            if (binding.cbVolunteering.isChecked) categories.add(EventCategory.VOLUNTEERING.ordinal)
            if (binding.cbSports.isChecked) categories.add(EventCategory.SPORTS.ordinal)

            val sessions = ArrayList<Session>()
            sessions.add(Session(1,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT).format(startDate),
                firstSessionStartTime.format24H(),
                firstSessionEndTime.format24H(),
                startDate.dayOfWeek.ordinal,
                if(isLimited()) if (firstSessionCheckInTime.hour>=0) firstSessionCheckInTime.format24H() else
                firstSessionStartTime.format24H() else ""))

            for (i in 0 until (binding.rvSessions.adapter?.itemCount!!)) {
                val holder = binding.rvSessions.findViewHolderForLayoutPosition(i) as SessionInputAdapter.SessionInputHolder
                val sessionStartDate = LocalDate.parse(holder.binding.cbEnableSession.text.toString(),
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
                sessions.add(Session(i+2,
                        DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT).format(sessionStartDate),
                        holder.startTime.format24H(),
                        holder.endTime.format24H(),
                        sessionStartDate.dayOfWeek.ordinal,
                        if (isLimited()) if(holder.checkInTime.hour>=0) holder.checkInTime.format24H() else
                            holder.startTime.format24H() else ""))
            }

            val temp = if (this::registrationCloseDate.isInitialized)
                registrationCloseDate.atTime(registrationCloseTime.hour, registrationCloseTime.minute)
            else
                startDate.atTime(firstSessionStartTime.hour, firstSessionStartTime.minute)

            val eventBuilder = Event.EventBuilder(
                binding.etEventName.text.toString().trim(),
                binding.etEventDescription.text.toString(),
                categories,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT).format(startDate),
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT).format(endDate),
                sessions,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DEFAULT).format(temp)
            )

            if (binding.etNumberOfParticipants.text.toString().trim()!="")
                eventBuilder.setMaxParticipants(binding.etNumberOfParticipants.text.toString().trim().toInt())


            val locatedEventData = if (binding.rbLocated.isChecked) {
                val location = ArrayList<Double>()
                location.add(locationLatLng.latitude)
                location.add(locationLatLng.longitude)
                LocatedEventData(cities.indexOf(binding.acCityMenu.text.toString()), location)
            }
            else null
            eventBuilder.setLocatedEventData(locatedEventData)

            eventBuilder.setWhatsAppLink(binding.etWhatAppLink.text.toString().trim())

            val event = eventBuilder.build()
            val inputStream = contentResolver.openInputStream(image!!)
            val eventImagePart: RequestBody = RequestBody.create(
                    MediaType.parse("image/*"), inputStream?.readBytes()!!
            )
            val eventImageMultipartBody = MultipartBody.Part.createFormData("image","image", eventImagePart)

            val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                    .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")

            RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token!!)
                    .createEvent(eventImageMultipartBody, event).enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.code() == 201) {
                                finish()
                            }
                            else if (response.code()==401){
                                Utils.instance.displayInformationalDialog(this@CreateEventActivity, "Error",
                                        "401: Unauthorized access",true)
                            }
                            else if (response.code() == 500){
                                Utils.instance.displayInformationalDialog(this@CreateEventActivity,
                                        "Error", "Server issue, please try again later", false)
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            Utils.instance.displayInformationalDialog(this@CreateEventActivity,
                                    "Error", "Can't connect to server", false)
                        }

                    })
        }

        val imageActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val bitmap = Utils.instance.uriToBitmap(result.data?.data!!, this)
                    binding.ivImagePreview.setImageBitmap(bitmap)
                    binding.btnRemoveImage.isEnabled = true
                    image = result.data!!.data
                    updateCreateEventButton()
                }
            }
        }

        val locationActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if (result.resultCode == Activity.RESULT_OK) {
                locationLatLng = result.data?.getParcelableExtra("latlng")!!
                locationName = result.data?.getStringExtra("name")!!
                binding.tvSelectedLocation.text = locationName
                updateCreateEventButton()
            }
        }

        binding.etEventName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.etEventName.text.toString().trim() == "") {
                    binding.tlEventName.error = getString(R.string.name_cant_be_empty_error)
                } else if (binding.etEventName.text.toString().trim().length > 128) {
                    binding.tlEventName.error = getString(R.string.event_name_length_error)
                } else {
                    binding.tlEventName.error = null
                }
                updateCreateEventButton()
            }

        })

        binding.etEventName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                binding.etEventName.setText(binding.etEventName.text.toString().trim(),
                    TextView.BufferType.EDITABLE)
                updateCreateEventButton()
            }
        }

        binding.etEventDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.etEventDescription.text.toString().trim() == "") {
                    binding.tlEventDescription.error = getString(R.string.field_cant_be_empty_error)
                } else if (Utils.instance.countWords(binding.etEventDescription.text.toString()) < 100) {
                    binding.tlEventDescription.error = getString(R.string.event_description_min_length_error)
                } else if (binding.etEventDescription.text.toString().trim().length > 65535) {
                    binding.tlEventDescription.error = getString(R.string.max_number_of_characters_error)
                } else {
                    binding.tlEventDescription.error = null
                }
                updateCreateEventButton()
            }

        })

        binding.etEventDescription.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) binding.etEventDescription.setText(
                    Utils.instance.connectWordsIntoString(binding.etEventDescription.text.toString().trim().split(' ')))
            updateCreateEventButton()
        }

        binding.cbEducational.setOnCheckedChangeListener { buttonView, isChecked ->
            updateEventCategoryStatus()
        }
        binding.cbEntertainment.setOnCheckedChangeListener { buttonView, isChecked ->
            updateEventCategoryStatus()
        }
        binding.cbVolunteering.setOnCheckedChangeListener { buttonView, isChecked ->
            updateEventCategoryStatus()
        }
        binding.cbSports.setOnCheckedChangeListener { buttonView, isChecked ->
            updateEventCategoryStatus()
        }

        binding.btnUploadImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            imageActivityResult.launch(Intent.createChooser(intent, getString(R.string.select_an_image)))
        }

        binding.btnRemoveImage.setOnClickListener {
            binding.ivImagePreview.setImageBitmap(null)
            binding.btnRemoveImage.isEnabled = false
            image = null
            updateCreateEventButton()
        }

        binding.rbOnline.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                alterCityAndLocationStatus(true)
            }
            else{
                alterCityAndLocationStatus(false)
            }
            alterLimitedLocatedSessions(isLimited())
        }

        binding.rbLocated.setOnCheckedChangeListener { buttonView, isChecked ->
            alterLimitedLocatedSessions(isLimited()) //needed because the other one will be called before the check actually changes
            updateCreateEventButton()
        }

        binding.etNumberOfParticipants.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.etNumberOfParticipants.text.toString().trim() !="" &&
                        binding.etNumberOfParticipants.text.toString().trim().toInt() !in 1 .. 1000) {
                    binding.tlNumberOfParticipants.error = getString(R.string.number_of_participants_limit_error)
                } else {
                    binding.tlNumberOfParticipants.error = null
                }
                updateCreateEventButton()
                alterLimitedLocatedSessions(isLimited())
            }

        })

        binding.etNumberOfParticipants.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus) {
                binding.etNumberOfParticipants.setText(binding.etNumberOfParticipants.text.toString().trim(),
                    TextView.BufferType.EDITABLE)
                updateCreateEventButton()
            }
        }


        binding.btnSelectDates.setOnClickListener {
            val builder: MaterialDatePicker.Builder<Pair<Long, Long>> = MaterialDatePicker.Builder.dateRangePicker()

            val calendarConstraints = CalendarConstraints.Builder()
            val startConstraint = LocalDate.now()
            val min = startConstraint.plusDays(2).atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
            calendarConstraints.setStart(min)
            calendarConstraints.setValidator(DateValidatorPointForward.from(min))
            builder.setCalendarConstraints(calendarConstraints.build())
            builder.setTitleText(getString(R.string.select_start_end_dates))
            val picker = builder.build()
            picker.addOnPositiveButtonClickListener {
                val from: LocalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.first!!),
                    ZoneId.systemDefault()).toLocalDate()
                val to: LocalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.second!!),
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
                    createRecyclerView(diff + 1) //diff = number of days - 1
                    binding.tvDateError.text = getString(R.string.session_times_error)
                    binding.btnRegistrationCloseDate.isEnabled = true
                    binding.btnRegistrationCloseTime.isEnabled = false
                    binding.tvRegistrationCloseDate.text = getString(R.string.select_date)
                    binding.tvRegistrationCloseTime.text = getString(R.string.select_time)
                    registrationCloseTime = TimeStamp(-1,-1)
                }
            }
            picker.show(supportFragmentManager, builder.build().toString())
        }

        cities = listOf(getString(R.string.Amman),getString(R.string.Zarqa),getString(R.string.Balqa)
            ,getString(R.string.Madaba),getString(R.string.Irbid),getString(R.string.Mafraq)
            ,getString(R.string.Jerash),getString(R.string.Ajloun),getString(R.string.Karak)
            ,getString(R.string.Aqaba),getString(R.string.Maan),getString(R.string.Tafila))

        val cityAdapter = ArrayAdapter(this, R.layout.city_list_item, cities)
        binding.acCityMenu.setAdapter(cityAdapter)
        Toast.makeText(this, binding.acCityMenu.text.toString(), Toast.LENGTH_SHORT).show()
        binding.acCityMenu.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                updateCreateEventButton()
            }

        })


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
                binding.tvRegistrationCloseDate.text = registrationCloseDate.toString()
                binding.btnRegistrationCloseTime.isEnabled = true
                registrationCloseTime = TimeStamp(firstSessionStartTime.hour,firstSessionStartTime.minute)
                if (registrationCloseTime.hour>-1) {
                    binding.tvRegistrationCloseTime.text = registrationCloseTime.format12H()
                }
            }

            picker.show(supportFragmentManager, builder.build().toString())


        }

        binding.btnRegistrationCloseTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(12)
                    .setMinute(10)
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

    fun updateCreateEventButton(){
        var enabled = binding.etEventName.text.toString().trim() != "" && binding.tlEventName.error == null
                && binding.etEventDescription.text.toString().trim() !="" && binding.tlEventDescription.error == null
                && image!=null /*&& binding.rvSessions.adapter!=null*/

        enabled = enabled && binding.tvEventCategoryError.visibility == View.INVISIBLE
                && binding.tvDateError.text == ""
        if (binding.rbLocated.isChecked){
            enabled = enabled && binding.acCityMenu.text.toString() != ""
                    && this::locationLatLng.isInitialized
        }

        binding.btnCreateEvent.isEnabled = enabled

    }

    private fun updateEventCategoryStatus(){
        binding.tvEventCategoryError.visibility = if (!(binding.cbEducational.isChecked || binding.cbEntertainment.isChecked ||
                binding.cbVolunteering.isChecked || binding.cbSports.isChecked)) View.VISIBLE else View.INVISIBLE
        updateCreateEventButton()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(INSTANCE_STATE_IMAGE, image)
    }

    private fun alterCityAndLocationStatus(b: Boolean){
        binding.tlCityMenu.isEnabled = !b
        binding.btnSelectLocation.isEnabled = !b
        binding.tvCityAndLocationWarning.visibility = if(b)View.VISIBLE else View.INVISIBLE
        updateCreateEventButton()
    }

    private fun createRecyclerView(size: Int){
        val dates = ArrayList<String>()
        for(i in 1 until size){
            dates.add(startDate.plusDays(i.toLong()).toString())
        }
        val adapter = SessionInputAdapter(dates, isLimited(), firstSessionStartTime, firstSessionEndTime, firstSessionCheckInTime)
        binding.rvSessions.adapter = adapter
        val layoutManager = LinearLayoutManager(binding.rvSessions.context, LinearLayoutManager.VERTICAL, false)
        binding.rvSessions.layoutManager = layoutManager
        binding.rvSessions.addItemDecoration( DividerItemDecoration(binding.rvSessions.context, layoutManager.orientation))
        binding.loFirstSession.visibility = View.VISIBLE
        binding.cbEnableSession.text = startDate.toString()
        binding.cbEnableSession.isEnabled = false


    }

    fun alterLimitedLocatedSessions(b: Boolean){
        if (binding.rvSessions.adapter!=null) {
            if (isLimited()){
                if (binding.btnEndTime.isEnabled){
                    binding.btnCheckInTime.isEnabled = true
                }
            }
            else{
                binding.btnCheckInTime.isEnabled = false
                binding.tvCheckInTime.text = getString(R.string.select_time)
                firstSessionCheckInTime = TimeStamp(-1,-1)
            }
            for (i in 0 until (binding.rvSessions.adapter?.itemCount!!)) {
                val holder = binding.rvSessions.findViewHolderForLayoutPosition(i) as SessionInputAdapter.SessionInputHolder
                holder.setLimited(b)
            }
        }
    }

    fun isLimited(): Boolean{
        return binding.rbLocated.isChecked && binding.etNumberOfParticipants.text.toString().trim()!=""
                && binding.tlNumberOfParticipants.error == null
    }

    private fun setClickListenersForFirstSession(){

        binding.cbEnableSession.isChecked = true
        binding.btnEndTime.isEnabled = false
        binding.btnCheckInTime.isEnabled = false
        binding.btnStartTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(12)
                    .setMinute(10)
                    .setTitleText("Select start time")
                    .build()
            picker.addOnPositiveButtonClickListener {

                firstSessionStartTime = TimeStamp(picker.hour, picker.minute)
                binding.tvStartTime.text = firstSessionStartTime.format12H()
                binding.btnEndTime.isEnabled = true
                binding.tvEndTime.text = getString(R.string.select_time)
                binding.tvCheckInTime.text = getString(R.string.select_time)
                if (isLimited()){
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
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(12)
                    .setMinute(10)
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
                    .setMinute(10)
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
                    if (isLimited()){
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

    fun setDateError(){
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
        updateCreateEventButton()
    }
}