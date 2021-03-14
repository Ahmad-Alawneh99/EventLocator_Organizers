package com.eventlocator.eventlocatororganizers.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.databinding.ActivityLoginBinding
import com.eventlocator.eventlocatororganizers.retrofit.OrganizerService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.SharedPreferenceManager
import com.eventlocator.eventlocatororganizers.utilities.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLogin.isEnabled = false
        binding.btnLogin.setOnClickListener {
            val credentials = ArrayList<String>()
            credentials.add(binding.etEmail.text.toString())
            credentials.add(binding.etPassword.text.toString())

            RetrofitServiceFactory.createService(OrganizerService::class.java).login(credentials)
                    .enqueue(object: Callback<String>{
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.code() == 200) { //TODO: Make sure code is correct
                                val sharedPreferenceEditor =
                                        getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE).edit()
                                sharedPreferenceEditor.putString(SharedPreferenceManager.instance.TOKEN_KEY, response.body())
                                sharedPreferenceEditor.apply()
                                startActivity(Intent(applicationContext, ProfileActivity::class.java))
                            }
                            //TODO: Handle other request codes (for incorrect credentials)
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                            //TODO: Display error message
                        }

                    })
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
            if (!hasFocus) {
                binding.etEmail.setText(binding.etEmail.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateLoginButton()
            }
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
            if (!hasFocus) {
                binding.etPassword.setText(binding.etPassword.text.toString().trim(),TextView.BufferType.EDITABLE)
                updateLoginButton()
            }
        }
    }

    fun updateLoginButton(){
        binding.btnLogin.isEnabled = (binding.etEmail.text.toString().trim() != ""
                && binding.tlEmail.error == null
                && binding.etPassword.text.toString().trim() != ""
                && binding.tlPassword.error == null)
    }

}