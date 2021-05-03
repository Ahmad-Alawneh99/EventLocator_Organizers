package com.eventlocator.eventlocatororganizers.ui

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.databinding.ActivityUpdateEmailBinding
import com.eventlocator.eventlocatororganizers.retrofit.OrganizerService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.SharedPreferenceManager
import com.eventlocator.eventlocatororganizers.utilities.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateEmailActivity : AppCompatActivity() {
    lateinit var binding: ActivityUpdateEmailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.isEnabled = false

        binding.btnSave.setOnClickListener {
            val dialogAlert = Utils.instance.createSimpleDialog(this, "Update email",
                    "Are you sure that you want to save these changes?")
            dialogAlert.setPositiveButton("Yes"){ di: DialogInterface, i: Int ->

                binding.btnSave.isEnabled = false
                binding.pbLoading.visibility = View.VISIBLE

                val data = ArrayList<String>()
                data.add(binding.etEmail.text.toString().trim())
                data.add(binding.etPassword.text.toString().trim())

                val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                        .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
                RetrofitServiceFactory.createServiceWithAuthentication(OrganizerService::class.java,token!!)
                        .updateOrganizerEmail(data).enqueue(object: Callback<String> {
                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                if (response.code()==200){
                                    getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                                            .edit().putString(SharedPreferenceManager.instance.TOKEN_KEY, response.body()!!)
                                            .apply()
                                    Utils.instance.displayInformationalDialog(this@UpdateEmailActivity, "Success",
                                            "Changes saved",true)
                                }
                                else if (response.code() == 401){
                                    Utils.instance.displayInformationalDialog(this@UpdateEmailActivity, "Error",
                                            "401: Unauthorized access",true)
                                }
                                else if (response.code() == 403){
                                    Utils.instance.displayInformationalDialog(this@UpdateEmailActivity, "Error",
                                            "Incorrect password, please try again",false)
                                }
                                else if (response.code() == 406){
                                    Utils.instance.displayInformationalDialog(this@UpdateEmailActivity, "Error",
                                            "You are already using this email",false)
                                }
                                else if (response.code() == 409){
                                    Utils.instance.displayInformationalDialog(this@UpdateEmailActivity, "Error",
                                            "Email already exists, please use a different email",false)
                                }
                                else if (response.code()==500){
                                    Utils.instance.displayInformationalDialog(this@UpdateEmailActivity, "Error",
                                            "Server issue, please try again later",false)
                                }
                                binding.btnSave.isEnabled = true
                                binding.pbLoading.visibility = View.INVISIBLE
                            }

                            override fun onFailure(call: Call<String>, t: Throwable) {
                                Utils.instance.displayInformationalDialog(this@UpdateEmailActivity, "Error",
                                        "Can't connect to server",false)
                                binding.btnSave.isEnabled = true
                                binding.pbLoading.visibility = View.INVISIBLE
                            }

                        })
            }

            dialogAlert.setNegativeButton("No"){ di: DialogInterface, i: Int ->}
            dialogAlert.create().show()
        }

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.etEmail.text.toString().trim() == "") {
                    binding.tlEmail.error = getString(R.string.email_cant_be_empty_error)
                } else if (Utils.instance.isEmail(binding.etEmail.text.toString().trim())) {
                    binding.tlEmail.error = null
                } else {
                    binding.tlEmail.error = getString(R.string.invalid_email_error)
                }
                updateSaveButton()
            }

        })

        binding.etEmail.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etEmail.setText(binding.etEmail.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateSaveButton()
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
                updateSaveButton()
            }

        })

        binding.etPassword.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) binding.etPassword.setText(binding.etPassword.text.toString().trim(),TextView.BufferType.EDITABLE)
        }
    }

    fun updateSaveButton(){
        binding.btnSave.isEnabled = (binding.etEmail.text.toString().trim() != ""
                && binding.tlEmail.error == null
                && binding.etPassword.text.toString().trim() != ""
                && binding.tlPassword.error == null)
    }
}