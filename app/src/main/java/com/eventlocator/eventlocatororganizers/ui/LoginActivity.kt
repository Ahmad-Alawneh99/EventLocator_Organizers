package com.eventlocator.eventlocatororganizers.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.databinding.ActivityLoginBinding
import com.eventlocator.eventlocatororganizers.utilities.Utils

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLogin.isEnabled = false

        binding.btnLogin.setOnClickListener {
            //TODO: handle log in
        }

        binding.etEmail.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.etEmail.text.toString().trim() == ""){
                    binding.tlEmail.error = getString(R.string.email_cant_be_empty_error)
                }
                else if (Utils.instance.isEmail(binding.etEmail.text.toString().trim())){
                    binding.tlEmail.error = null

                }
                else{
                    binding.tlEmail.error = getString(R.string.invalid_email_error)
                }
                updateLoginButton()
            }

        })

        binding.etEmail.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) binding.etEmail.setText(binding.etEmail.text.toString().trim(), TextView.BufferType.EDITABLE)
        }

        binding.etPassword.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.etPassword.text.toString().trim() == ""){
                    binding.tlPassword.error = getString(R.string.password_cant_be_empty_error)
                }
                else{
                    binding.tlPassword.error = null
                }
                updateLoginButton()
            }

        })

        binding.etPassword.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) binding.etPassword.setText(binding.etPassword.text.toString().trim(),TextView.BufferType.EDITABLE)
        }
    }

    fun updateLoginButton(){
        binding.btnLogin.isEnabled = (binding.etEmail.text.toString().trim() != ""
                && binding.tlEmail.error == null
                && binding.etPassword.text.toString().trim() != ""
                && binding.tlPassword.error == null)
    }
}