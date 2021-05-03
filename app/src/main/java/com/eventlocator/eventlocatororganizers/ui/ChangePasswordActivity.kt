package com.eventlocator.eventlocatororganizers.ui

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.databinding.ActivityChangePasswordBinding
import com.eventlocator.eventlocatororganizers.retrofit.OrganizerService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.SharedPreferenceManager
import com.eventlocator.eventlocatororganizers.utilities.Utils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity() {
    lateinit var binding: ActivityChangePasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnSave.isEnabled = false

        binding.btnSave.setOnClickListener {
            val dialogAlert = Utils.instance.createSimpleDialog(this, "Change password",
                    "Are you sure that you want to save these changes?")

            dialogAlert.setPositiveButton("Yes"){ di: DialogInterface, i: Int ->
                binding.btnSave.isEnabled = false
                binding.pbLoading.visibility = View.VISIBLE

                val data = ArrayList<String>()
                data.add(binding.etCurrentPassword.text.toString().trim())
                data.add(binding.etNewPassword.text.toString().trim())

                val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                        .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")

                RetrofitServiceFactory.createServiceWithAuthentication(OrganizerService::class.java, token!!)
                        .changeOrganizerPassword(data).enqueue(object: Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                if (response.code()==200){
                                    Utils.instance.displayInformationalDialog(this@ChangePasswordActivity, "Success",
                                            "Changes saved",true)
                                }
                                else if (response.code() == 401){
                                    Utils.instance.displayInformationalDialog(this@ChangePasswordActivity, "Error",
                                            "401: Unauthorized access",true)
                                }
                                else if (response.code() == 403){
                                    Utils.instance.displayInformationalDialog(this@ChangePasswordActivity, "Error",
                                            "Your current password is incorrect, please try again",false)
                                }
                                else if (response.code() == 406){
                                    Utils.instance.displayInformationalDialog(this@ChangePasswordActivity, "Error",
                                            "Your new password can't be the same as your old password",false)
                                }
                                else if (response.code()==500){
                                    Utils.instance.displayInformationalDialog(this@ChangePasswordActivity, "Error",
                                            "Server issue, please try again later",false)
                                }
                                binding.btnSave.isEnabled = true
                                binding.pbLoading.visibility = View.INVISIBLE
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                Utils.instance.displayInformationalDialog(this@ChangePasswordActivity, "Error",
                                        "Can't connect to server",false)
                                binding.btnSave.isEnabled = true
                                binding.pbLoading.visibility = View.INVISIBLE
                            }

                        })
            }
            dialogAlert.setNegativeButton("No"){ di: DialogInterface, i: Int ->}
            dialogAlert.create().show()
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