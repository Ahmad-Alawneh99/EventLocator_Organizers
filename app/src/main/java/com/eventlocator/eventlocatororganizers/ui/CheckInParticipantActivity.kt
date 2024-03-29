package com.eventlocator.eventlocatororganizers.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.data.Session
import com.eventlocator.eventlocatororganizers.databinding.ActivityCheckInParticipantBinding
import com.eventlocator.eventlocatororganizers.retrofit.EventService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormat
import com.eventlocator.eventlocatororganizers.utilities.DateTimeFormatterFactory
import com.eventlocator.eventlocatororganizers.utilities.SharedPreferenceManager
import com.eventlocator.eventlocatororganizers.utilities.Utils
import com.google.zxing.integration.android.IntentIntegrator
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.Exception
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList

class CheckInParticipantActivity : AppCompatActivity() {
    lateinit var binding: ActivityCheckInParticipantBinding
    var eventID: Long = -1
    lateinit var currentSession: Session
    lateinit var eventName: String
    var scannedParticipantID: Long = -1
    lateinit var participantName: String
    var scannedEventID: Long = -1
    var scannedSessionID = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckInParticipantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getData()

        binding.tvEventName.text = eventName
        val date = LocalDate.parse(currentSession.date, DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DEFAULT))
        binding.tvSessionDateAndDay.text =date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()) +", "+
                DateTimeFormatterFactory.createDateTimeFormatter(DateTimeFormat.DATE_DISPLAY).format(date)

        binding.btnScan.setOnClickListener {
            val integrator = IntentIntegrator(this)
            integrator.setPrompt("Scan a QR code")
            integrator.setCameraId(0)
            integrator.setBeepEnabled(false)
            integrator.setBarcodeImageEnabled(true)
            integrator.initiateScan()
        }

        binding.btnConfirm.isEnabled = false

        binding.btnConfirm.setOnClickListener {
            binding.btnConfirm.isEnabled = false
            binding.btnScan.isEnabled = false
            binding.pbLoading.visibility = View.VISIBLE
            val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                    .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
            RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token!!)
                    .checkInParticipant(scannedEventID, scannedSessionID, scannedParticipantID)
                    .enqueue(object: Callback<ResponseBody>{
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.code() == 200){
                                Utils.instance.displayInformationalDialog(this@CheckInParticipantActivity,
                                        "Success", "Participant checked in successfully", false)
                                binding.btnConfirm.isEnabled = false
                                binding.tvParticipantName.text = this@CheckInParticipantActivity.getString(R.string.scan_a_participant)
                            }
                            else if (response.code() == 500){
                                Utils.instance.displayInformationalDialog(this@CheckInParticipantActivity,
                                        "Error", "Server issue, please try again later", false)
                            }
                            binding.btnConfirm.isEnabled = true
                            binding.btnScan.isEnabled = true
                            binding.pbLoading.visibility = View.INVISIBLE
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            Utils.instance.displayInformationalDialog(this@CheckInParticipantActivity,
                                    "Error", "Can't connect to server", false)
                            binding.btnConfirm.isEnabled = true
                            binding.btnScan.isEnabled = true
                            binding.pbLoading.visibility = View.INVISIBLE
                        }

                    })
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                try{
                    val scannedData = result.contents.split(',')
                    scannedParticipantID = scannedData[0].toLong()
                    scannedEventID = scannedData[1].toLong()
                    scannedSessionID = scannedData[2].toInt()
                    if (scannedEventID != eventID){
                        Utils.instance.displayInformationalDialog(this, "Error", "The current event does not match the scanned QR code", false)
                    }
                    else if (scannedSessionID!= currentSession.id){
                        Utils.instance.displayInformationalDialog(this, "Error", "The current session does not match the scanned QR code", false)
                    }
                    else{
                        binding.btnScan.isEnabled = false
                        binding.pbLoading.visibility = View.VISIBLE
                        val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                                .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
                        RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token!!)
                                .prepareToCheckInParticipant(scannedEventID, scannedSessionID, scannedParticipantID)
                                .enqueue(object: Callback<ArrayList<String>> {
                                    override fun onResponse(call: Call<ArrayList<String>>, response: Response<ArrayList<String>>) {
                                        if (response.code()== 200){
                                            participantName = response.body()!![0]

                                            binding.btnConfirm.isEnabled = true
                                            binding.tvParticipantName.text = participantName

                                        }
                                        else if (response.code() == 404){
                                            Utils.instance.displayInformationalDialog(this@CheckInParticipantActivity,
                                                    "Error", "The participant is not registered in the event", false)
                                        }
                                        else if (response.code() == 409){
                                            Utils.instance.displayInformationalDialog(this@CheckInParticipantActivity,
                                                    "Error", "The participant has already checked in for this session", false)
                                        }
                                        else if (response.code() == 401){
                                            Utils.instance.displayInformationalDialog(this@CheckInParticipantActivity,
                                                    "Error", "401: Unauthorized access", true)
                                        }
                                        else if (response.code() == 500){
                                            Utils.instance.displayInformationalDialog(this@CheckInParticipantActivity,
                                                    "Error", "Server issue, please try again later", false)
                                        }
                                        binding.btnScan.isEnabled = true
                                        binding.pbLoading.visibility = View.INVISIBLE
                                    }

                                    override fun onFailure(call: Call<ArrayList<String>>, t: Throwable) {
                                        Utils.instance.displayInformationalDialog(this@CheckInParticipantActivity,
                                                "Error", t.message!!, false)
                                        binding.btnScan.isEnabled = true
                                        binding.pbLoading.visibility = View.INVISIBLE
                                    }

                                })
                    }
                }
                catch (e: Exception){
                    Utils.instance.displayInformationalDialog(this, "Error", "Invalid QR code", false)
                }
            }

        }
    }

    private fun getData(){
        eventID = intent.getLongExtra("eventID",-1)
        eventName = intent.getStringExtra("eventName")!!
        currentSession = intent.getSerializableExtra("currentSession") as Session

    }
}