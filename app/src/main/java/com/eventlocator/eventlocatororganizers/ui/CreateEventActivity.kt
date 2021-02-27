package com.eventlocator.eventlocatororganizers.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.core.util.Pair
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.databinding.ActivityCreateEventBinding
import com.eventlocator.eventlocatororganizers.utilities.Utils
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.*
import java.util.*

class CreateEventActivity : AppCompatActivity() {
    //TODO("Not yet implemented")
    lateinit var binding: ActivityCreateEventBinding
    val DATE_PERIOD_LIMIT = 6
    val IMAGE_REQUEST_CODE = 1
    val INSTANCE_STATE_IMAGE = "Image"
    var image: Uri? = null
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
        alterCityAndLocationStatus(true)
        if (image == null)
            binding.btnRemoveImage.isEnabled = false

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
                binding.etEventName.setText(binding.etEventName.text.toString().trim(), TextView.BufferType.EDITABLE)
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
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_an_image)), IMAGE_REQUEST_CODE)
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
            //TODO: Handle effect on sessions
        }

        binding.etNumberOfParticipants.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.etNumberOfParticipants.text.toString().trim().toInt() !in 1 .. 1000) {
                    binding.tlNumberOfParticipants.error = getString(R.string.number_of_participants_limit_error)
                } else {
                    binding.tlNumberOfParticipants.error = null
                }
                updateCreateEventButton()
                //TODO: Handle effect on sessions
            }

        })

        binding.etNumberOfParticipants.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus) {
                binding.etNumberOfParticipants.setText(binding.etNumberOfParticipants.text.toString().trim(),TextView.BufferType.EDITABLE)
                updateCreateEventButton()
            }
        }


        binding.btnSelectDates.setOnClickListener {
            var builder: MaterialDatePicker.Builder<Pair<Long, Long>> = MaterialDatePicker.Builder.dateRangePicker()

            var calendarConstraints = CalendarConstraints.Builder()
            var startConstraint = LocalDate.now()
            var min = startConstraint.plusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            calendarConstraints.setStart(min)
            calendarConstraints.setValidator(DateValidatorPointForward.from(min))
            builder.setCalendarConstraints(calendarConstraints.build())
            builder.setTitleText(getString(R.string.select_start_end_dates))
            var picker = builder.build()
            picker.addOnPositiveButtonClickListener {
                var from: LocalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.first!!), ZoneId.systemDefault()).toLocalDate()
                var to: LocalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.second!!), ZoneId.systemDefault()).toLocalDate()
                if (Duration.between(from.atStartOfDay(), to.atStartOfDay()).toDays() > DATE_PERIOD_LIMIT){
                    AlertDialog.Builder(this)
                            .setTitle(getString(R.string.date_error))
                            .setMessage(getString(R.string.date_period_error_text))
                            .setPositiveButton(getString(R.string.ok)){ dialogInterface: DialogInterface, i: Int -> }
                            .create().show()
                }
                else {
                    binding.tvStartDate.text = from.toString()
                    binding.tvEndDate.text = to.toString()

                }
            }
            picker.show(supportFragmentManager, builder.build().toString())
        }

        //TODO: Handle sessions


    }

    fun updateCreateEventButton(){

    }

    fun updateEventCategoryStatus(){
        binding.tvEventCategoryError.visibility = if (!(binding.cbEducational.isChecked || binding.cbEntertainment.isChecked ||
                binding.cbVolunteering.isChecked || binding.cbSports.isChecked)) View.VISIBLE else View.INVISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode){
            IMAGE_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val bitmap = Utils.instance.uriToBitmap(data?.data!!, this)
                        binding.ivImagePreview.setImageBitmap(bitmap)
                        binding.btnRemoveImage.isEnabled = true
                        image = data.data
                        updateCreateEventButton()
                    }
                }
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(INSTANCE_STATE_IMAGE, image)
    }

    fun alterCityAndLocationStatus(b: Boolean){
        binding.tlCityMenu.isEnabled = !b
        binding.btnSelectLocation.isEnabled = !b
        binding.tvCityAndLocationWarning.visibility = if(b)View.VISIBLE else View.INVISIBLE
    }
}