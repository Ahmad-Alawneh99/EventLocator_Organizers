package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.databinding.ActivityOrganizationSetUpProfileBinding
import com.eventlocator.eventlocatororganizers.utilities.Utils

class OrganizationSetUpProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityOrganizationSetUpProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrganizationSetUpProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnRegister.isEnabled = false
        binding.btnRemoveImage.isEnabled = false

        binding.etAbout.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.etAbout.text.toString().trim()==""){
                    binding.tlAbout.error = getString(R.string.field_cant_be_empty_error)
                }
                else if (Utils.instance.countWords(binding.etAbout.text.toString()) !in 50 .. 500){
                    binding.tlAbout.error = getString(R.string.about_limit_error)
                }
                else if (binding.etAbout.text.toString().trim().length>65535){
                    binding.tlAbout.error = getString(R.string.max_number_of_characters_error)
                }
                else{
                    binding.tlAbout.error = null
                }
                updateRegisterButton()
            }

        })

        binding.etAbout.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)binding.etAbout.setText(
                    Utils.instance.connectWordsIntoString(binding.etAbout.text.toString().trim().split(' ')))
        }


    }

    fun updateRegisterButton(){
        binding.btnRegister.isEnabled = true
    }


}