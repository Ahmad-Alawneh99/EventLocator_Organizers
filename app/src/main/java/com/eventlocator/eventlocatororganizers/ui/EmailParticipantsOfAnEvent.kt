package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.databinding.ActivityEmailParticipantsOfAnEventBinding
import com.eventlocator.eventlocatororganizers.utilities.SharedPreferenceManager
import com.eventlocator.eventlocatororganizers.utilities.Utils

class EmailParticipantsOfAnEvent : AppCompatActivity() {
    lateinit var binding: ActivityEmailParticipantsOfAnEventBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailParticipantsOfAnEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnSendEmail.isEnabled = false
        binding.btnSendEmail.setOnClickListener {
            //TODO: add confirmation box
            val eventID = intent.getIntExtra("eventID", -1)
            val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")

            //TODO: add call to backend
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