package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity : AppCompatActivity() {
    lateinit var binding: ActivityChangePasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnSave.isEnabled = false

        binding.btnSave.setOnClickListener {
            //TODO: Handle save
        }

        binding.etCurrentPassword.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.etCurrentPassword.text.toString().trim() == ""){
                    binding.tlCurrentPassword.error = getString(R.string.password_cant_be_empty_error)
                }
                else{
                    binding.tlCurrentPassword.error = null
                }
                updateSaveButton()
            }

        })

        binding.etCurrentPassword.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etCurrentPassword.setText(binding.etCurrentPassword.text.toString().trim(), TextView.BufferType.EDITABLE)
            }
        }

        binding.etNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.etNewPassword.text.toString().trim() == "") {
                    binding.tlNewPassword.error = getString(R.string.password_cant_be_empty_error)
                } else if (binding.etNewPassword.text.toString().trim().length !in 8..16) {
                    binding.tlNewPassword.error = getString(R.string.password_length_error)
                } else {
                    var hasNum = false
                    var hasLetter = false

                    for (i in binding.etNewPassword.text.toString().trim().indices) {
                        if (!hasNum) {
                            hasNum = binding.etNewPassword.text.toString().trim()[i] in '0'..'9'
                        }
                        if (!hasLetter) {
                            hasLetter = binding.etNewPassword.text.toString().trim()[i] in 'a'..'z' ||
                                    binding.etNewPassword.text.toString().trim()[i] in 'A'..'Z'
                        }
                        if (hasLetter && hasNum) break
                    }

                    if (hasLetter && hasNum) {
                        binding.tlNewPassword.error = null
                    } else {
                        binding.tlNewPassword.error = getString(R.string.passwords_contents_error)
                    }
                }

                if (binding.etNewPassword.text.toString().trim() != binding.etConfirmNewPassword.text.toString().trim()) {
                    binding.tlConfirmNewPassword.error = getString(R.string.passwords_dont_match_error)
                } else {
                    binding.tlConfirmNewPassword.error = null
                }
                updateSaveButton()
            }

        })

        binding.etNewPassword.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                binding.etNewPassword.setText(binding.etNewPassword.text.toString().trim(),TextView.BufferType.EDITABLE)
                updateSaveButton()
            }
        }

        binding.etConfirmNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.etNewPassword.text.toString().trim() != binding.etConfirmNewPassword.text.toString().trim()) {
                    binding.tlConfirmNewPassword.error = getString(R.string.passwords_dont_match_error)
                } else {
                    binding.tlConfirmNewPassword.error = null
                }
                updateSaveButton()
            }

        })

        binding.etConfirmNewPassword.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                binding.etConfirmNewPassword.setText(binding.etConfirmNewPassword.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateSaveButton()
            }
        }

    }

    fun updateSaveButton(){
        binding.btnSave.isEnabled = (binding.etCurrentPassword.text.toString().trim() != ""
                && binding.etCurrentPassword.error == null
                && binding.etNewPassword.text.toString().trim() != ""
                && binding.etNewPassword.text.toString().trim() == binding.etConfirmNewPassword.text.toString().trim()
                && binding.tlNewPassword.error == null)
    }


}