package com.example.urgetruckkotlin.view

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.urgetruckkotlin.R
import com.example.urgetruckkotlin.adapter.TrackVehicleRecyclerView
import com.example.urgetruckkotlin.databinding.ActivityTrackVehicleBinding
import com.example.urgetruckkotlin.helper.RFIDHandlerForTrackVehical
import com.example.urgetruckkotlin.helper.Resource
import com.example.urgetruckkotlin.helper.SessionManager
import com.example.urgetruckkotlin.helper.Utils
import com.example.urgetruckkotlin.model.securityInspection.WeightDetailsResultModel
import com.example.urgetruckkotlin.model.trackVehical.JobMilestone
import com.example.urgetruckkotlin.model.trackVehical.TrackVehicleModel
import com.example.urgetruckkotlin.repository.URGETRUCKRepository
import com.example.urgetruckkotlin.viewmodel.TrackVehicalDetailsFactory
import com.example.urgetruckkotlin.viewmodel.TrackVehicleDetailsViewModel
import com.example.urgetruckkotlin.viewmodel.WbDetailViewModelFactory
import com.example.urgetruckkotlin.viewmodel.WbDetailsViewModel
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty

class TrackVehicleActivity : AppCompatActivity(),
    RFIDHandlerForTrackVehical.ResponseHandlerInterface {
    lateinit var binding: ActivityTrackVehicleBinding
    lateinit var TagDataSet: ArrayList<String>

    private lateinit var session: SessionManager
    private var checkstate = true
    private lateinit var viewModel: TrackVehicleDetailsViewModel

    //rfid
    private val RfidValue = ""
    private var mediaPlayer: MediaPlayer? = null
    var rfidHandler: RFIDHandlerForTrackVehical? = null
    var isRFIDInit = false
    var resumeFlag = false

    private lateinit var progress: ProgressDialog
    private fun initReader() {
        rfidHandler = RFIDHandlerForTrackVehical()
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_track_vehicle)
        session = SessionManager(this)
        progress = ProgressDialog(this)
        progress.setMessage("Loading...")
        binding.layoutToolbar.toolbarText.setText("Security Inspection")
        mediaPlayer = MediaPlayer.create(this, R.raw.scanner_sound)
        binding.layoutToolbar.ivLogoLeftToolbar.visibility = View.VISIBLE
        binding.layoutToolbar.ivLogoLeftToolbar.setImageResource(R.drawable.ut_logo_with_outline)
        binding.layoutToolbar.ivLogoLeftToolbar.setOnClickListener { view: View? ->
            startActivity(
                Intent(
                    this@TrackVehicleActivity,
                    HomeActivity::class.java
                )
            )
            finishAffinity()
        }
        binding.scanLayout.autoCompleteTextViewRfid.setText(RfidValue)
        TagDataSet = ArrayList()

        val urgeTruckRepository = URGETRUCKRepository()
        val viewModelProviderFactory =
            TrackVehicalDetailsFactory(application, urgeTruckRepository)
        viewModel = ViewModelProvider(
            this,
            viewModelProviderFactory
        )[TrackVehicleDetailsViewModel::class.java]

        TagDataSet = ArrayList<String>()
        defaulReaderOn()
        binding.trackVehicalLayout.rvTrackVehicle.setHasFixedSize(true)

        binding.trackVehicalLayout.rvTrackVehicle.layoutManager = LinearLayoutManager(this)
        binding.btnScanrfid.setOnClickListener(View.OnClickListener() { view ->
            confirmInput()
        })
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
        //APi
        viewModel.getVehicleTrackingDetailsMutableLiveData.observe(this) { response ->
            when (response) {

                is Resource.Success -> {
                    binding.clScan.visibility = View.GONE
                    binding.clDisplay.visibility = View.VISIBLE
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse != null) {
                                try {
                                    binding.trackVehicalLayout.tvvrn.append(
                                        resultResponse.vehicleTransactionDetails.vrn
                                    )
                                    binding.trackVehicalLayout.tvDriverName.append(

                                        resultResponse.vehicleTransactionDetails.driverName

                                    )
                                    binding.trackVehicalLayout.tvtxn.append(
                                        resultResponse.vehicleTransactionDetails.vehicleTransactionCode

                                    )
                                    if (resultResponse.vehicleTransactionDetails.tranType == 1

                                    ) {
                                        binding.trackVehicalLayout.tvtransaction.append("Outbound")
                                    } else if (resultResponse.vehicleTransactionDetails.tranType === 2
                                    ) {
                                        binding.trackVehicalLayout.tvtransaction.append("Inbound")
                                    } else if (
                                        resultResponse.vehicleTransactionDetails.tranType === 3
                                    ) {
                                        binding.trackVehicalLayout.tvtransaction.append("Internal")
                                    }
                                    val milestones: List<JobMilestone> =
                                        resultResponse.vehicleTransactionDetails.jobMilestones


                                    //Collections.reverse(milestones);
                                    //Collections.reverse(milestones);
                                    val adapter = TrackVehicleRecyclerView(
                                        this,
                                        milestones
                                    )
                                    binding.trackVehicalLayout.rvTrackVehicle.adapter=adapter
                                } catch (e: Exception) {
                                    Utils.showCustomDialog(
                                        this@TrackVehicleActivity,
                                        "Exception: No Data Found"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@TrackVehicleActivity,
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
                            this@TrackVehicleActivity   ,
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


    }

    private fun confirmInput() {
        if (binding.clScan.visibility == View.VISIBLE) {
            if (validateRFIDorVRN()) {

                 callgetVehicaltDetailsApi()

            }
        }
    }

    fun callgetVehicaltDetailsApi() {
        val baseurl: String = Utils.getSharedPrefs(this, "apiurl").toString()
        var edRfid = binding.scanLayout.autoCompleteTextViewRfid.text.toString().trim()
        var edVrm = binding.scanLayout.tvVrn.text.toString().trim()
        try {
            if (checkstate) {
                viewModel.getTrackVehicleDetails(
                    "",
                    baseurl,
                    TrackVehicleModel("12345566", edRfid, "")
                )  //Rfid

            } else {
                viewModel.getTrackVehicleDetails(
                    "",
                    baseurl,
                    TrackVehicleModel("12345566", "", edVrm)
                )
            }
            val baseurl: String =
                Utils.getSharedPrefs(this@TrackVehicleActivity, "apiurl").toString()

        } catch (e: Exception) {

        }
    }

    private fun validateRFIDorVRN(): Boolean {
        val scanRFIDInput = binding.scanLayout.tvRfid.editText.toString().trim { it <= ' ' }
        val vrnInput = binding.scanLayout.textInputLayoutVehicleno.editText
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
            Toast.makeText(this@TrackVehicleActivity, status, Toast.LENGTH_SHORT).show()
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
}