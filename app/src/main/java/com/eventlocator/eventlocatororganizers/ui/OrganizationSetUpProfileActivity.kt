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
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.data.Organizer
import com.eventlocator.eventlocatororganizers.data.SocialMediaAccount
import com.eventlocator.eventlocatororganizers.databinding.ActivityOrganizationSetUpProfileBinding
import com.eventlocator.eventlocatororganizers.retrofit.OrganizerService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.Utils
import com.google.android.material.textfield.TextInputLayout
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrganizationSetUpProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityOrganizationSetUpProfileBinding
    val INSTANCE_STATE_IMAGE = "Image"
    var image: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrganizationSetUpProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState!=null){
            image = savedInstanceState.getParcelable(INSTANCE_STATE_IMAGE)
            if (image!=null){
                binding.ivLogoPreview.setImageBitmap(Utils.instance.uriToBitmap(image!!,this))
                updateSignUpButton()
            }
        }
        binding.btnSignUp.isEnabled = false
        if (image==null)
            binding.btnRemoveImage.isEnabled = false

        val imageActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val bitmap = Utils.instance.uriToBitmap(result.data?.data!!, this)
                    binding.ivLogoPreview.setImageBitmap(bitmap)
                    binding.btnRemoveImage.isEnabled = true
                    image = result.data!!.data
                    updateSignUpButton()
                }
            }
        }

        binding.btnSignUp.setOnClickListener {
            val bundle = intent.getBundleExtra("data")!!
            val organizerBuilder = Organizer.OrganizerBuilder(
                bundle.getString("name", "name"),
                bundle.getString("email", "email"),
                binding.etAbout.text.toString(),
                bundle.getString("phonenumber", "phonenumber"),
                bundle.getString("password", "password")
            )

            val socialMediaAccounts = ArrayList<SocialMediaAccount>()
            socialMediaAccounts.add(
                SocialMediaAccount(
                    binding.etFacebookName.text.toString(),
                    binding.etFacebookURL.text.toString()
                )
            )
            socialMediaAccounts.add(
                SocialMediaAccount(
                    binding.etYoutubeName.text.toString(),
                    binding.etYoutubeURL.text.toString()
                )
            )
            socialMediaAccounts.add(
                SocialMediaAccount(
                    binding.etInstagramName.text.toString(),
                    binding.etInstagramURL.text.toString()
                )
            )
            socialMediaAccounts.add(
                SocialMediaAccount(
                    binding.etTwitterName.text.toString(),
                    binding.etTwitterURL.text.toString()
                )
            )
            organizerBuilder.setSocialMediaAccounts(socialMediaAccounts)

            val proofImage = bundle.getParcelable<Uri>("proofimage")!!

            var inputStream = contentResolver.openInputStream(proofImage)
            val proofImagePart: RequestBody = RequestBody.create(
                MediaType.parse("image/*"), inputStream?.readBytes()!!
            )
            val proofImageMultipartBody = MultipartBody.Part.createFormData("image","image", proofImagePart)
            var profilePictureMultipartBody: MultipartBody.Part? = null
            inputStream = contentResolver.openInputStream(image!!)
            val profilePicturePart = RequestBody.create(MediaType.parse("image/*"), inputStream?.readBytes()!!)
            profilePictureMultipartBody = MultipartBody.Part.createFormData("image", "image", profilePicturePart)

            val organizer = organizerBuilder.build()

            RetrofitServiceFactory.createService(OrganizerService::class.java).createOrganizer(proofImageMultipartBody,
                profilePictureMultipartBody, organizer)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()
                        //TODO: Handle success (Take to login)
                        //TODO: Check status code
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(applicationContext, t.localizedMessage, Toast.LENGTH_SHORT).show()
                        //TODO: Handle failure (display what's wrong)
                    }

                })
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
                updateSignUpButton()
            }

        })

        binding.etAbout.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)binding.etAbout.setText(
                    Utils.instance.connectWordsIntoString(binding.etAbout.text.toString().trim().split(' ')))
            updateSignUpButton()
        }

        binding.btnUploadLogo.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            imageActivityResult.launch(Intent.createChooser(intent, getString(R.string.select_an_image)))
        }

        binding.btnRemoveImage.setOnClickListener {
            binding.ivLogoPreview.setImageBitmap(null)
            binding.btnRemoveImage.isEnabled = false
            updateSignUpButton()
        }

        binding.etFacebookName.addTextChangedListener(createTextWatcherForAccountNames(binding.etFacebookName,
                binding.tlFacebookName,binding.etFacebookURL))
        binding.etFacebookName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                binding.etFacebookName.setText(binding.etFacebookName.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateSignUpButton()
            }
        }

        binding.etFacebookURL.addTextChangedListener(createTextWatcherForAccountURLs(binding.etFacebookURL, binding.tlFacebookURL,
        binding.tlFacebookName, binding.etFacebookName))

        binding.etFacebookURL.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etFacebookURL.setText(binding.etFacebookURL.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateSignUpButton()
            }
        }

        binding.etYoutubeName.addTextChangedListener(createTextWatcherForAccountNames(binding.etYoutubeName,
                binding.tlYoutubeName, binding.etYoutubeURL))

        binding.etYoutubeName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                binding.etYoutubeName.setText(binding.etYoutubeName.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateSignUpButton()
            }
        }

        binding.etYoutubeURL.addTextChangedListener(createTextWatcherForAccountURLs(binding.etYoutubeURL, binding.tlYoutubeURL,
        binding.tlYoutubeName, binding.etYoutubeName))

        binding.etYoutubeURL.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etYoutubeURL.setText(binding.etYoutubeURL.text.toString().trim(),TextView.BufferType.EDITABLE)
                updateSignUpButton()
            }
        }

        binding.etInstagramName.addTextChangedListener(createTextWatcherForAccountNames(binding.etInstagramName,
                binding.tlInstagramName, binding.etInstagramURL))

        binding.etInstagramName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etInstagramName.setText(binding.etInstagramName.text.toString().trim(),TextView.BufferType.EDITABLE)
                updateSignUpButton()
            }
        }

        binding.etInstagramURL.addTextChangedListener(createTextWatcherForAccountURLs(binding.etInstagramURL, binding.tlInstagramURL,
        binding.tlInstagramName, binding.etInstagramName))

        binding.etInstagramURL.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etInstagramURL.setText(binding.etInstagramURL.text.toString().trim(),TextView.BufferType.EDITABLE)
                updateSignUpButton()
            }
        }

        binding.etTwitterName.addTextChangedListener(createTextWatcherForAccountNames(binding.etTwitterName,
                binding.tlTwitterName,binding.etTwitterURL))

        binding.etTwitterName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etTwitterName.setText(binding.etTwitterName.text.toString().trim(),TextView.BufferType.EDITABLE)
                updateSignUpButton()
            }
        }

        binding.etTwitterURL.addTextChangedListener(createTextWatcherForAccountURLs(binding.etTwitterURL,binding.tlTwitterURL,
        binding.tlTwitterName, binding.etTwitterName))

        binding.etTwitterURL.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etTwitterURL.setText(binding.etTwitterURL.text.toString().trim(),TextView.BufferType.EDITABLE)
                updateSignUpButton()
            }
        }


    }

    fun updateSignUpButton(){
        binding.btnSignUp.isEnabled = (binding.etAbout.text.toString().trim()!="" && binding.tlAbout.error == null
                && binding.tlFacebookName.error == null && binding.tlFacebookURL.error == null
                && binding.tlYoutubeName.error == null && binding.tlYoutubeURL.error == null
                && binding.tlInstagramName.error == null && binding.tlInstagramURL.error == null
                && binding.tlTwitterName.error == null && binding.tlTwitterURL.error == null && image!=null)

//        val btd: BitmapDrawable? = if (binding.ivLogoPreview.drawable is BitmapDrawable)
//            binding.ivLogoPreview.drawable as BitmapDrawable else null
//
//        binding.btnSignUp.isEnabled = binding.btnSignUp.isEnabled  && !(btd == null || btd.bitmap == null)
    }


    fun createTextWatcherForAccountNames(etName: EditText, tl: TextInputLayout, etURL: EditText): TextWatcher{
        return object: TextWatcher{
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
                updateSignUpButton()
            }

        }
    }

    fun createTextWatcherForAccountURLs(etURL: EditText, tl: TextInputLayout, tlName: TextInputLayout, etName: EditText): TextWatcher{
        return object: TextWatcher{
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
                updateSignUpButton()
            }

        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(INSTANCE_STATE_IMAGE, image)
    }

}