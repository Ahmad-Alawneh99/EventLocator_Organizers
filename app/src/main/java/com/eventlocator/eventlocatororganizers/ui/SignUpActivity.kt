package com.eventlocator.eventlocatororganizers.ui

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.databinding.ActivitySignUpBinding
import com.eventlocator.eventlocatororganizers.retrofit.OrganizerService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.Utils
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SignUpActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding
    val INSTANCE_STATE_IMAGE = "Image"
    var image: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState!=null){
            image = savedInstanceState.getParcelable(INSTANCE_STATE_IMAGE)
            if (image!=null){
                binding.ivImagePreview.setImageBitmap(Utils.instance.uriToBitmap(image!!,this))
                updateNextButton()
            }
        }
        binding.btnNext.isEnabled = false

        val imageActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val bitmap = Utils.instance.uriToBitmap(result.data?.data!!, this)
                    binding.ivImagePreview.setImageBitmap(bitmap)
                    image = result.data!!.data
                    updateNextButton()
                }
            }
        }


        binding.btnNext.setOnClickListener {
            binding.btnNext.isEnabled = false
            binding.pbLoading.visibility = View.VISIBLE
            val bundle = Bundle()
            bundle.putString("email", binding.etEmail.text.toString())
            bundle.putString("name", binding.etName.text.toString())
            bundle.putString("password", binding.etPassword.text.toString())
            bundle.putString("phonenumber", binding.etPhoneNumber.text.toString())
            bundle.putParcelable("proofimage", image)

            val toBeChecked = ArrayList<String>()
            toBeChecked.add(binding.etEmail.text.toString())
            toBeChecked.add(binding.etName.text.toString())
            toBeChecked.add(binding.etPhoneNumber.text.toString())
            RetrofitServiceFactory.createService(OrganizerService::class.java)
                    .checkIfExists(toBeChecked).enqueue(object: Callback<ArrayList<Int>> {
                        override fun onResponse(call: Call<ArrayList<Int>>, response: Response<ArrayList<Int>>) {
                            if (response.code()==200){
                                val intent = if (binding.rbIndividual.isChecked)
                                    Intent(this@SignUpActivity, IndividualSetUpProfileActivity::class.java)
                                else
                                    Intent(this@SignUpActivity, OrganizationSetUpProfileActivity::class.java)

                                intent.putExtra("data", bundle)
                                startActivity(intent)
                                binding.btnNext.isEnabled = true
                                binding.pbLoading.visibility = View.INVISIBLE
                            }
                            else if (response.code() == 201){
                                val res: ArrayList<Int> = response.body()!!
                                var message = "The following values already exist:\n"
                                if (res.contains(0)) message+="-Email\n"
                                if (res.contains(1)) message+="-Name\n"
                                if(res.contains(2)) message+="-Phone number\n"
                                message += "Please use different values"
                                Utils.instance.displayInformationalDialog(this@SignUpActivity,
                                        "Error", message, false)
                                binding.btnNext.isEnabled = true
                                binding.pbLoading.visibility = View.INVISIBLE
                            }
                            else if (response.code() == 500){
                                Utils.instance.displayInformationalDialog(this@SignUpActivity,
                                        "Error", "Server issue, please try again later", false)
                                binding.btnNext.isEnabled = true
                                binding.pbLoading.visibility = View.INVISIBLE
                            }
                        }

                        override fun onFailure(call: Call<ArrayList<Int>>, t: Throwable) {
                            Utils.instance.displayInformationalDialog(this@SignUpActivity,
                                    "Error", "Can't connect to server", false)
                            binding.btnNext.isEnabled = true
                            binding.pbLoading.visibility = View.INVISIBLE
                        }

                    })
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
                updateNextButton()
            }

        })

        binding.etEmail.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etEmail.setText(binding.etEmail.text.toString().trim(),TextView.BufferType.EDITABLE)
                updateNextButton()
            }
        }

        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.etName.text.toString().trim() == "") {
                    binding.tlName.error = getString(R.string.name_cant_be_empty_error)
                } else if (binding.etName.text.toString().trim().length > 32) {
                    binding.tlName.error = getString(R.string.org_name_max_length_error)
                } else {
                    var hasNumbers = false
                    for (i in binding.etName.text.toString().trim().indices) {
                        if (binding.etName.text.toString().trim()[i] in '0'..'9'){
                            hasNumbers = true
                            break
                        }
                    }
                    if (hasNumbers){
                        binding.tlName.error = getString(R.string.name_contains_numbers_error)
                    }
                    else {
                        binding.tlName.error = null
                    }
                }
                updateNextButton()
            }

        })

        binding.etName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                binding.etName.setText(binding.etName.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateNextButton()
            }
        }

        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.etPhoneNumber.text.toString().trim() == "") {
                    binding.tlPhoneNumber.error = getString(R.string.phone_number_cant_be_empty_error)
                } else if (binding.etPhoneNumber.text.toString().trim().length > 10) {
                    binding.tlPhoneNumber.error = getString(R.string.invalid_phone_error)
                } else {
                    binding.tlPhoneNumber.error = null
                }
                updateNextButton()
            }

        })

        binding.etPhoneNumber.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus) {
                binding.etPhoneNumber.setText(binding.etPhoneNumber.text.toString().trim(),
                    TextView.BufferType.EDITABLE)
                updateNextButton()
            }
        }

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.etPassword.text.toString().trim() == "") {
                    binding.tlPassword.error = getString(R.string.password_cant_be_empty_error)
                } else if (binding.etPassword.text.toString().trim().length !in 8..16) {
                    binding.tlPassword.error = getString(R.string.password_length_error)
                } else {
                    var hasNum = false
                    var hasLetter = false

                    for (i in binding.etPassword.text.toString().trim().indices) {
                        if (!hasNum) {
                            hasNum = binding.etPassword.text.toString().trim()[i] in '0'..'9'
                        }
                        if (!hasLetter) {
                            hasLetter = binding.etPassword.text.toString().trim()[i] in 'a'..'z' ||
                                    binding.etPassword.text.toString().trim()[i] in 'A'..'Z'
                        }
                        if (hasLetter && hasNum) break
                    }

                    if (hasLetter && hasNum) {
                        binding.tlPassword.error = null
                    } else {
                        binding.tlPassword.error = getString(R.string.passwords_contents_error)
                    }
                }

                if (binding.etPassword.text.toString().trim() != binding.etConfirmPassword.text.toString().trim()) {
                    binding.tlConfirmPassword.error = getString(R.string.passwords_dont_match_error)
                } else {
                    binding.tlConfirmPassword.error = null
                }
                updateNextButton()
            }

        })

        binding.etPassword.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                binding.etPassword.setText(binding.etPassword.text.toString().trim(),TextView.BufferType.EDITABLE)
                updateNextButton()
            }
        }

        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.etPassword.text.toString().trim() != binding.etConfirmPassword.text.toString().trim()) {
                    binding.tlConfirmPassword.error = getString(R.string.passwords_dont_match_error)
                } else {
                    binding.tlConfirmPassword.error = null
                }
                updateNextButton()
            }

        })

        binding.etConfirmPassword.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                binding.etConfirmPassword.setText(binding.etConfirmPassword.text.toString().trim(),
                    TextView.BufferType.EDITABLE)
                updateNextButton()
            }
        }

        binding.btnUploadImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            imageActivityResult.launch(Intent.createChooser(intent, getString(R.string.select_an_image)))
        }

    }

    fun updateNextButton(){
        binding.btnNext.isEnabled = (binding.etEmail.text.toString().trim() != "" && binding.tlEmail.error == null
                && binding.etName.text.toString().trim()!= "" && binding.tlName.error == null
                && binding.etPhoneNumber.text.toString().trim()!="" && binding.tlPhoneNumber.error == null
                && binding.etPassword.text.toString().trim() != ""
                && binding.etPassword.text.toString().trim() == binding.etConfirmPassword.text.toString().trim()
                && binding.tlPassword.error == null && image!=null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(INSTANCE_STATE_IMAGE, image)
    }


}