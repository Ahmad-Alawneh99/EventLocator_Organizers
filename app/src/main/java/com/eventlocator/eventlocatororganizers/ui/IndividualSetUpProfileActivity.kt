package com.eventlocator.eventlocatororganizers.ui

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.databinding.ActivityIndividualSetUpProfileBinding
import com.eventlocator.eventlocatororganizers.utilities.Utils
import com.google.android.material.textfield.TextInputLayout

class IndividualSetUpProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityIndividualSetUpProfileBinding
    val IMAGE_REQUEST_CODE = 1
    val INSTANCE_STATE_IMAGE = "Image"
    var image: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIndividualSetUpProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState!=null){
            image = savedInstanceState.getParcelable(INSTANCE_STATE_IMAGE)
            if (image!=null){
                binding.ivProfilePicturePreview.setImageBitmap(Utils.instance.uriToBitmap(image!!,this))
                updateRegisterButton()
            }
        }
        binding.btnRegister.isEnabled = false
        if (image==null)
            binding.btnRemoveImage.isEnabled = false

        binding.btnRegister.setOnClickListener {
            //TODO: Handle registration
        }

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
            updateRegisterButton()
        }

        binding.btnUploadProfilePicture.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_an_image)), IMAGE_REQUEST_CODE)
        }

        binding.btnRemoveImage.setOnClickListener {
            binding.ivProfilePicturePreview.setImageBitmap(null)
            binding.btnRemoveImage.isEnabled = false
            updateRegisterButton()
        }

        binding.etFacebookName.addTextChangedListener(createTextWatcherForAccountNames(binding.etFacebookName,
                binding.tlFacebookName,binding.etFacebookURL))
        binding.etFacebookName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                binding.etFacebookName.setText(binding.etFacebookName.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateRegisterButton()
            }
        }

        binding.etFacebookURL.addTextChangedListener(createTextWatcherForAccountURLs(binding.etFacebookURL, binding.tlFacebookURL,
                binding.tlFacebookName, binding.etFacebookName))

        binding.etFacebookURL.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etFacebookURL.setText(binding.etFacebookURL.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateRegisterButton()
            }
        }

        binding.etYoutubeName.addTextChangedListener(createTextWatcherForAccountNames(binding.etYoutubeName,
                binding.tlYoutubeName, binding.etYoutubeURL))

        binding.etYoutubeName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                binding.etYoutubeName.setText(binding.etYoutubeName.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateRegisterButton()
            }
        }

        binding.etYoutubeURL.addTextChangedListener(createTextWatcherForAccountURLs(binding.etYoutubeURL, binding.tlYoutubeURL,
                binding.tlYoutubeName, binding.etYoutubeName))

        binding.etYoutubeURL.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etYoutubeURL.setText(binding.etYoutubeURL.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateRegisterButton()
            }
        }

        binding.etInstagramName.addTextChangedListener(createTextWatcherForAccountNames(binding.etInstagramName,
                binding.tlInstagramName, binding.etInstagramURL))

        binding.etInstagramName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etInstagramName.setText(binding.etInstagramName.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateRegisterButton()
            }
        }

        binding.etInstagramURL.addTextChangedListener(createTextWatcherForAccountURLs(binding.etInstagramURL, binding.tlInstagramURL,
                binding.tlInstagramName, binding.etInstagramName))

        binding.etInstagramURL.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etInstagramURL.setText(binding.etInstagramURL.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateRegisterButton()
            }
        }

        binding.etTwitterName.addTextChangedListener(createTextWatcherForAccountNames(binding.etTwitterName,
                binding.tlTwitterName,binding.etTwitterURL))

        binding.etTwitterName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etTwitterName.setText(binding.etTwitterName.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateRegisterButton()
            }
        }

        binding.etTwitterURL.addTextChangedListener(createTextWatcherForAccountURLs(binding.etTwitterURL,binding.tlTwitterURL,
                binding.tlTwitterName, binding.etTwitterName))

        binding.etTwitterURL.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etTwitterURL.setText(binding.etTwitterURL.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateRegisterButton()
            }
        }

        binding.etLinkedInName.addTextChangedListener(createTextWatcherForAccountNames(binding.etLinkedInName,
                binding.tlLinkedInName, binding.etLinkedInURL))

        binding.etLinkedInName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etLinkedInName.setText(binding.etLinkedInName.text.toString().trim(),TextView.BufferType.EDITABLE)
                updateRegisterButton()
            }
        }

        binding.etLinkedInURL.addTextChangedListener(createTextWatcherForAccountURLs(binding.etLinkedInURL,binding.tlLinkedInURL,
                binding.tlLinkedInName, binding.etLinkedInName))

        binding.etLinkedInURL.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etLinkedInURL.setText(binding.etLinkedInURL.text.toString().trim(),TextView.BufferType.EDITABLE)
                updateRegisterButton()
            }
        }



    }

    fun updateRegisterButton(){
        binding.btnRegister.isEnabled = (binding.etAbout.text.toString().trim()!="" && binding.tlAbout.error == null
                && binding.tlFacebookName.error == null && binding.tlFacebookURL.error == null
                && binding.tlYoutubeName.error == null && binding.tlYoutubeURL.error == null
                && binding.tlInstagramName.error == null && binding.tlInstagramURL.error == null
                && binding.tlTwitterName.error == null && binding.tlTwitterURL.error == null
                && binding.tlLinkedInName.error == null && binding.tlLinkedInURL.error == null)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode){
            IMAGE_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val bitmap = Utils.instance.uriToBitmap(data?.data!!, this)
                        binding.ivProfilePicturePreview.setImageBitmap(bitmap)
                        binding.btnRemoveImage.isEnabled = true
                        image = data.data
                        updateRegisterButton()
                    }
                }
            }
        }

    }

    fun createTextWatcherForAccountNames(etName: EditText, tl: TextInputLayout, etURL: EditText): TextWatcher {
        return object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (etName.text.toString().trim() == "" && etURL.text.toString().trim()!= ""){
                    tl.error = getString(R.string.social_media_name_error)
                }
                else if(etName.text.toString().trim().length > 32){
                    tl.error = getString(R.string.account_name_max_characters_error)
                }
                else{
                    tl.error = null
                }
                updateRegisterButton()
            }

        }
    }

    fun createTextWatcherForAccountURLs(etURL: EditText, tl: TextInputLayout, tlName: TextInputLayout, etName: EditText): TextWatcher {
        return object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (etURL.text.toString().trim().length>65535){
                    tl.error = getString(R.string.max_number_of_characters_error)
                }
                else if(etURL.text.toString().trim() == ""){
                    tl.error = null
                    if (tlName.error == getString(R.string.social_media_name_error)) tlName.error = null
                }
                else if(!Patterns.WEB_URL.matcher(etURL.text.toString().trim()).matches()){
                    tl.error = getString(R.string.URL_not_valid_error)
                }
                else{
                    tl.error = null
                    if (tlName.error == getString(R.string.social_media_name_error)) tlName.error = null
                }

                if (etURL.text.toString().trim()!="" && etName.text.toString().trim()==""){
                    tlName.error = getString(R.string.social_media_name_error)
                }
                updateRegisterButton()
            }

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(INSTANCE_STATE_IMAGE, image)
    }
}