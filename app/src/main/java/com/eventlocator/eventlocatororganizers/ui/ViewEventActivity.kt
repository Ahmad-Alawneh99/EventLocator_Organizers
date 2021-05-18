package com.eventlocator.eventlocatororganizers.ui

import android.R.attr.bitmap
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.data.*
import com.eventlocator.eventlocatororganizers.databinding.ActivityViewEventBinding
import com.eventlocator.eventlocatororganizers.databinding.SessionDisplayBinding
import com.eventlocator.eventlocatororganizers.retrofit.EventService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputLayout
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.math.BigDecimal
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.*


class ViewEventActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewEventBinding
    lateinit var event: Event
    var eventID:Long = 0

    private val menu_group_id = 1
    private val view_feedback_id = 1
    private val view_participants_id = 2
    private val cancel_event_id = 3
    private val edit_event_id = 4
    private val edit_whole_event_id = 5
    private val view_statistics_id = 6
    private val email_particiapnts_id = 7
    private val share_on_twitter_id = 8
    lateinit var cities: List<String>
    lateinit var editEventLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cities = listOf(getString(R.string.Amman), getString(R.string.Zarqa), getString(R.string.Balqa), getString(R.string.Madaba), getString(R.string.Irbid), getString(R.string.Mafraq), getString(R.string.Jerash), getString(R.string.Ajloun), getString(R.string.Karak), getString(R.string.Aqaba), getString(R.string.Maan), getString(R.string.Tafila))
        eventID = intent.getLongExtra("eventID", -1)
        editEventLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if (result.resultCode == RESULT_OK){
                eventID = result.data!!.getLongExtra("newEventId",-1)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getAndLoadEvent()
    }

    fun loadEvent(){
        val imageBitmap = BitmapFactory.
        decodeStream(ByteArrayInputStream(Base64.decode(event.image, Base64.DEFAULT)))
        binding.ivEventImage.setImageBitmap(imageBitmap)
        binding.ivEventImage.setOnClickListener {
            val intent = Intent(this, ViewImageActivity::class.java)
            intent.putExtra("image", event.image)
            startActivity(intent)
        }
        val startDateFormatted = LocalDate.parse(event.startDate, DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        val endDateFormatted = LocalDate.parse(event.endDate, DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))

        binding.tvEventName.text = event.name
        binding.tvEventDate.text = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY)
            .format(startDateFormatted) + " - " + DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY)
            .format(endDateFormatted)

        if (event.isLimitedLocated() && event.getCurrentLimitedSessionIncludingCheckInTime()!=null){
            binding.btnCheckInParticipant.visibility = View.VISIBLE
            binding.btnCheckInParticipant.setOnClickListener{
                val intent = Intent(this, CheckInParticipantActivity::class.java)
                intent.putExtra("eventName", event.name)
                intent.putExtra("eventID", event.id)
                intent.putExtra("currentSession", event.getCurrentLimitedSessionIncludingCheckInTime())
                startActivity(intent)
            }
        }
        else{
            binding.btnCheckInParticipant.visibility = View.INVISIBLE
        }


        binding.tvRating.text = if(event.isFinished()) {
            if (event.rating == 0.0) "No ratings yet"
            else BigDecimal(event.rating).setScale(2).toString() + "/5"
        }
        else ""
        if (!event.isFinished()){
            binding.llRating.visibility = View.GONE
        }

        binding.tvEventStatus.text = event.getStatus()
        binding.tvDescription.text = event.description

        val layoutManager = object: LinearLayoutManager(this) {
            override fun canScrollVertically():Boolean =  false
        }
        binding.rvSessions.layoutManager = layoutManager
        val adapter = SessionDisplayAdapter(event.sessions)
        binding.rvSessions.adapter = adapter

        binding.tvMaxNumOfParticipants.text = if(event.maxParticipants>0)event.maxParticipants.toString()
            else getString(R.string.no_limit)

        binding.tvNumOfParticipants.text = event.currentNumberOfParticipants.toString()



        if (event.locatedEventData!=null) {
            binding.llCity.visibility = View.VISIBLE
            binding.llLocation.visibility = View.VISIBLE
            binding.tvCity.text = cities[event.locatedEventData!!.city]
            binding.tvLocation.setOnClickListener {
                val intent = Intent(this, ViewLocationActivity::class.java)
                val latLng = LatLng(event.locatedEventData!!.location[0], event.locatedEventData!!.location[1])
                intent.putExtra("latLng", latLng)
                startActivity(intent)
            }
        }
        else{
            binding.llCity.visibility = View.GONE
            binding.llLocation.visibility = View.GONE
        }


        val registrationCloseDateTime = LocalDateTime.parse(event.registrationCloseDateTime,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DEFAULT))
        binding.tvRegistrationCloseDateTime.text = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DISPLAY)
            .format(registrationCloseDateTime)

        var categories = ""
        for(i in 0 until event.categories.size){
            categories+= when(event.categories[i]){
                EventCategory.EDUCATIONAL.ordinal -> getString(R.string.educational)
                EventCategory.ENTERTAINMENT.ordinal -> getString(R.string.entertainment)
                EventCategory.VOLUNTEERING.ordinal -> getString(R.string.volunteering)
                EventCategory.SPORTS.ordinal -> getString(R.string.sports)
                else -> ""
            }
            if (i!=event.categories.size-1)categories+=','
        }

        binding.tvCategories.text = categories

        if (event.canceledEventData!=null) {
            binding.llCancellationDate.visibility = View.VISIBLE
            binding.llCancellationReason.visibility = View.VISIBLE

            val cancellationDateTime = LocalDateTime.parse(event.canceledEventData!!.cancellationDateTime,
                    DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DEFAULT))

            binding.tvCancellationDateTime.text = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DISPLAY)
                    .format(cancellationDateTime)
            binding.tvCancellationReason.text = event.canceledEventData!!.cancellationReason
        }
        else{
            binding.llCancellationDate.visibility = View.GONE
            binding.llCancellationReason.visibility = View.GONE
        }

        if (event.whatsAppLink != null || event.whatsAppLink == ""){
            binding.llWhatsAppGroup.visibility = View.GONE
        }
        else{
            binding.llWhatsAppGroup.visibility = View.VISIBLE
            binding.tvWhatsAppGroup.text = event.whatsAppLink
        }


    }

    private fun getAndLoadEvent(){
        binding.pbLoading.visibility = View.VISIBLE
        val sharedPreference = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
        val token = sharedPreference.getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
        RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token!!)
                .getEvent(eventID).enqueue(object : Callback<Event> {
                    override fun onResponse(call: Call<Event>, response: Response<Event>) {
                        if (response.code() == 200) {
                            event = response.body()!!
                            loadEvent()
                            invalidateOptionsMenu()
                        } else if (response.code() == 401) {
                            Utils.instance.displayInformationalDialog(this@ViewEventActivity,
                                    "Error", "401: Unauthorized access", true)
                        } else if (response.code() == 404) {
                            Utils.instance.displayInformationalDialog(this@ViewEventActivity,
                                    "Error", "Event not found", true)
                        } else if (response.code() == 500) {
                            Utils.instance.displayInformationalDialog(this@ViewEventActivity,
                                    "Error", "Server issue, please try again later", true)
                        }
                        binding.pbLoading.visibility = View.INVISIBLE
                    }

                    override fun onFailure(call: Call<Event>, t: Throwable) {
                        Utils.instance.displayInformationalDialog(this@ViewEventActivity,
                                "Error", "Can't connect to server", true)
                        binding.pbLoading.visibility = View.INVISIBLE
                    }

                })
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (this::event.isInitialized)
            loadMenuItems(menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            view_feedback_id -> {
                val intent = Intent(this, ViewEventFeedbackActivity::class.java)
                intent.putExtra("eventID", event.id)
                intent.putExtra("totalParticipants", event.currentNumberOfParticipants)
                startActivity(intent)
            }
            email_particiapnts_id -> {
                val intent = Intent(this, EmailParticipantsOfAnEvent::class.java)
                intent.putExtra("eventID", event.id)
                startActivity(intent)
            }
            view_statistics_id -> {
                val intent = Intent(this, EventStatisticsActivity::class.java)
                intent.putExtra("eventID", event.id)
                intent.putExtra("eventName", event.name)
                intent.putExtra("sessions", event.sessions)
                startActivity(intent)
            }
            cancel_event_id -> {
                promptCancelEvent()
            }
            edit_whole_event_id -> {
                val intent = Intent(this, EditPendingEventActivity::class.java)
                intent.putExtra("eventID", event.id)
                intent.putExtra("sessionCount", event.sessions.size)
                editEventLauncher.launch(intent)
            }
            edit_event_id -> {
                val intent = Intent(this, EditConfirmedEventActivity::class.java)
                intent.putExtra("eventID", event.id)
                intent.putExtra("sessionCount", event.sessions.size)
                editEventLauncher.launch(intent)
            }
            view_participants_id -> {
                if (event.isLimitedLocated() && event.getCurrentLimitedSessionIncludingCheckInTime() != null) {
                    val intent = Intent(this, ViewParticipantsDuringALimitedLocatedSession::class.java)
                    intent.putExtra("eventID", event.id)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, ViewParticipantsOfAnEventActivity::class.java)
                    intent.putExtra("eventID", event.id)
                    startActivity(intent)
                }
            }
            share_on_twitter_id -> {
                val message = "I'm organizing ${event.name} on EventLocator app, register in my event through the app!"
                var intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.setPackage("com.twitter.android")
                intent.putExtra(Intent.EXTRA_TEXT, message)
                try {
                    startActivity(intent)
                } catch (ex: ActivityNotFoundException) {
                    val tweetUrl = "https://twitter.com/intent/tweet?text=" + URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
                    val uri: Uri = Uri.parse(tweetUrl)
                    intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadMenuItems(menu: Menu?){
        if (menu == null) return
        if (!event.isCanceled()){
            if (event.isFinished()){
                menu.add(menu_group_id, view_feedback_id, 1, "View feedback")
                if (event.isLimitedLocated()){
                    menu.add(menu_group_id, view_statistics_id, 6, "View statistics")
                }
                menu.add(menu_group_id, view_participants_id, 2, "View participants")
            }
            else{
                if (!event.hasStarted()){
                    menu.add(menu_group_id, cancel_event_id, 10, "Cancel event")
                }
                val eventStartDate = LocalDate.parse(event.startDate,
                        DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
                val eventStartDateTime = eventStartDate.atTime(LocalTime.parse(event.sessions[0].startTime,
                        DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))
                if (event.status == EventStatus.PENDING.ordinal){
                    menu.add(menu_group_id, edit_whole_event_id, 4, "Edit event")
                }
                else if (LocalDateTime.now().isBefore(eventStartDateTime.minusHours(24))){
                    menu.add(menu_group_id, edit_event_id, 4, "Edit event")
                }

                if (event.status != EventStatus.PENDING.ordinal) {
                    menu.add(menu_group_id, view_participants_id, 2, "View participants")
                    menu.add(menu_group_id, email_particiapnts_id, 3, "Send email to participants")
                    menu.add(menu_group_id, share_on_twitter_id, 9, "Share on Twitter")
                }
            }

        }


    }

    private fun promptCancelEvent(){
        val layout = layoutInflater.inflate(R.layout.cancel_event, binding.root, false)
        val builder = AlertDialog.Builder(this).setTitle("Are you sure you want to cancel the event?")
        var lateCancel = false
        val eventStartDate = LocalDate.parse(event.startDate,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        val eventStartDateTime = eventStartDate.atTime(LocalTime.parse(event.sessions[0].startTime,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))
        if (LocalDateTime.now().isAfter(eventStartDateTime.minusHours(24))){
            lateCancel = true
        }
        layout.findViewById<TextView>(R.id.tvCancelWarning).isEnabled = lateCancel

        val etCancellationReason = layout.findViewById<EditText>(R.id.etCancellationReason)
        builder.setView(layout)
            .setPositiveButton("Confirm") { d: DialogInterface, i: Int ->
                val alertDialog = Utils.instance.createSimpleDialog(this, "Cancel event", "Are you sure that you want to cancel this event?")
                alertDialog.setPositiveButton("Yes"){di: DialogInterface, i: Int ->
                    val result = etCancellationReason.text.toString().trim()
                    val currentTime = LocalDateTime.now()
                    val formattedCurrentTime = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DEFAULT)
                            .format(currentTime)
                    val canceledEventData = CanceledEventData(formattedCurrentTime, result)
                    cancelEvent(canceledEventData, lateCancel)
                }
                alertDialog.setNegativeButton("No"){di: DialogInterface, i2: Int ->}
                alertDialog.create().show()

            }
            .setNegativeButton("Cancel") { d: DialogInterface, i: Int ->

            }
        val cancelDialog = builder.show()
        val positiveButton = cancelDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.isEnabled = false
        val tlCancellationReason = layout.findViewById<TextInputLayout>(R.id.tlCancellationReason)
        tlCancellationReason.error = getString(R.string.field_cant_be_empty_error)
        etCancellationReason.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (etCancellationReason.text.toString().trim() == "") {
                    tlCancellationReason.error = getString(R.string.field_cant_be_empty_error)
                    positiveButton.isEnabled = false
                } else {
                    tlCancellationReason.error = null
                    positiveButton.isEnabled = true
                }
            }

        })
    }

    private fun cancelEvent(data: CanceledEventData, lateCancel: Boolean){
        val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
            .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
        RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token!!)
            .cancelEvent(eventID, data, lateCancel).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.code() == 200) {
                            NotificationUtils.cancelNotification(this@ViewEventActivity, event.id)
                            Utils.instance.displayInformationalDialog(this@ViewEventActivity,
                                    "Success", "Event canceled", true)
                        } else if (response.code() == 500) {
                            Utils.instance.displayInformationalDialog(this@ViewEventActivity,
                                    "Error", "Server issue, please try again later", false)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Utils.instance.displayInformationalDialog(this@ViewEventActivity,
                                "Error", "Can't connect to server", false)
                    }

                })
    }


}

class SessionDisplayAdapter(private val sessions: ArrayList<Session>):
        RecyclerView.Adapter<SessionDisplayAdapter.SessionDisplayViewHolder>(){

    inner class SessionDisplayViewHolder(var binding: SessionDisplayBinding):
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionDisplayViewHolder {
        val binding = SessionDisplayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SessionDisplayViewHolder(binding)
    }


    override fun onBindViewHolder(holder: SessionDisplayViewHolder, position: Int) {
        val date = LocalDate.parse(sessions[position].date, DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        val timeFormatterDefault = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)
        val timeFormatterDisplay = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DISPLAY)
        val startTime = LocalTime.parse(sessions[position].startTime, timeFormatterDefault)
        val endTime = LocalTime.parse(sessions[position].endTime, timeFormatterDefault)
        holder.binding.tvSessionDateAndDay.text = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()) +", "+
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY).format(date)

        holder.binding.tvStartTime.text = timeFormatterDisplay.format(startTime)
        holder.binding.tvEndTime.text = timeFormatterDisplay.format(endTime)

        if (sessions[position].checkInTime!=""){
            val checkInTime = timeFormatterDefault.parse(sessions[position].checkInTime)
            holder.binding.tvCheckInTime.text = timeFormatterDisplay.format(checkInTime)
        }
        else{
            holder.binding.llCheckInTime.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = sessions.size

}