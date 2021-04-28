package com.eventlocator.eventlocatororganizers.ui

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.databinding.ActivityEmailParticipantsOfAnEventBinding
import com.eventlocator.eventlocatororganizers.retrofit.EventService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.SharedPreferenceManager
import com.eventlocator.eventlocatororganizers.utilities.Utils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmailParticipantsOfAnEvent : AppCompatActivity() {
    lateinit var binding: ActivityEmailParticipantsOfAnEventBinding
    var eventID: Long = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailParticipantsOfAnEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnSendEmail.isEnabled = false
        eventID = intent.getLongExtra("eventID", -1)

        binding.btnSendEmail.setOnClickListener {
            val alertDialog = Utils.instance.createSimpleDialog(this, "Send email", "Are you sure that you want to send an email to the participants of this event?")
            alertDialog.setPositiveButton("Yes"){di: DialogInterface, i: Int ->
                binding.pbLoading.visibility = View.VISIBLE
                binding.btnSendEmail.isEnabled = false
                val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                        .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
                val emailData = ArrayList<String>()
                emailData.add(binding.etTitle.text.toString().trim())
                emailData.add(binding.etContent.text.toString().trim())
                RetrofitServiceFactory.createServiceWithAuthentication(EventService::class.java, token!!)
                        .emailParticipantsOfAnEvent(eventID,emailData).enqueue(object: Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                if (response.code() == 200){
                                    Utils.instance.displayInformationalDialog(this@EmailParticipantsOfAnEvent, "Success",
                                            "Email sent",true)
                                }
                                else if (response.code() == 401){
                                    Utils.instance.displayInformationalDialog(this@EmailParticipantsOfAnEvent, "Error",
                                            "401: Unauthorized access",true)
                                }
                                else if (response.code() == 404){
                                    Utils.instance.displayInformationalDialog(this@EmailParticipantsOfAnEvent, "Error",
                                            "404: No participants found for this event",true)
                                }
                                else if (response.code() == 500){
                                    Utils.instance.displayInformationalDialog(this@EmailParticipantsOfAnEvent,
                                            "Error", "Server issue, please try again later", false)
                                }
                                binding.pbLoading.visibility = View.INVISIBLE
                                binding.btnSendEmail.isEnabled = true
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                Utils.instance.displayInformationalDialog(this@EmailParticipantsOfAnEvent,
                                        "Error", "Can't connect to server", false)
                                binding.pbLoading.visibility = View.INVISIBLE
                                binding.btnSendEmail.isEnabled = true
                            }

                        })
            }
            alertDialog.setNegativeButton("No"){di: DialogInterface, i: Int ->}
            alertDialog.create().show()

        }

        binding.etTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.etTitle.text.toString().trim() == "") {
                    binding.tlTitle.error = getString(R.string.field_cant_be_empty_error)
                } else if (binding.etTitle.text.toString().trim().length <= 64) {
                    binding.tlTitle.error = null
                } else {
                    binding.tlTitle.error = getString(R.string.title_length_error)
                }
                updateSendButton()
            }

        })

        binding.etTitle.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etTitle.setText(binding.etTitle.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateSendButton()
            }
        }

        binding.etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.etContent.text.toString().trim() == "") {
                    binding.tlContent.error = getString(R.string.field_cant_be_empty_error)
                } else if (binding.etContent.text.toString().trim().length <= 65535) {
                    binding.tlContent.error = null
                } else {
                    binding.tlContent.error = getString(R.string.max_number_of_characters_error)
                }
                updateSendButton()
            }

        })

        binding.etContent.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etContent.setText(binding.etContent.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateSendButton()
            }
        }
    }


    fun updateSendButton(){
        binding.btnSendEmail.isEnabled = (binding.etTitle.text.toString()!="" && binding.tlTitle.error == null
                && binding.etContent.text.toString()!="" && binding.tlContent.error == null)
    }
}