package com.eventlocator.eventlocatororganizers.ui

import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.data.CanceledEventData
import com.eventlocator.eventlocatororganizers.data.Event
import com.eventlocator.eventlocatororganizers.data.LocatedEventData
import com.eventlocator.eventlocatororganizers.data.Session
import com.eventlocator.eventlocatororganizers.databinding.ActivityViewEventBinding
import com.eventlocator.eventlocatororganizers.retrofit.EventService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.*
import com.google.android.material.textfield.TextInputLayout
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class ViewEventActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewEventBinding
    lateinit var event: Event
    var menu: Menu? = null
    var eventID = 0

    private val menu_group_id = 1
    private val view_feedback_id = 1
    private val view_participants_id = 2
    private val cancel_event_id = 3
    private val edit_event_id = 4
    private val edit_whole_event_id = 5
    private val view_statistics_id = 6
    private val email_particiapnts_id = 7
    lateinit var cities: List<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cities = listOf(getString(R.string.Amman),getString(R.string.Zarqa),getString(R.string.Balqa)
                ,getString(R.string.Madaba),getString(R.string.Irbid),getString(R.string.Mafraq)
                ,getString(R.string.Jerash),getString(R.string.Ajloun),getString(R.string.Karak)
                ,getString(R.string.Aqaba),getString(R.string.Maan),getString(R.string.Tafila))
        getAndLoadEvent()





    }

    override fun onResume() {
        super.onResume()
        //getAndLoadEvent()
    }

    fun loadEvent(){
        binding.ivEventImage.setImageBitmap(BitmapFactory.
        decodeStream(ByteArrayInputStream(Base64.decode(event.image, Base64.DEFAULT))))
        val startDateFormatted = LocalDate.parse(event.startDate, DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        val endDateFormatted = LocalDate.parse(event.endDate, DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))

        binding.tvEventName.text = event.name
        binding.tvEventDate.text = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY)
            .format(startDateFormatted) + " - " + DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY)
            .format(endDateFormatted)
        binding.tvRating.text = if(isFinished())event.rating.toString()
            else "This event didn't finish yet"
        binding.tvEventStatus.text = getEventStatus()
        binding.tvDescription.text = event.description

        val adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, event.sessions)
        binding.lvSessionTimes.adapter = adapter

        binding.tvMaxNumOfParticipants.text = if(event.maxParticipants>0)event.maxParticipants.toString()
            else getString(R.string.no_limit)

        binding.tvNumOfParticipants.text = event.participants.size.toString()

        //TODO: surround optional data with card views and hide them when there is no data

        if (event.locatedEventData!=null) {
            binding.tvCity.text = cities[event.locatedEventData!!.city]
            val location = Geocoder(this).getFromLocation(
                    event.locatedEventData!!.location[0],
                    event.locatedEventData!!.location[1], 1)

            binding.tvLocation.text = if (location.size == 0) "Unnamed location" else location[0].getAddressLine(0)
            //TODO: Set click listener to show location on map
        }


        val registrationCloseDateTime = LocalDateTime.parse(event.registrationCloseDateTime,
            DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DEFAULT))
        binding.tvRegistrationCloseDateTime.text = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DISPLAY)
            .format(registrationCloseDateTime)

        //TODO: find a better way to do this
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
            val cancellationDateTime = LocalDateTime.parse(event.canceledEventData!!.cancellationDateTime,
                    DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DEFAULT))

            binding.tvCancellationDateTime.text = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DISPLAY)
                    .format(cancellationDateTime)
            binding.tvCancellationReason.text = event.canceledEventData!!.cancellationReason
        }

        //TODO: Handle when not available
        binding.tvWhatsAppGroup.text = event.whatsAppLink


    }

    private fun getAndLoadEvent(){
        eventID = intent.getIntExtra("eventID", -1)
        val sharedPreference = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
        val token = sharedPreference.getString(SharedPreferenceManager.instance.TOKEN_KEY,"EMPTY")
        RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token!!)
                .getEvent(eventID).enqueue(object : Callback<Event> {
                    override fun onResponse(call: Call<Event>, response: Response<Event>) {
                        if (response.code()==200) {
                            event = response.body()!!
                            loadEvent()
                            loadMenuItems()
                        }
                        Toast.makeText(applicationContext, "Here", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(call: Call<Event>, t: Throwable) {
                        Log.e("MY",t.message!!)
                    }

                })
    }

    private fun getEventStatus(): String{
        //TODO: Add colors and convert to string resource
        if (isCanceled()){
            return "This event is canceled"
        }
        else if (isFinished()){
            return "This event has finished"
        }
        else if (isRegistrationClosed()){
            return "Registration closed"
        }
        else if (getCurrentSession()!=null){
            return "Session #"+getCurrentSession()!!.id+" is happening now"
        }
        else if (event.status == EventStatus.PENDING.ordinal){
            return "This event is pending and is not visible to the public yet"
        }
        else{
            return "This event is active"
        }
    }


    private fun isCanceled(): Boolean = event.canceledEventData != null


    private fun isRegistrationClosed(): Boolean {
        val registrationCloseDateTime =
                LocalDateTime.parse(event.registrationCloseDateTime,
                        DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DEFAULT))
        return LocalDateTime.now().isAfter(registrationCloseDateTime)
    }

    private fun isFinished(): Boolean {
        val eventEndDate = LocalDate.parse(event.endDate, DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        val eventEndDateTime = eventEndDate.atTime(LocalTime.parse(event.sessions[event.sessions.size-1].endTime,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))

        return LocalDateTime.now().isAfter(eventEndDateTime)
    }

    private fun hasStarted(): Boolean{
        val eventStartDate = LocalDate.parse(event.startDate,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        val eventStartDateTime = eventStartDate.atTime(LocalTime.parse(event.sessions[0].startTime,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))
        return LocalDateTime.now().isAfter(eventStartDateTime)
    }

    private fun isLimitedLocated():Boolean{
        return event.maxParticipants!=-1 && event.locatedEventData!=null
    }

    private fun getCurrentSession(): Session? {
        for(j in 0 until event.sessions.size) {
            val sessionDate = LocalDate.parse(event.sessions[j].date,
                    DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
            val sessionStartDateTime = sessionDate.atTime(LocalTime.parse(event.sessions[j].startTime,
                    DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))
            val sessionEndDateTime = sessionDate.atTime(LocalTime.parse(event.sessions[j].endTime,
                    DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))
            if (LocalDateTime.now().isAfter(sessionStartDateTime) && LocalDateTime.now().isBefore(sessionEndDateTime)) {
                return event.sessions[j]
            }
        }
        return null
    }

    private fun getCurrentLimitedSessionIncludingCheckInTime(): Session?{
        //never use before checking if the event was limited located
        for(j in 0 until event.sessions.size) {
            val sessionDate = LocalDate.parse(event.sessions[j].date,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
            val sessionCheckInDateTime = sessionDate.atTime(LocalTime.parse(event.sessions[j].checkInTime,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))
            val sessionEndDateTime = sessionDate.atTime(LocalTime.parse(event.sessions[j].endTime,
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))
            if (LocalDateTime.now().isAfter(sessionCheckInDateTime) && LocalDateTime.now().isBefore(sessionEndDateTime)) {
                return event.sessions[j]
            }
        }
        return null
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            view_feedback_id -> {
                val intent = Intent(this, ViewEventFeedbackActivity::class.java)
                intent.putExtra("eventID", event.id)
                startActivity(intent)
            }
            email_particiapnts_id -> {
                val intent = Intent(this, EmailParticipantsOfAnEvent::class.java)
                intent.putExtra("eventID", event.id)
                startActivity(intent)
            }
            view_statistics_id -> {
                //TODO: open statistics activity
            }
            cancel_event_id -> {
                promptCancelEvent()
            }
            edit_whole_event_id -> {
                //TODO: open edit whole event activity
            }
            edit_event_id -> {
                //TODO: open edit event activity
            }
            view_participants_id -> {
                if (isLimitedLocated() && getCurrentLimitedSessionIncludingCheckInTime()!=null){
                    //TODO: open view participants when there is a limited located session running
                }
                else{
                    val intent = Intent(this, ViewParticipantsOfAnEventActivity::class.java)
                    intent.putExtra("eventID", event.id)
                    startActivity(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun loadMenuItems(){
        //TODO: string resource
        if (menu == null) return
        if (!isCanceled()){
            if (isFinished()){
                menu!!.add(menu_group_id, view_feedback_id,1, "View feedback")
                menu!!.add(menu_group_id, email_particiapnts_id, 3, "Send email to participants")
                if (isLimitedLocated()){
                    menu!!.add(menu_group_id, view_statistics_id, 6, "View statistics")
                }
            }
            else{
                if (!hasStarted()){
                    menu!!.add(menu_group_id, cancel_event_id, 10, "Cancel event")
                }
                val eventStartDate = LocalDate.parse(event.startDate,
                        DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
                val eventStartDateTime = eventStartDate.atTime(LocalTime.parse(event.sessions[0].startTime,
                        DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))
                if (LocalDateTime.now().isBefore(eventStartDateTime.minusHours(24))){
                    menu!!.add(menu_group_id, edit_event_id, 4, "Edit event")
                }
                else if (event.status == EventStatus.PENDING.ordinal){
                    menu!!.add(menu_group_id, edit_whole_event_id, 4, "Edit event")
                }
            }
            menu!!.add(menu_group_id, view_participants_id,2,"View participants")
        }


    }

    private fun promptCancelEvent(){
        val layout = layoutInflater.inflate(R.layout.cancel_event,binding.root, false)
        val builder = AlertDialog.Builder(this).setTitle("Are you sure you want to cancel the event?")
        var warningEnabled = false
        val eventStartDate = LocalDate.parse(event.startDate,
            DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        val eventStartDateTime = eventStartDate.atTime(LocalTime.parse(event.sessions[0].startTime,
            DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.TIME_DEFAULT)))
        if (LocalDateTime.now().isAfter(eventStartDateTime.minusHours(24))){
            warningEnabled = true
        }
        layout.findViewById<TextView>(R.id.tvCancelWarning).isEnabled = warningEnabled

        val etCancellationReason = layout.findViewById<EditText>(R.id.etCancellationReason)
        builder.setView(layout)
            .setPositiveButton("Confirm") { d: DialogInterface, i: Int ->
                val result = etCancellationReason.text.toString().trim()
                val currentTime = LocalDateTime.now()
                val formattedCurrentTime = DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_TIME_DEFAULT)
                    .format(currentTime)
                val canceledEventData = CanceledEventData(formattedCurrentTime, result)
                cancelEvent(canceledEventData)
            }
            .setNegativeButton("Cancel") { d: DialogInterface, i: Int ->

            }
        val cancelDialog = builder.show()
        val positiveButton = cancelDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.isEnabled = false
        val tlCancellationReason = layout.findViewById<TextInputLayout>(R.id.tlCancellationReason)
        tlCancellationReason.error = getString(R.string.field_cant_be_empty_error)
        etCancellationReason.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (etCancellationReason.text.toString().trim() == ""){
                    tlCancellationReason.error = getString(R.string.field_cant_be_empty_error)
                    positiveButton.isEnabled = false
                }
                else{
                    tlCancellationReason.error = null
                    positiveButton.isEnabled = true
                }
            }

        })
    }

    fun cancelEvent(data: CanceledEventData){
        val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
            .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
        RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token!!)
            .cancelEvent(eventID, data).enqueue(object: Callback<ResponseBody>{
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.code() == 200){
                        finish()
                    }
                    else{
                        Toast.makeText(applicationContext, "Nope", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

            })
    }


}