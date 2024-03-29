package com.eventlocator.eventlocatororganizers.ui

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.eventlocator.eventlocatororganizers.R
import com.eventlocator.eventlocatororganizers.data.Organizer
import com.eventlocator.eventlocatororganizers.data.SocialMediaAccount
import com.eventlocator.eventlocatororganizers.databinding.ActivityOrganizationEditProfileBinding
import com.eventlocator.eventlocatororganizers.retrofit.OrganizerService
import com.eventlocator.eventlocatororganizers.retrofit.RetrofitServiceFactory
import com.eventlocator.eventlocatororganizers.utilities.SharedPreferenceManager
import com.eventlocator.eventlocatororganizers.utilities.Utils
import com.google.android.material.textfield.TextInputLayout
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayInputStream


class OrganizationEditProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityOrganizationEditProfileBinding
    val INSTANCE_STATE_IMAGE = "Image"
    var image: Uri? = null
    lateinit var organizer: Organizer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrganizationEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAndLoadOrganizerInfo()

        if (savedInstanceState!=null){
            image = savedInstanceState.getParcelable(INSTANCE_STATE_IMAGE)
            if (image!=null){
                binding.ivLogoPreview.setImageBitmap(Utils.instance.uriToBitmap(image!!,this))
                updateSaveButton()
            }
        }

        binding.btnSave.isEnabled = false


        val imageActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val bitmap = Utils.instance.uriToBitmap(result.data?.data!!, this)
                    binding.ivLogoPreview.setImageBitmap(bitmap)
                    image = result.data!!.data
                    updateSaveButton()
                }
            }
        }

        binding.btnSave.setOnClickListener{

            val dialogAlert = Utils.instance.createSimpleDialog(this, "Edit profile",
                    "Are you sure that you want to save these changes?")
            dialogAlert.setPositiveButton("Yes"){di: DialogInterface, i: Int ->
                binding.btnSave.isEnabled = false
                binding.pbLoading.visibility = View.VISIBLE
                val organizerBuilder = Organizer.OrganizerBuilder(
                        "WILL NOT BE USED",
                        "WILL NOT BE USED",
                        binding.etAbout.text.toString(),
                        binding.etPhoneNumber.text.toString(),
                        "WILL NOT BE USED"
                )

                val socialMediaAccounts = ArrayList<SocialMediaAccount>()
                socialMediaAccounts.add(
                        SocialMediaAccount(
                                binding.etFacebookName.text.toString().trim(),
                                binding.etFacebookURL.text.toString().trim()
                        )
                )
                socialMediaAccounts.add(
                        SocialMediaAccount(
                                binding.etYoutubeName.text.toString().trim(),
                                binding.etYoutubeURL.text.toString().trim()
                        )
                )
                socialMediaAccounts.add(
                        SocialMediaAccount(
                                binding.etInstagramName.text.toString().trim(),
                                binding.etInstagramURL.text.toString().trim()
                        )
                )
                socialMediaAccounts.add(
                        SocialMediaAccount(
                                binding.etTwitterName.text.toString().trim(),
                                binding.etTwitterURL.text.toString().trim()
                        )
                )
                organizerBuilder.setSocialMediaAccounts(socialMediaAccounts)
                val organizer = organizerBuilder.build()

                var profilePictureMultipartBody: MultipartBody.Part? = null
                if (image!=null){
                    val inputStream = contentResolver.openInputStream(image!!)
                    val profilePicturePart = RequestBody.create(MediaType.parse("image/*"), inputStream?.readBytes()!!)
                    profilePictureMultipartBody = MultipartBody.Part.createFormData("image", "image", profilePicturePart)
                }


                val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                        .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")
                RetrofitServiceFactory.createServiceWithAuthentication(OrganizerService::class.java, token!!)
                        .editOrganizerProfile(organizer, profilePictureMultipartBody, 0).enqueue(object: Callback<String>{
                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                if (response.code() == 200){
                                    getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                                            .edit().putString(SharedPreferenceManager.instance.TOKEN_KEY, response.body()!!)
                                            .apply()
                                    Utils.instance.displayInformationalDialog(this@OrganizationEditProfileActivity,
                                            "Success",
                                            "Changes saved",true)
                                }
                                else if (response.code() == 401){

                                    Utils.instance.displayInformationalDialog(this@OrganizationEditProfileActivity,
                                            "Error",
                                            "401: Unauthorized access",true)
                                }
                                else if (response.code() == 409){
                                    Utils.instance.displayInformationalDialog(this@OrganizationEditProfileActivity,
                                            "Error",
                                            "The new phone number already exists, please use a different one",false)
                                }
                                else if (response.code() == 500){
                                    Utils.instance.displayInformationalDialog(this@OrganizationEditProfileActivity,
                                            "Error",
                                            "Server issue, please try again later",false)
                                }
                                binding.btnSave.isEnabled = true
                                binding.pbLoading.visibility = View.INVISIBLE
                            }

                            override fun onFailure(call: Call<String>, t: Throwable) {
                                Utils.instance.displayInformationalDialog(this@OrganizationEditProfileActivity,
                                        "Error",
                                        "Can't connect to server",false)
                                binding.btnSave.isEnabled = true
                                binding.pbLoading.visibility = View.INVISIBLE
                            }

                        })
            }

            dialogAlert.setNegativeButton("No"){di: DialogInterface, i: Int ->}
            dialogAlert.create().show()

        }

        binding.btnUpdateEmail.setOnClickListener {
            startActivity(Intent(this, UpdateEmailActivity::class.java))
        }

        binding.btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
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
                else if (binding.etAbout.text.toString().trim().length > 65535){
                    binding.tlAbout.error = getString(R.string.max_number_of_characters_error)
                }
                else{
                    binding.tlAbout.error = null
                }
                updateSaveButton()
            }

        })

        binding.etAbout.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)binding.etAbout.setText(
                    Utils.instance.connectWordsIntoString(binding.etAbout.text.toString().trim().split(' ')))
            updateSaveButton()
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
                updateSaveButton()
            }

        })

        binding.etPhoneNumber.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus) {
                binding.etPhoneNumber.setText(binding.etPhoneNumber.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateSaveButton()
            }
        }

        binding.btnUploadLogo.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            imageActivityResult.launch(Intent.createChooser(intent, getString(R.string.select_an_image)))
        }

        binding.etFacebookName.addTextChangedListener(createTextWatcherForAccountNames(binding.etFacebookName,
                binding.tlFacebookName,binding.etFacebookURL))
        binding.etFacebookName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                binding.etFacebookName.setText(binding.etFacebookName.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateSaveButton()
            }
        }

        binding.etFacebookURL.addTextChangedListener(createTextWatcherForAccountURLs(binding.etFacebookURL, binding.tlFacebookURL,
                binding.tlFacebookName, binding.etFacebookName))

        binding.etFacebookURL.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etFacebookURL.setText(binding.etFacebookURL.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateSaveButton()
            }
        }

        binding.etYoutubeName.addTextChangedListener(createTextWatcherForAccountNames(binding.etYoutubeName,
                binding.tlYoutubeName, binding.etYoutubeURL))

        binding.etYoutubeName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                binding.etYoutubeName.setText(binding.etYoutubeName.text.toString().trim(), TextView.BufferType.EDITABLE)
                updateSaveButton()
            }
        }

        binding.etYoutubeURL.addTextChangedListener(createTextWatcherForAccountURLs(binding.etYoutubeURL, binding.tlYoutubeURL,
                binding.tlYoutubeName, binding.etYoutubeName))

        binding.etYoutubeURL.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etYoutubeURL.setText(binding.etYoutubeURL.text.toString().trim(),TextView.BufferType.EDITABLE)
                updateSaveButton()
            }
        }

        binding.etInstagramName.addTextChangedListener(createTextWatcherForAccountNames(binding.etInstagramName,
                binding.tlInstagramName, binding.etInstagramURL))

        binding.etInstagramName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etInstagramName.setText(binding.etInstagramName.text.toString().trim(),TextView.BufferType.EDITABLE)
                updateSaveButton()
            }
        }

        binding.etInstagramURL.addTextChangedListener(createTextWatcherForAccountURLs(binding.etInstagramURL, binding.tlInstagramURL,
                binding.tlInstagramName, binding.etInstagramName))

        binding.etInstagramURL.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etInstagramURL.setText(binding.etInstagramURL.text.toString().trim(),TextView.BufferType.EDITABLE)
                updateSaveButton()
            }
        }

        binding.etTwitterName.addTextChangedListener(createTextWatcherForAccountNames(binding.etTwitterName,
                binding.tlTwitterName,binding.etTwitterURL))

        binding.etTwitterName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etTwitterName.setText(binding.etTwitterName.text.toString().trim(),TextView.BufferType.EDITABLE)
                updateSaveButton()
            }
        }

        binding.etTwitterURL.addTextChangedListener(createTextWatcherForAccountURLs(binding.etTwitterURL,binding.tlTwitterURL,
                binding.tlTwitterName, binding.etTwitterName))

        binding.etTwitterURL.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                binding.etTwitterURL.setText(binding.etTwitterURL.text.toString().trim(),TextView.BufferType.EDITABLE)
                updateSaveButton()
            }
        }

    }

    fun updateSaveButton(){
        binding.btnSave.isEnabled = (binding.etAbout.text.toString().trim()!="" && binding.tlAbout.error == null
                && binding.tlFacebookName.error == null && binding.tlFacebookURL.error == null
                && binding.tlYoutubeName.error == null && binding.tlYoutubeURL.error == null
                && binding.tlInstagramName.error == null && binding.tlInstagramURL.error == null
                && binding.tlTwitterName.error == null && binding.tlTwitterURL.error == null
                && binding.etPhoneNumber.text.toString().trim()!="" && binding.tlPhoneNumber.error == null)

        val noChanges = (binding.etAbout.text.toString() == organizer.about
                && binding.etPhoneNumber.text.toString() == organizer.phoneNumber
                && binding.etFacebookName.text.toString() == organizer.socialMediaAccounts[0].accountName
                && binding.etFacebookURL.text.toString() == organizer.socialMediaAccounts[0].url
                && binding.etYoutubeName.text.toString() == organizer.socialMediaAccounts[1].accountName
                && binding.etYoutubeURL.text.toString() == organizer.socialMediaAccounts[1].url
                && binding.etInstagramName.text.toString() == organizer.socialMediaAccounts[2].accountName
                && binding.etInstagramURL.text.toString() == organizer.socialMediaAccounts[2].url
                && binding.etTwitterName.text.toString() == organizer.socialMediaAccounts[3].accountName
                && binding.etTwitterURL.text.toString() == organizer.socialMediaAccounts[3].url)
                && image==null

        binding.btnSave.isEnabled = binding.btnSave.isEnabled && !noChanges
    }


    private fun createTextWatcherForAccountNames(etName: EditText, tl: TextInputLayout, etURL: EditText): TextWatcher {
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
                updateSaveButton()
            }

        }
    }

    private fun createTextWatcherForAccountURLs(etURL: EditText, tl: TextInputLayout, tlName: TextInputLayout, etName: EditText): TextWatcher {
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
                updateSaveButton()
            }

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(INSTANCE_STATE_IMAGE, image)
    }

    private fun getAndLoadOrganizerInfo(){
        binding.pbLoading.visibility = View.VISIBLE
        val token = getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE)
                .getString(SharedPreferenceManager.instance.TOKEN_KEY, "EMPTY")

        RetrofitServiceFactory.createServiceWithAuthentication(OrganizerService::class.java, token!!)
                .getOrganizerInfo().enqueue(object: Callback<Organizer> {
                    override fun onResponse(call: Call<Organizer>, response: Response<Organizer>) {
                        if (response.code() == 202) {
                            organizer = response.body()!!
                            binding.etAbout.setText(organizer.about, TextView.BufferType.EDITABLE)

                            binding.etPhoneNumber.setText(organizer.phoneNumber, TextView.BufferType.EDITABLE)

                            binding.etFacebookName.setText(organizer.socialMediaAccounts[0].accountName, TextView.BufferType.EDITABLE)
                            binding.etFacebookURL.setText(organizer.socialMediaAccounts[0].url, TextView.BufferType.EDITABLE)

                            binding.etYoutubeName.setText(organizer.socialMediaAccounts[1].accountName, TextView.BufferType.EDITABLE)
                            binding.etYoutubeURL.setText(organizer.socialMediaAccounts[1].url, TextView.BufferType.EDITABLE)

                            binding.etInstagramName.setText(organizer.socialMediaAccounts[2].accountName, TextView.BufferType.EDITABLE)
                            binding.etInstagramURL.setText(organizer.socialMediaAccounts[2].url, TextView.BufferType.EDITABLE)

                            binding.etTwitterName.setText(organizer.socialMediaAccounts[3].accountName, TextView.BufferType.EDITABLE)
                            binding.etTwitterURL.setText(organizer.socialMediaAccounts[3].url, TextView.BufferType.EDITABLE)

                            binding.ivLogoPreview.setImageBitmap(BitmapFactory.decodeStream(
                                    ByteArrayInputStream(Base64.decode(organizer.image, Base64.DEFAULT))))

                            updateSaveButton()
                        }
                        else if (response.code()== 401){
                            Utils.instance.displayInformationalDialog(this@OrganizationEditProfileActivity, "Error",
                                    "401: Unauthorized access",true)
                            getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE).edit()
                                    .putString(SharedPreferenceManager.instance.TOKEN_KEY, null).apply()
                        }
                        else if (response.code() == 404){
                            Utils.instance.displayInformationalDialog(this@OrganizationEditProfileActivity, "Error",
                                    "404: Organizer not found",true)
                            getSharedPreferences(SharedPreferenceManager.instance.SHARED_PREFERENCE_FILE, MODE_PRIVATE).edit()
                                    .putString(SharedPreferenceManager.instance.TOKEN_KEY, null).apply()
                        }
                        else if (response.code() == 500){
                            Utils.instance.displayInformationalDialog(this@OrganizationEditProfileActivity,
                                    "Error", "Server issue, please try again later", true)
                        }
                        binding.pbLoading.visibility = View.INVISIBLE
                    }

                    override fun onFailure(call: Call<Organizer>, t: Throwable) {
                        Utils.instance.displayInformationalDialog(this@OrganizationEditProfileActivity,
                                "Error", "Can't connect to server", true)
                        binding.pbLoading.visibility = View.INVISIBLE
                    }

                })

    }
}