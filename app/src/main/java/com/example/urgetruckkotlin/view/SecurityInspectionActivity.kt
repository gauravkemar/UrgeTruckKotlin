package com.example.urgetruckkotlin.view


import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.urgetruckkotlin.R
import com.example.urgetruckkotlin.databinding.ActivitySecurityInspectionBinding
import com.example.urgetruckkotlin.helper.FileUtil
import com.example.urgetruckkotlin.helper.RFIDHandlerForSecurityInspection
import com.example.urgetruckkotlin.helper.Resource
import com.example.urgetruckkotlin.helper.SessionManager
import com.example.urgetruckkotlin.helper.Utils
import com.example.urgetruckkotlin.model.securityInspection.SecurityCheckModel
import com.example.urgetruckkotlin.model.securityInspection.WBListResultModel
import com.example.urgetruckkotlin.model.securityInspection.WBResponseModel
import com.example.urgetruckkotlin.model.securityInspection.WeightDetailsResultModel
import com.example.urgetruckkotlin.repository.URGETRUCKRepository
import com.example.urgetruckkotlin.viewmodel.WbDetailViewModelFactory
import com.example.urgetruckkotlin.viewmodel.WbDetailsViewModel
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File


class SecurityInspectionActivity : AppCompatActivity(),
    RFIDHandlerForSecurityInspection.ResponseHandlerInterface {
    lateinit var binding: ActivitySecurityInspectionBinding
    private lateinit var session: SessionManager
    private lateinit var viewModel: WbDetailsViewModel

    private val RfidValue = ""
    private var checkstate = true
    private lateinit var weightDetailsResultModel: WeightDetailsResultModel
    private lateinit var wbResponseModel: ArrayList<WBResponseModel>
    private lateinit var wBListResultModel: WBListResultModel
    private var files: MutableList<Uri> = ArrayList()
    private var auto = true
    private var reason = "Accept"

    //rfid
    lateinit var TagDataSet: ArrayList<String>
    private var mediaPlayer: MediaPlayer? = null
    var rfidHandler: RFIDHandlerForSecurityInspection? = null
    var isRFIDInit = false
    var resumeFlag = false
    lateinit var wbId:  MutableList<Int>
    lateinit var wbName: ArrayList<String>
    private val REQUEST_CODE_ASK_PERMISSIONS = 123
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_security_inspection)
        session = SessionManager(this)
        TagDataSet = ArrayList()
        progress = ProgressDialog(this)
        progress.setMessage("Loading...")
        wbId =  ArrayList()
        wbName =  ArrayList()
        binding.scanLayout.autoCompleteTextViewRfid.setText(RfidValue)
        val urgeTruckRepository = URGETRUCKRepository()
        val viewModelProviderFactory =
            WbDetailViewModelFactory(application, urgeTruckRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[WbDetailsViewModel::class.java]

        //inittoolbar
        val layout_toolbar = findViewById<View>(R.id.layout_toolbar)
        val settingBtn = layout_toolbar.findViewById<ImageView>(R.id.ivRightSettings)
        binding.layoutToolbar.ivLeftToolbar.visibility=View.VISIBLE
        binding.layoutToolbar.ivLeftToolbar.setImageResource(R.drawable.ut_logo_with_outline)
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

        getAllWeighBridge()

        binding.scanLayout.rgVehicleDetails.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            if (radioGroup.checkedRadioButtonId == R.id.rbScanRfid) {
                binding.scanLayout.textInputLayoutVehicleno.setVisibility(View.GONE)
                binding.scanLayout.tvVrn.setText("")
                binding.scanLayout.tvRfid.setError("")
                binding.scanLayout.tvRfid.setVisibility(View.VISIBLE)
                checkstate = true
            }
            else if (radioGroup.checkedRadioButtonId == R.id.rbVrn) {
                binding.scanLayout.textInputLayoutVehicleno.setError("")
                binding.scanLayout.textInputLayoutVehicleno.setVisibility(View.VISIBLE)
                binding.scanLayout.autoCompleteTextViewRfid.setText("")
                binding.scanLayout.tvRfid.setVisibility(View.GONE)
                checkstate = false
            }
        })
        populateDropdown()
        binding.securityInspectionLayout.uploadPhotoLayout.ivAddImage.setOnClickListener(View.OnClickListener() { view ->
            addImage()
        })
        binding.btnScanrfid.setOnClickListener(View.OnClickListener() { view ->
            confirmInput()
        })

        binding.securityInspectionLayout.btnPostSecurityCheck.setOnClickListener(View.OnClickListener() { view ->
            confirmInput()
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
                                    binding.securityInspectionLayout.tvOriginalWeight.setText(
                                        weightDetailsResultModel?.weighmentDetails?.expectedWeight
                                    )
                                    binding.securityInspectionLayout.tvNewWeight.setText(
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
        viewModel.getAllWeighBridgeListMutableLiveData.observe(this) { response ->
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
        viewModel.postSecurityCheckMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.clScan.visibility = View.GONE
                    binding.clWeight.visibility = View.VISIBLE
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse != null) {
                                try {
                                    Utils.showCustomDialogFinish(
                                        this,
                                        resultResponse.status
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

        binding.securityInspectionLayout.radioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            if (radioGroup.checkedRadioButtonId == R.id.rbAccept) {
                reason = "Accept"
                auto = true
                binding.securityInspectionLayout.actvWbSelection.setText("")
                binding.securityInspectionLayout.cbWbAllocate.isChecked = true
                binding.securityInspectionLayout.layoutWbSelection.visibility = View.GONE
                checkstate = true
            } else if (radioGroup.checkedRadioButtonId == R.id.rbReprocess) {
                reason = "AcceptWithReweighment"
                binding.securityInspectionLayout.layoutWbSelection.visibility = View.GONE

            } else if (radioGroup.checkedRadioButtonId == R.id.rbReject) {
                reason = "Reject"
                auto = true
                binding.securityInspectionLayout.actvWbSelection.setText("")
                binding.securityInspectionLayout.layoutWbSelection.visibility = View.GONE
            }
            Log.e("reason", reason)
        })
       binding.securityInspectionLayout.cbWbAllocate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.securityInspectionLayout.textInputLayoutWbSelection.visibility = View.INVISIBLE
                binding.securityInspectionLayout.actvWbSelection.setText("")
                auto = true
            } else {
                binding.securityInspectionLayout.textInputLayoutWbSelection.visibility = View.VISIBLE
                auto = false
            }
        }

    }

    private fun getAllWeighBridge() {
      try {
          val baseurl: String = Utils.getSharedPrefs(this, "apiurl").toString()
          viewModel.getAllWeighBridgeList("",baseurl)
      }
      catch (e:Exception)
      {
          Toasty.warning(
              this@SecurityInspectionActivity,
              e.printStackTrace().toString(),
              Toasty.LENGTH_SHORT
          ).show()
      }
    }


    private fun validateRFIDorVRN(): Boolean {
        val scanRFIDInput = binding.scanLayout.tvRfid.editText.toString().trim { it <= ' ' }
        val vrnInput= binding.scanLayout.textInputLayoutVehicleno.editText
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
        if (!auto) {
            if (!validateWb()) {
                return
            }
        }
        if (!validateReason()) {
            return
        }


        requestPermission()
        uploadImages()
    }


    private fun validateReason(): Boolean {
        val reasonInput: String =
            binding.securityInspectionLayout.textInputLayoutReason.editText.toString()
                .trim { it <= ' ' }
        return if (reasonInput.isEmpty()) {
            binding.securityInspectionLayout.textInputLayoutReason.setError("Please Select a reason")
            false
        } else {
            binding.securityInspectionLayout.textInputLayoutReason.setError(null)
            true
        }
    }

    private fun validateWb(): Boolean {
        val wbInput =
            binding.securityInspectionLayout.textInputLayoutWbSelection.editText.toString()
                .trim { it <= ' ' }
        return if (wbInput.isEmpty()) {
            binding.securityInspectionLayout.textInputLayoutWbSelection.setError("Please Select a WeighBridge")
            false
        } else {
            binding.securityInspectionLayout.textInputLayoutWbSelection.setError(null)
            true
        }
    }


    //scan rfid
    private fun confirmInput() {

        if (binding.clScan.visibility== View.VISIBLE)
        {


            if (validateRFIDorVRN()) {
                callgetWeightDetailsApi()
            }
        }else if (binding.clWeight.visibility== View.VISIBLE){

            confirmInputCheck()
        }

    }


    fun callgetWeightDetailsApi() {
        val baseurl: String = Utils.getSharedPrefs(this, "apiurl").toString()
        var edRfid = binding.scanLayout.autoCompleteTextViewRfid.text.toString().trim()
        var edVrm = binding.scanLayout.tvVrn.text.toString().trim()
        try {
            if (checkstate) {
                viewModel.getWeightDetails("", baseurl, 123456789, edRfid, "")
            } else {
                viewModel.getWeightDetails("", baseurl, 123456789, "", edVrm)
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
                    binding.securityInspectionLayout.uploadPhotoLayout.parentLinearLayout.addView(rowView, binding.securityInspectionLayout.uploadPhotoLayout.parentLinearLayout.childCount - 1)
                    binding.securityInspectionLayout.uploadPhotoLayout.parentLinearLayout.isFocusable
                    val selectedImage = rowView.findViewById<ImageView>(R.id.number_edit_text)
                    val img = data.extras?.get("data") as Bitmap
                    selectedImage.setImageBitmap(img)
                    Picasso.get().load(getImageUri(this!!, img)).into(selectedImage)
                    val imgPath = FileUtil.getPath(this!!, getImageUri(this!!, img))
                    files.add(Uri.parse(imgPath))
                    Log.e("image", imgPath)
                }

                1 -> if (resultCode == Activity.RESULT_OK && data != null) {
                    val inflater =
                        this?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val rowView = inflater.inflate(R.layout.image, null)
                    // Add the new row before the add field button.
                    binding.securityInspectionLayout.uploadPhotoLayout.parentLinearLayout.addView(rowView, binding.securityInspectionLayout.uploadPhotoLayout.parentLinearLayout.childCount - 1)
                    binding.securityInspectionLayout.uploadPhotoLayout.parentLinearLayout.isFocusable
                    val selectedImage = rowView.findViewById<ImageView>(R.id.number_edit_text)
                    val img = data.data
                    Picasso.get().load(img).into(selectedImage)
                    val imgPath = FileUtil.getPath(this!!, img!!)
                    files.add(Uri.parse(imgPath))
                    Log.e("image", imgPath)
                }
            }
        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "intuenty", null)
        Log.d("image uri", path ?: "null")
        return if (path != null) Uri.parse(path) else null
    }

    fun addImage() {
        if (files.size > 4) {
            Toast.makeText(this, "Maximum 5 photos can be uploaded", Toast.LENGTH_SHORT).show()
        } else {
            selectImage(this!!)
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
    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this!!, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this!!, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_ASK_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    uploadImages()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    private fun uploadImages() {
        try {
            var list: MutableList<MultipartBody.Part> = ArrayList()
            for (u in files)
            {
                list.add(prepareFilePart("file", u));
            }
            val baseurl: String = Utils.getSharedPrefs(this, "apiurl").toString()

            val modal = if (auto) {
                SecurityCheckModel(
                    "123456789",
                    "",
                    weightDetailsResultModel.weighmentDetails.jobMilestoneId.toString(),
                    weightDetailsResultModel.weighmentDetails.vehicleTransactionId.toString(),
                    weightDetailsResultModel.weighmentDetails.vrn,0,
                    "WEIGHMENT DISTURBANCY",
                    reason,
                )
            } else {
                val weighbridgeId = wbId[wbName.indexOf(binding.securityInspectionLayout.actvWbSelection.text.toString())]
                SecurityCheckModel(
                    "123456789",
                    "",
                    weightDetailsResultModel.weighmentDetails.jobMilestoneId.toString(),
                    weightDetailsResultModel.weighmentDetails.vehicleTransactionId.toString(),
                    weightDetailsResultModel.weighmentDetails.vrn,weighbridgeId,
                    "WEIGHMENT DISTURBANCY",
                    reason,
                )
            }
            val filename = RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                Gson().toJson(modal)
            )
            viewModel.postSecurityCheck("",baseurl,list,filename)
        }
        catch (e:Exception)
        {
            Toasty.warning(
                this@SecurityInspectionActivity,
                 e.printStackTrace().toString(),
                Toasty.LENGTH_SHORT
            ).show()
        }


    }

}