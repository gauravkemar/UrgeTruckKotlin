package com.example.urgetruckkotlin.view

import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.urgetruckkotlin.R
import com.example.urgetruckkotlin.RFIDHandlerForDispatch
import com.example.urgetruckkotlin.databinding.ActivityVehicalDetectionBinding
import com.example.urgetruckkotlin.helper.SessionManager
import com.example.urgetruckkotlin.helper.Utils
import com.example.urgetruckkotlin.model.login.vehicalDetection.PostRfidModel
import com.example.urgetruckkotlin.model.login.vehicalDetection.getLocationList.Location
import com.example.urgetruckkotlin.model.login.vehicalDetection.getlocationmasterdatabylocationId.GetLocationMasterDataByLocationId
import com.example.urgetruckkotlin.viewmodel.VehicalDetectionModel
import com.zebra.rfid.api3.TagData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException


class VehicleDetectionActivity : AppCompatActivity(),
    RFIDHandlerForDispatch.ResponseHandlerInterface {
    lateinit var binding: ActivityVehicalDetectionBinding
    private lateinit var viewModel: VehicalDetectionModel
    private lateinit var progress: ProgressDialog
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
    private val selectedChildLocationId = 0
    private val selectedChild2LocationId = 0
    private var checkstate = true
    lateinit var modal: PostRfidModel
    lateinit var toolbarText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vehical_detection)

        parentLocationMapping = java.util.HashMap()
        childLocationMapping = java.util.HashMap()
        child2LocationMapping = java.util.HashMap()

        parentLocation = java.util.ArrayList()
        childLocation = java.util.ArrayList()
        child2Location = java.util.ArrayList()


        //inittoolbar
        binding.layoutToolbar.toolbarText.setText("Vehicle Detection")
        val mediaPlayer = MediaPlayer.create(this, R.raw.scanner_sound)
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

        binding.btnVehicledetection.setOnClickListener { view -> confirmInput(view) }

        getParentLocationDefaultData()
        TagDataSet = java.util.ArrayList<String>()
        connectReader()

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
        } catch (e: Exception) {

        }
    }



    private fun validateRFIDorVRN(): Boolean {
        val scanRFIDInput: String = binding.scanLayout.tvRfid.editText.toString().trim { it <= ' ' }
        val vrnInput: String =
            binding.scanLayout.textInputLayoutVehicleno.editText.toString().trim { it <= ' ' }
        if (binding.scanLayout.rbScanRfid.isChecked() && scanRFIDInput.isEmpty()) {
            binding.scanLayout.tvRfid.setError("Press trigger to Scan RFID")
            return false
        } else if ( binding.scanLayout.rbVrn.isChecked() && vrnInput.isEmpty()) {
            binding.scanLayout.textInputLayoutVehicleno.setError("Please enter VRN")
            return false
        } else if ( binding.scanLayout.rbVrn.isChecked() && vrnInput.length < 8) {
            binding.scanLayout.textInputLayoutVehicleno.setError("Please enter 8 to 10 digits VRN")
        }
        return true
    }

    override fun handleTriggerPress(pressed: Boolean) {
        if (pressed) {
            performInventory()
        } else stopInventory()
    }

    private fun getParentLocationDefaultData() {
        try {
            if (Utils.isConnected(this)) {
                findViewById<View>(R.id.progressbar).visibility = View.VISIBLE
                val baseurl: String =
                    Utils.getSharedPreferences(this@VehicleDetectionNewActivity, "apiurl")
                val apiService: ApiInterface =
                    APiClient.getClient(baseurl).create(ApiInterface::class.java)
                val call: Call<GetLocationListResponse> =
                    apiService.getVehicleLocationDefaultList(123456789, "")
                call.enqueue(object :
                    Callback<GetLocationListResponse?> {
                    override fun onResponse(
                        call: Call<GetLocationListResponse?>,
                        response: Response<GetLocationListResponse?>
                    ) {
                        findViewById<View>(R.id.progressbar).visibility = View.GONE
                        val locationModel: GetLocationListResponse? = response.body()
                        parentLocationsModel = locationModel.getLocations()
                        for (location in parentLocationsModel) {
                            parentLocation.add(location.getDisplayName())
                            addToParentLocationCoordinatesMap(
                                location.getDisplayName(),
                                location.getLocationCode()
                            )
                        }
                        populateParentLocationDropdown(parentLocation)
                    }

                    override fun onFailure(call: Call<GetLocationListResponse?>, t: Throwable) {
                        Log.d("TAG", "Response = $t")
                        findViewById<View>(R.id.progressbar).visibility = View.GONE
                        // Utils.showCustomDialogFinish(VehicleDetectionActivity.this,t.toString());
                        if (t is SocketTimeoutException) {
                            // Handle timeout exception with custom message
                            Utils.showCustomDialog(
                                this@VehicleDetectionNewActivity,
                                "Network error,\n Please check Network!!"
                            )
                        } else {
                            // Handle other exceptions
                            Utils.showCustomDialog(this@VehicleDetectionNewActivity, t.toString())
                        }
                    }
                })
            } else {
                Utils.showCustomDialogFinish(this, getString(R.string.internet_connection))
            }
        } catch (e: Exception) {
            Utils.showCustomDialogFinish(this, e.message)
        }
    }
}





