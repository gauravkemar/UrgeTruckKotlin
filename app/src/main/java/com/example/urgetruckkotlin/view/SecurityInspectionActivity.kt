package com.example.urgetruckkotlin.view


import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.urgetruckkotlin.R
import com.example.urgetruckkotlin.databinding.ActivitySecurityInspectionBinding
import com.example.urgetruckkotlin.helper.FileUtil
import com.example.urgetruckkotlin.helper.RFIDHandlerForSecurityInspection
import com.example.urgetruckkotlin.helper.Resource
import com.example.urgetruckkotlin.helper.SessionManager
import com.example.urgetruckkotlin.helper.Utils
import com.example.urgetruckkotlin.model.securityInspection.WBListResultModel
import com.example.urgetruckkotlin.model.securityInspection.WBResponseModel
import com.example.urgetruckkotlin.model.securityInspection.WeightDetailsResultModel
import com.example.urgetruckkotlin.viewmodel.WbDetailsViewModel
import com.example.urgetruckkotlin.viewmodel.WbListViewModel
import com.squareup.picasso.Picasso
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class SecurityInspectionActivity : AppCompatActivity(),
    RFIDHandlerForSecurityInspection.ResponseHandlerInterface {
    lateinit var binding: ActivitySecurityInspectionBinding
    private lateinit var session: SessionManager
    private lateinit var viewModel: WbDetailsViewModel
    private lateinit var viewModel1: WbListViewModel
    private var checkstate = true
    private lateinit var weightDetailsResultModel: WeightDetailsResultModel
    private lateinit var wbResponseModel: ArrayList<WBResponseModel>
    private lateinit var wBListResultModel: WBListResultModel
    private val files: List<Uri> = ArrayList()
    private var auto = true
    private var reason = "Accept"

    //rfid
    lateinit var TagDataSet: ArrayList<String>
    private var mediaPlayer: MediaPlayer? = null
    var rfidHandler: RFIDHandlerForSecurityInspection? = null
    var isRFIDInit = false
    var resumeFlag = false

    private lateinit var progress: ProgressDialog
    private fun initReader() {
        rfidHandler = RFIDHandlerForSecurityInspection()
        rfidHandler!!.init(this)
    }

    private fun defaulReaderOn() {
        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            isRFIDInit = true
            Thread.sleep(1000)
            initReader()
        }
    }

    //UPLOAD iMAGE
    val list: MutableList<MultipartBody.Part> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_security_inspection)
        session = SessionManager(this)
        TagDataSet = ArrayList()

        //inittoolbar
        binding.layoutToolbar.toolbarText.setText("Vehicle Detection")
        mediaPlayer = MediaPlayer.create(this, R.raw.scanner_sound)
        binding.layoutToolbar.ivLogoLeftToolbar.visibility = View.VISIBLE
        binding.layoutToolbar.ivLogoLeftToolbar.setImageResource(R.drawable.ut_logo_with_outline)
        binding.layoutToolbar.ivLogoLeftToolbar.setOnClickListener { view: View? ->
            startActivity(
                Intent(
                    this@SecurityInspectionActivity,
                    HomeActivity::class.java
                )
            )
            finishAffinity()
        }
        TagDataSet = ArrayList<String>()
        defaulReaderOn()

        binding.scanLayout.rgVehicleDetails.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            if (radioGroup.checkedRadioButtonId == R.id.rbScanRfid) {
                binding.scanLayout.textInputLayoutVehicleno.setVisibility(View.GONE)
                binding.scanLayout.tvVrn.setText("")
                binding.scanLayout.tvRfid.setError("")
                binding.scanLayout.tvRfid.setVisibility(View.VISIBLE)
                checkstate = true
            } else if (radioGroup.checkedRadioButtonId == R.id.rbVrn) {
                binding.scanLayout.textInputLayoutVehicleno.setError("")
                binding.scanLayout.textInputLayoutVehicleno.setVisibility(View.VISIBLE)
                binding.scanLayout.autoCompleteTextViewRfid.setText("")
                binding.scanLayout.tvRfid.setVisibility(View.GONE)
                checkstate = false
            }
        })
        populateDropdown()
        binding.SecurityInspectionLayout.uploadPhotoLayout.ivAddImage.setOnClickListener(View.OnClickListener() { view ->
            addImage()
        })
        binding.btnScanrfid.setOnClickListener(View.OnClickListener() { view ->
            confirmInput()
        })
        binding.SecurityInspectionLayout.btnPostSecurityCheck.setOnClickListener(View.OnClickListener() { view ->
            confirmInputCheck()
        })

        viewModel.getWeightDetailsMutableLiveData.observe(this) { response ->
            when (response) {

                is Resource.Success -> {
                    binding.clScan.visibility = View.GONE
                    binding.clWeight.visibility = View.VISIBLE
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse != null) {
                                try {

                                    weightDetailsResultModel = resultResponse
                                    binding.scanLayout.tvVrn.setText(weightDetailsResultModel?.weighmentDetails?.vrn)
                                    binding.SecurityInspectionLayout.tvOriginalWeight.setText(
                                        weightDetailsResultModel?.weighmentDetails?.expectedWeight
                                    )
                                    binding.SecurityInspectionLayout.tvNewWeight.setText(
                                        weightDetailsResultModel?.weighmentDetails?.actualWeight
                                    )


                                } catch (e: Exception) {
                                    Utils.showCustomDialog(
                                        this@SecurityInspectionActivity,
                                        "Exception: No Data Found"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@SecurityInspectionActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()


                    response.message?.let { errorMessage ->
                        Toasty.error(
                            this@SecurityInspectionActivity,
                            "failed - \nError Message: $errorMessage"
                        ).show()
                        Utils.showCustomDialogFinish(this, errorMessage)
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
        viewModel1.getAllWeighBridgeListMutableLiveData.observe(this) { response ->
            when (response) {

                is Resource.Success -> {
                    binding.clScan.visibility = View.GONE
                    binding.clWeight.visibility = View.VISIBLE
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse != null) {
                                try {

                                    wbResponseModel = resultResponse

                                    val wbId = ArrayList<String>()
                                    val wbName = ArrayList<String>()

                                    for (item in wbResponseModel) {
                                        wbId.add(item.weighBridgeId)
                                        wbName.add(item.weighBridgeName)
                                    }

                                    populateWbDropdown(wbName)


                                } catch (e: Exception) {
                                    Utils.showCustomDialog(
                                        this@SecurityInspectionActivity,
                                        "Exception: No Data Found"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@SecurityInspectionActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()


                    response.message?.let { errorMessage ->
                        Toasty.error(
                            this@SecurityInspectionActivity,
                            "failed - \nError Message: $errorMessage"
                        ).show()
                        Utils.showCustomDialogFinish(this, errorMessage)
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
        binding.SecurityInspectionLayout.radioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            if (radioGroup.checkedRadioButtonId == R.id.rbAccept) {
                reason = "Accept"
                auto = true
                binding.SecurityInspectionLayout.actvWbSelection.setText("")
                binding.SecurityInspectionLayout.cbWbAllocate.isChecked = true
                binding.SecurityInspectionLayout.layoutWbSelection.visibility = View.GONE
                checkstate = true
            } else if (radioGroup.checkedRadioButtonId == R.id.rbReprocess) {
                reason = "AcceptWithReweighment"
                binding.SecurityInspectionLayout.layoutWbSelection.visibility = View.GONE

            } else if (radioGroup.checkedRadioButtonId == R.id.rbReject) {
                reason = "Reject"
                auto = true
                binding.SecurityInspectionLayout.actvWbSelection.setText("")
                binding.SecurityInspectionLayout.layoutWbSelection.visibility = View.GONE


            }
            Log.e("reason", reason)
        })

    }


    private fun validateRFIDorVRN(): Boolean {
        val scanRFIDInput: String = binding.scanLayout.tvRfid.editText.toString().trim { it <= ' ' }
        val vrnInput: String = binding.scanLayout.textInputLayoutVehicleno.editText
            .toString().trim { it <= ' ' }
        if (binding.scanLayout.rbScanRfid.isChecked() && scanRFIDInput.isEmpty()) {
            binding.scanLayout.tvRfid.setError("Press trigger to Scan RFID")
            return false
        } else if (binding.scanLayout.rbVrn.isChecked() && vrnInput.isEmpty()) {
            binding.scanLayout.textInputLayoutVehicleno.setError("Please enter VRN")
            return false
        } else if (binding.scanLayout.rbVrn.isChecked() && vrnInput.length < 8) {
            binding.scanLayout.textInputLayoutVehicleno.setError("Please enter 8 to 10 digits VRN")
            return false
        }
        return true
    }

    fun populateDropdown() {
        val reasons = arrayOf("WB Not working", "Vehicle Defect", "Other")

        val adapter = ArrayAdapter(
            this@SecurityInspectionActivity,
            R.layout.dropdown_menu_popup_item,
            reasons
        )

        val autoCompleteTextView =
            findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewrsecurityreason)
        autoCompleteTextView.setAdapter(adapter)
    }

    fun populateWbDropdown(wbName: ArrayList<String>) {
        val adapter1 = ArrayAdapter(
            this@SecurityInspectionActivity,
            R.layout.dropdown_menu_popup_item,
            wbName
        )
        val editTextFilledExposedDropdown1: AutoCompleteTextView =
            findViewById(R.id.actvWbSelection)
        editTextFilledExposedDropdown1.setAdapter(adapter1)

    }

    fun confirmInputCheck() {
        if (!validateReason() || !validateWb()) {
            return
        }


        // callPostSecurityCheck(v);
//        requestPermission()
//        uploadImages()
    }

//    private fun uploadImages() {
//        val list = ArrayList<MultipartBody.Part>()
//
//        for (uri in files) {
//            Log.i("uris", uri.path)
//            list.add(prepareFilePart("file", uri))
//        }
//
//    }


    private fun validateReason(): Boolean {
        val reasonInput: String =
            binding.SecurityInspectionLayout.textInputLayoutReason.editText.toString()
                .trim { it <= ' ' }
        return if (reasonInput.isEmpty()) {
            binding.SecurityInspectionLayout.textInputLayoutReason.setError("Please Select a reason")
            false
        } else {
            binding.SecurityInspectionLayout.textInputLayoutReason.setError(null)
            true
        }
    }

    private fun validateWb(): Boolean {
        val wbInput =
            binding.SecurityInspectionLayout.textInputLayoutWbSelection.editText.toString()
                .trim { it <= ' ' }
        return if (wbInput.isEmpty()) {
            binding.SecurityInspectionLayout.textInputLayoutWbSelection.setError("Please Select a WeighBridge")
            false
        } else {
            binding.SecurityInspectionLayout.textInputLayoutWbSelection.setError(null)
            true
        }
    }


    //scan rfid
    private fun confirmInput() {
        if (validateRFIDorVRN()) {
            return
        } else {
            callgetWeightDetailsApi()
        }
    }


    fun callgetWeightDetailsApi() {
        val baseurl: String = Utils.getSharedPrefs(this, "apiurl").toString()
        var edRfid = binding.scanLayout.autoCompleteTextViewRfid.toString().trim()
        var edVrm = binding.scanLayout.tvVrn.toString().trim()
        try {
            if (checkstate) {
                viewModel.getWeightDetails("", baseurl, 123456789, edRfid, "")

            } else {
                viewModel.getWeightDetails("", baseurl, 123456789, edVrm, "")
            }
            val baseurl: String =
                Utils.getSharedPrefs(this@SecurityInspectionActivity, "apiurl").toString()

        } catch (e: Exception) {

        }
    }

    private fun setToDefault() {
        TagDataSet.clear()
    }

    ////rfid handle
    override fun onResume() {
        super.onResume()

        if (resumeFlag) {
            resumeFlag = false
            if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()

        if (isRFIDInit) {
            rfidHandler!!.onDestroy()
        }
        if (mediaPlayer != null) {
            mediaPlayer?.release()
        }

    }

    override fun onPostResume() {
        super.onPostResume()
        if (isRFIDInit) {
            val status = rfidHandler!!.onResume()
            Toast.makeText(this@SecurityInspectionActivity, status, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onPause() {
        super.onPause()
        if (isRFIDInit) {
            rfidHandler!!.onPause()
        }
        resumeFlag = true
    }

    fun performInventory() {
        rfidHandler!!.performInventory()
    }

    fun stopInventory() {
        rfidHandler!!.stopInventory()
    }


    override fun handleTriggerPress(pressed: Boolean) {
        if (pressed) {
            performInventory()
        } else stopInventory()
    }

    override fun handleTagdata(tagData: Array<TagData>) {
        val sb = StringBuilder()
        sb.append(tagData[0].tagID)
        runOnUiThread {
            var tagDataFromScan = tagData[0].tagID
            mediaPlayer?.start()

            //binding.tvBarcode.setText(tagDataFromScan)
            //Log.e(TAG, "RFID Data : $tagDataFromScan")
            binding.scanLayout.autoCompleteTextViewRfid.setText(tagData[0].tagID.toString())
            stopInventory()

            if (!TagDataSet?.contains(tagDataFromScan)!!)
                TagDataSet.add(tagDataFromScan)
            val adapter1: ArrayAdapter<String> = ArrayAdapter<String>(
                this,
                R.layout.dropdown_menu_popup_item,
                TagDataSet
            )
            runOnUiThread {
                if (TagDataSet.size == 1) {
                    binding.scanLayout.autoCompleteTextViewRfid.setText(
                        adapter1.getItem(0).toString(),
                        false
                    )
                } else {
                    binding.scanLayout.autoCompleteTextViewRfid.setText("")
                    binding.scanLayout.tvRfid.setError("Select the RFID value from dropdown")
                }
                binding.scanLayout.autoCompleteTextViewRfid.setAdapter<ArrayAdapter<String>>(
                    adapter1
                )
            }

        }
    }

    private fun showNonCancelablePopup() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Please Update Application!!.")
        // Set the dialog as non-cancelable
        builder.setCancelable(false)
        val alertDialog = builder.create()
        // Show the dialog
        alertDialog.show()
    }

    private fun showProgressBar() {
        progress.show()
    }

    private fun hideProgressBar() {
        progress.cancel()
    }

    //upload image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == Activity.RESULT_OK && data != null) {
                    val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val rowView = inflater.inflate(R.layout.image, null)
                    // Add the new row before the add field button.
                    parentLinearLayout.addView(rowView, parentLinearLayout.childCount - 1)
                    parentLinearLayout.isFocusable
                    val selectedImage = rowView.findViewById<ImageView>(R.id.number_edit_text)
                    val img = data.extras?.get("data") as Bitmap
                    selectedImage.setImageBitmap(img)
                    Picasso.get().load(getImageUri(activity!!, img)).into(selectedImage)
                    val imgPath = FileUtil.getPath(activity!!, getImageUri(activity!!, img))
                    files.add(Uri.parse(imgPath))
                    Log.e("image", imgPath)
                }

                1 -> if (resultCode == Activity.RESULT_OK && data != null) {
                    val inflater =
                        activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val rowView = inflater.inflate(R.layout.image, null)
                    // Add the new row before the add field button.
                    parentLinearLayout.addView(rowView, parentLinearLayout.childCount - 1)
                    parentLinearLayout.isFocusable
                    val selectedImage = rowView.findViewById<ImageView>(R.id.number_edit_text)
                    val img = data.data
                    Picasso.get().load(img).into(selectedImage)
                    val imgPath = FileUtil.getPath(activity!!, img!!)
                    files.add(Uri.parse(imgPath))
                    Log.e("image", imgPath)
                }
            }
        }
    }

    fun addImage() {
        if (files.size > 4) {
            Toast.makeText(activity, "Maximum 5 photos can be uploaded", Toast.LENGTH_SHORT).show()
        } else {
            selectImage(activity!!)
        }
    }

    private fun selectImage(context: Context) {
        val options =
            if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
                arrayOf("Choose from Gallery", "Cancel")
            } else {
                arrayOf("Take Photo", "Choose from Gallery", "Cancel")
            }

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setTitle("Choose a Media")

        builder.setItems(options) { dialog, item ->
            when (options[item]) {
                "Take Photo" -> {
                    val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(takePicture, 0)
                }

                "Choose from Gallery" -> {
                    val getIntent = Intent(Intent.ACTION_GET_CONTENT)
                    getIntent.type = "image/*"
                    startActivityForResult(getIntent, 1)
                }

                "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }


    fun prepareFilePart(partName: String, fileUri: Uri): MultipartBody.Part {
        val file = File(fileUri.path)
        Log.i("here is error", file.absolutePath)
        // create RequestBody instance from file
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

}