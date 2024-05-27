package com.example.urgetruckkotlin.view

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.urgetruckkotlin.R
import com.example.urgetruckkotlin.databinding.ActivityVehicalDetectionBinding
import com.example.urgetruckkotlin.helper.RFIDHandlerForVehicleDetection
import com.example.urgetruckkotlin.helper.Resource
import com.example.urgetruckkotlin.helper.SessionManager
import com.example.urgetruckkotlin.helper.Utils
import com.example.urgetruckkotlin.model.login.vehicalDetection.PostRfidModel
import com.example.urgetruckkotlin.model.login.vehicalDetection.getLocationList.Location
import com.example.urgetruckkotlin.model.login.vehicalDetection.getlocationmasterdatabylocationId.GetLocationMasterDataByLocationId
import com.example.urgetruckkotlin.viewmodel.VehicalDetectionViewModel
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty


class VehicleDetectionActivity : AppCompatActivity(),
    RFIDHandlerForVehicleDetection.ResponseHandlerInterface {
    lateinit var binding: ActivityVehicalDetectionBinding
    private lateinit var viewModel: VehicalDetectionViewModel
    private lateinit var session: SessionManager
    private lateinit var parentLocation: ArrayList<String>
    private lateinit var parentLocationAdapter: ArrayAdapter<String>

    private lateinit var childLocation: ArrayList<String>
    private lateinit var childLocationAdapter: ArrayAdapter<String>

    private lateinit var child2Location: ArrayList<String>
    private lateinit var child2LocationAdapter: ArrayAdapter<String>


    private lateinit var parentLocationMapping: HashMap<String, String>
    private lateinit var childLocationMapping: HashMap<String, String>
    private lateinit var child2LocationMapping: HashMap<String, String>


    lateinit var parentLocationsModel: List<Location>
    lateinit var childLocationModel: List<Location>
    lateinit var child2LocationModel: ArrayList<GetLocationMasterDataByLocationId>
    private val selectedParentLocationId = 0
    private var selectedChildLocationId = 0
    private var selectedChild2LocationId = 0
    private var checkstate = true
    lateinit var modal: PostRfidModel
    lateinit var toolbarText: TextView

    lateinit var TagDataSet: ArrayList<String>

    //rfid
    private var mediaPlayer: MediaPlayer? = null
    var rfidHandler: RFIDHandlerForVehicleDetection? = null
    var isRFIDInit = false
    var resumeFlag = false

    private lateinit var progress: ProgressDialog
    private fun initReader() {
        rfidHandler = RFIDHandlerForVehicleDetection()
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vehical_detection)

        parentLocationMapping = HashMap()
        childLocationMapping = HashMap()
        child2LocationMapping = HashMap()
        parentLocation = ArrayList()
        childLocation = ArrayList()
        child2Location = ArrayList()
        TagDataSet = ArrayList()

        //inittoolbar
        progress = ProgressDialog(this)
        progress.setMessage("Loading...")
        binding.layoutToolbar.toolbarText.setText("Vehicle Detection")
        mediaPlayer = MediaPlayer.create(this, R.raw.scanner_sound)
        binding.layoutToolbar.ivLogoLeftToolbar.visibility = View.VISIBLE
        binding.layoutToolbar.ivLogoLeftToolbar.setImageResource(R.drawable.ut_logo_with_outline)
        binding.layoutToolbar.ivLogoLeftToolbar.setOnClickListener { view: View? ->
            startActivity(
                Intent(
                    this@VehicleDetectionActivity,
                    HomeActivity::class.java
                )
            )
            finishAffinity()
        }
        getParentLocationDefaultData()
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

        viewModel.getVehicleLocationDefaultListMutable.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    parentLocation.clear()
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse != null) {
                                try {
                                    parentLocationsModel = resultResponse.locations

                                    for (location in parentLocationsModel) {
                                        parentLocation.add(location.displayName)
                                        addToParentLocationCoordinatesMap(
                                            location.displayName,
                                            location.locationCode
                                        )
                                    }
                                    populateParentLocationDropdown(parentLocation)

                                } catch (e: Exception) {
                                    Utils.showCustomDialog(
                                        this@VehicleDetectionActivity,
                                        "Exception: No Data Found"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@VehicleDetectionActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    // Utils.showCustomDialogFinish(VehicleDetectionActivity.this,t.toString());
                    binding.textInputLayoutlocation.visibility = View.GONE
                    binding.textInputLayoutChild3.visibility = View.GONE
                    binding.textInputLayoutChild2.visibility = View.GONE
                    /*   try {
                           if (t is SocketTimeoutException) {
                               // Handle timeout exception with custom message
                               Utils.showCustomDialog(
                                   this@VehicleDetectionActivity,
                                   "Network error,\n Please check Network!!"
                               )
                           } else {
                               // Handle other exceptions
                               Utils.showCustomDialog(this@VehicleDetectionActivity, t.toString())
                           }
                       } catch (e: java.lang.Exception) {
                           Utils.showCustomDialog(
                               this@VehicleDetectionActivity,
                               "Exception : No Data Found"
                           )
                       }*/

                    response.message?.let { errorMessage ->
                        Toasty.error(
                            this@VehicleDetectionActivity,
                            "failed - \nError Message: $errorMessage"
                        ).show()
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
        viewModel.getVehicleLocationListMutable.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    childLocation.clear()
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse != null) {
                                findViewById<View>(R.id.progressbar).visibility = View.GONE
                                binding.textInputLayoutChild3.visibility = View.GONE
                                try {

                                     childLocationModel = resultResponse.locations
                                    for (location in childLocationModel) {
                                        childLocation.add(location.displayName)
                                        addToChildLocationCoordinatesMap(
                                            location.displayName,
                                            location.locationId.toString()
                                        )
                                    }

                                    if (childLocation.isNotEmpty()) {
                                        binding.textInputLayoutChild2.visibility = View.VISIBLE
                                        populateChildDropdown(childLocation)
                                    } else {
                                        binding.textInputLayoutChild3.visibility = View.GONE
                                        binding.textInputLayoutChild2.visibility = View.GONE
                                    }
                                } catch (e: Exception) {
                                    Utils.showCustomDialog(
                                        this@VehicleDetectionActivity,
                                        "Exception: No Data Found"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@VehicleDetectionActivity,
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
                            this@VehicleDetectionActivity,
                            "failed - \nError Message: $errorMessage"
                        ).show()
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
        viewModel.getLocationMasterDataByLocationIdMutable.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    child2Location.clear()
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse != null) {
                                try {
                                    child2LocationModel = resultResponse
                                    for (location in child2LocationModel) {
                                        child2Location.add(location.deviceName)
                                        addToChild2LocationCoordinatesMap(
                                            location.deviceLocationMappingId.toString(),
                                            location.deviceName
                                        )
                                    }
                                    if (child2Location.isNotEmpty()) {
                                        binding.textInputLayoutChild3.visibility = View.VISIBLE
                                        populateChild2Dropdown(child2Location)
                                    } else {
                                        binding.textInputLayoutChild3.visibility = View.GONE
                                    }

                                } catch (e: Exception) {
                                    Utils.showCustomDialog(
                                        this@VehicleDetectionActivity,
                                        "Exception: No Data Found"
                                    )
                                }
                            }

                        } catch (e: Exception) {
                            Toasty.warning(
                                this@VehicleDetectionActivity,
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
                            this@VehicleDetectionActivity,
                            "failed - \nError Message: $errorMessage"
                        ).show()
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
        viewModel.postrfIDMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse != null) {
                                try {
                                    setToDefault()
                                    val statusMessage = resultResponse.statusMessage
                                    if (statusMessage != null) {
                                        Utils.showCustomDialogFinish(
                                            this@VehicleDetectionActivity,
                                            statusMessage
                                        )
                                    } else {
                                        Utils.showCustomDialogFinish(
                                            this@VehicleDetectionActivity,
                                            "Success"
                                        )
                                    }

                                } catch (e: Exception) {
                                    Utils.showCustomDialog(
                                        this@VehicleDetectionActivity,
                                        "Exception: No Data Found"
                                    )
                                }
                            }

                        } catch (e: Exception) {
                            Toasty.warning(
                                this@VehicleDetectionActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                is Resource.Error -> {
                    setToDefault()
                    hideProgressBar()

                    response.message?.let { errorMessage ->
                        Toasty.error(
                            this@VehicleDetectionActivity,
                            "failed - \nError Message: $errorMessage"
                        ).show()
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }

        binding.btnVehicledetection.setOnClickListener { view -> confirmInput(view) }
        getParentLocationDefaultData()
        TagDataSet = ArrayList<String>()
        defaulReaderOn()
    }
    private fun setToDefault() {
        getParentLocationDefaultData()
        selectedChild2LocationId = 0
        binding.textInputLayoutChild2.visibility = View.GONE
        binding.textInputLayoutChild3.visibility = View.GONE
        binding.autoCompleteTextViewReason.setText("")
        binding.scanLayout.autoCompleteTextViewRfid.setText("")
        binding.autoCompleteTextViewLocation.setText("")
        TagDataSet.clear()
    }

    fun populateParentLocationDropdown(locationDataArray: MutableList<String>) {
        populateDropdown()
        parentLocationAdapter = ArrayAdapter(
            this,
            R.layout.dropdown_menu_popup_item,
            locationDataArray
        )
        binding.autoCompleteTextViewLocation.setAdapter(parentLocationAdapter)
        binding.autoCompleteTextViewLocation.setOnItemClickListener { adapterView, view, position, id ->
            val selectedItem = binding.autoCompleteTextViewLocation.text.toString()
            val selectedItemPosi = adapterView?.selectedItemPosition
            var selectedKey: String? = null

            for ((key, value) in parentLocationMapping) {
                if (key == selectedItem) {
                    selectedKey = value
                    break
                }
            }
            if (selectedKey != null) {
                getChildLocationDefaultData(selectedKey)
            }
        }
    }

    fun populateDropdown() {
        val adapter1 = ArrayAdapter(
            this,
            R.layout.dropdown_menu_popup_item,
            resources.getStringArray(R.array.vehicle_detectionreasons)
        )

        val editTextFilledExposedDropdown1: AutoCompleteTextView =
            findViewById(R.id.autoCompleteTextView_reason)
        editTextFilledExposedDropdown1.setAdapter(adapter1)
    }

    fun populateChildDropdown(locationDataArray: ArrayList<String>) {
        binding.autoCompleteTextViewLocationChild2.setText("")

        val childLocationAdapter = ArrayAdapter(
            this,
            R.layout.dropdown_menu_popup_item,
            locationDataArray
        )
        binding.autoCompleteTextViewLocationChild2.setAdapter(childLocationAdapter)

        binding.autoCompleteTextViewLocationChild2.setOnItemClickListener { adapterView, view, position, id ->
            val selectedItem = binding.autoCompleteTextViewLocationChild2.text.toString()
            val selectedItemPosi = adapterView?.selectedItemPosition

            val selectedKey = childLocationMapping[selectedItem]
            selectedKey?.let {
                selectedChildLocationId = it.toInt()
                getChild2LocationDefaultData(it.toInt())
            }
        }
    }

    fun populateChild2Dropdown(locationDataArray: ArrayList<String>) {
        binding.autoCompleteTextViewLocationChild3.setText("")

        val child2LocationAdapter = ArrayAdapter(
            this,
            R.layout.dropdown_menu_popup_item,
            locationDataArray
        )

        binding.autoCompleteTextViewLocationChild3.setAdapter(child2LocationAdapter)
        binding.autoCompleteTextViewLocationChild3.setOnItemClickListener { adapterView, _, _, _ ->
            val selectedItem = binding.autoCompleteTextViewLocationChild3.text.toString()
            val selectedKey =
                child2LocationMapping.entries.firstOrNull { it.value == selectedItem }?.key

            selectedKey?.let {
                selectedChild2LocationId = it.toInt()
            }
        }
    }


    fun confirmInput(v: View?) {
        if (!validateReason() || !validateLocation() || !validateRFIDorVRN()) {
            return
        } else {
            callPostRfidApi()
        }
        //Toast.makeText(this, input, Toast.LENGTH_SHORT).show();
    }

    private fun validateReason(): Boolean {
        val reasonInput: String =
            binding.textInputLayoutreasons.editText?.text.toString().trim { it <= ' ' }

        return if (reasonInput.isEmpty()) {
            binding.textInputLayoutreasons.setError("Please Select a reason")
            false
        } else {
            binding.textInputLayoutreasons.setError(null)
            true
        }
    }

    private fun validateLocation(): Boolean {
        return if (selectedChild2LocationId == 0) {
            Toast.makeText(this, "Please Select Location", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    fun callPostRfidApi() {
        binding.progressbar.setVisibility(View.VISIBLE)
        try {
            if (checkstate) {
                modal = PostRfidModel(
                    "123456",
                    binding.scanLayout.tvRfid.editText.toString().trim { it <= ' ' },
                    selectedChild2LocationId.toString(),
                    "",
                    binding.autoCompleteTextViewReason.text.toString().trim()
                )
            } else {
                modal = PostRfidModel(
                    "123456",
                    "",
                    selectedChild2LocationId.toString(),
                    binding.scanLayout.tvRfid.editText.toString().trim { it <= ' ' },
                    binding.autoCompleteTextViewReason.text.toString().trim()
                )
            }
            val baseurl: String =
                Utils.getSharedPrefs(this@VehicleDetectionActivity, "apiurl").toString()
            viewModel.postrfID("", baseUrl = baseurl, modal)
        } catch (e: Exception) {

        }
    }

    fun addToParentLocationCoordinatesMap(key: String?, value: String?) {
        parentLocationMapping[key!!] = value!!
    }

    fun addToChildLocationCoordinatesMap(key: String?, value: String?) {
        childLocationMapping[key!!] = value!!
    }

    fun addToChild2LocationCoordinatesMap(key: String?, value: String?) {
        child2LocationMapping[key!!] = value!!
    }


    private fun validateRFIDorVRN(): Boolean {
        val scanRFIDInput: String = binding.scanLayout.tvRfid.editText.toString().trim { it <= ' ' }
        val vrnInput: String =
            binding.scanLayout.textInputLayoutVehicleno.editText.toString().trim { it <= ' ' }
        if (binding.scanLayout.rbScanRfid.isChecked() && scanRFIDInput.isEmpty()) {
            binding.scanLayout.tvRfid.setError("Press trigger to Scan RFID")
            return false
        } else if (binding.scanLayout.rbVrn.isChecked() && vrnInput.isEmpty()) {
            binding.scanLayout.textInputLayoutVehicleno.setError("Please enter VRN")
            return false
        } else if (binding.scanLayout.rbVrn.isChecked() && vrnInput.length < 8) {
            binding.scanLayout.textInputLayoutVehicleno.setError("Please enter 8 to 10 digits VRN")
        }
        return true
    }

    private fun getParentLocationDefaultData() {
        try {
            val baseurl: String =
                Utils.getSharedPrefs(this@VehicleDetectionActivity, "apiurl").toString()
            viewModel.getVehicleLocationDefaultList("", baseurl, 12321, "null")
        } catch (e: Exception) {
            Toasty.warning(
                this@VehicleDetectionActivity,
                e.printStackTrace().toString(),
                Toasty.LENGTH_SHORT
            ).show()
        }
    }

    private fun getChildLocationDefaultData(parentLocationCode: String) {
        try {
            val baseurl: String =
                Utils.getSharedPrefs(this@VehicleDetectionActivity, "apiurl").toString()
            viewModel.getVehicleLocationList("", baseurl, 12321, parentLocationCode)
        } catch (e: Exception) {
            Toasty.warning(
                this@VehicleDetectionActivity,
                e.printStackTrace().toString(),
                Toasty.LENGTH_SHORT
            ).show()
        }
    }

    private fun getChild2LocationDefaultData(locationId: Int) {
        try {
            val baseurl: String =
                Utils.getSharedPrefs(this@VehicleDetectionActivity, "apiurl").toString()
            viewModel.getLocationMasterDataByLocationId("", baseurl, 12334, locationId)
        } catch (e: Exception) {
            Toasty.warning(
                this@VehicleDetectionActivity,
                e.printStackTrace().toString(),
                Toasty.LENGTH_SHORT
            ).show()
        }
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
            Toast.makeText(this@VehicleDetectionActivity, status, Toast.LENGTH_SHORT).show()
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
            if (tagDataFromScan.startsWith("E200")) {
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
}





