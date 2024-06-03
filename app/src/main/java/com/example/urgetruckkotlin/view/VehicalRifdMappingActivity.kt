package com.example.urgetruckkotlin.view


import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.urgetruckkotlin.R
import com.example.urgetruckkotlin.databinding.ActivityVehicalRifdMappingBinding
import com.example.urgetruckkotlin.helper.RFIDHandlerForVehiclemapping
import com.example.urgetruckkotlin.helper.Resource
import com.example.urgetruckkotlin.helper.SessionManager
import com.example.urgetruckkotlin.helper.Utils
import com.example.urgetruckkotlin.model.vehicalMapping.RfidMappingModel
import com.example.urgetruckkotlin.repository.URGETRUCKRepository
import com.example.urgetruckkotlin.viewmodel.VehicalRifdMappingModel
import com.example.urgetruckkotlin.viewmodel.VehicalRifdMappingModelFactory
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty

class VehicalRifdMappingActivity : AppCompatActivity(),
    RFIDHandlerForVehiclemapping.ResponseHandlerInterface {
    lateinit var binding: ActivityVehicalRifdMappingBinding
    private lateinit var session: SessionManager
    private val RfidValue = ""
    lateinit var TagDataSet: ArrayList<String>
    private lateinit var viewModel: VehicalRifdMappingModel
    lateinit var modal: RfidMappingModel
    //rfid
    private var mediaPlayer: MediaPlayer? = null
    var rfidHandler: RFIDHandlerForVehiclemapping? = null
    var isRFIDInit = false
    var resumeFlag = false

    private lateinit var progress: ProgressDialog
    private fun initReader() {
        rfidHandler = RFIDHandlerForVehiclemapping()
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vehical_rifd_mapping)
        session = SessionManager(this)
        progress = ProgressDialog(this)
        progress.setMessage("Loading...")
        binding.btnVehicleMapping.setText("Verify Tag")
        binding.scanLayout.textInputLayoutVehicleno.visibility = View.GONE
        binding.scanLayout.autoCompleteTextViewRfid.setText(RfidValue)


        TagDataSet = ArrayList()
        defaulReaderOn()
        val urgeTruckRepository = URGETRUCKRepository()
        val viewModelProviderFactory =
            VehicalRifdMappingModelFactory(application, urgeTruckRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[VehicalRifdMappingModel::class.java]
        //
        binding.layoutToolbar.toolbarText.setText("Vehicle RFID Mapping")
        mediaPlayer = MediaPlayer.create(this, R.raw.scanner_sound)
        binding.layoutToolbar.ivLogoLeftToolbar.visibility = View.VISIBLE
        binding.layoutToolbar.ivLogoLeftToolbar.setImageResource(R.drawable.ut_logo_with_outline)
        binding.layoutToolbar.ivLogoLeftToolbar.setOnClickListener { view: View? ->
            startActivity(
                Intent(
                    this@VehicalRifdMappingActivity,
                    HomeActivity::class.java
                )
            )
            finishAffinity()
        }
        binding.btnVehicleMapping.setOnClickListener(View.OnClickListener() {


            if (binding.btnVehicleMapping.getText().equals("Verify Tag")) {
                VerifyTag();
            } else if (binding.btnVehicleMapping.getText().equals("Map")) {
                VerifyTagAndVrn();
            }

        })
        viewModel.rfidMappingMutable.observe(this) { response ->
            when (response) {
                is Resource.Success -> {

                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            if (binding.btnVehicleMapping.text == "Verify Tag") {
                                binding.scanLayout.textInputLayoutVehicleno.visibility =
                                    View.VISIBLE
                                binding.scanLayout.tvVrn.setText(resultResponse.vrn)
                                binding.scanLayout.tvVrn.isFocusable = false
                                binding.scanLayout.autoCompleteTextViewRfid.setText(
                                    binding.scanLayout.autoCompleteTextViewRfid.getText()
                                        .toString()
                                        .trim { it <= ' ' })
                                binding.btnVehicleMapping.visibility = View.GONE
                                Utils.showCustomDialog(
                                    this@VehicalRifdMappingActivity,
                                    resultResponse.statusMessage ?: ""
                                )
                            } else {
                                binding.scanLayout.textInputLayoutVehicleno.visibility =
                                    View.VISIBLE
                                binding.scanLayout.tvVrn.setText(resultResponse.vrn)
                                binding.scanLayout.tvVrn.isFocusable = false
                                binding.scanLayout.autoCompleteTextViewRfid.setText(binding.scanLayout.autoCompleteTextViewRfid.getText()
                                    .toString()
                                    .trim { it <= ' ' })
                                binding.btnVehicleMapping.visibility = View.GONE
                                Utils.showCustomDialogFinish(
                                    this@VehicalRifdMappingActivity,
                                    resultResponse.statusMessage ?: ""
                                )
                            }

                        } catch (e: Exception) {
                            Toasty.warning(
                                this@VehicalRifdMappingActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()

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
                            this,
                            "failed - \nError Message: $errorMessage"
                        ).show()
                        if (errorMessage == "RecordNotFound") {
                            Utils.showCustomDialog(this@VehicalRifdMappingActivity, errorMessage ?: "")
                           binding.scanLayout.textInputLayoutVehicleno .visibility = View.VISIBLE
                            binding.scanLayout.autoCompleteTextViewRfid.setText( binding.scanLayout.autoCompleteTextViewRfid.getText()
                                .toString()
                                .trim { it <= ' ' })
                          binding. btnVehicleMapping.text = "Map"
                        } else if (errorMessage == "Duplicate") {
                            showCustomDialog(this@VehicalRifdMappingActivity, "The vehicle already has a different RFID tag mapped. Do you want to overwrite?")
                            binding.scanLayout.textInputLayoutVehicleno .visibility = View.VISIBLE
                            binding.scanLayout.autoCompleteTextViewRfid.setText( binding.scanLayout.autoCompleteTextViewRfid.getText()
                                .toString()
                                .trim { it <= ' ' })
                            binding.scanLayout.tvRfid.isFocusable = false
                            binding.scanLayout.tvVrn.isFocusable = false
                          binding.btnVehicleMapping.visibility = View.GONE
                        }
                    }


                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
    }
    fun showCustomDialog(context: Context?, message: String?) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton(
                "Cancel"
            ) { dialogInterface, i -> }
            .setNegativeButton(
                "Yes"
            ) { dialogInterface, i -> callPostRfidMapApi(true) }
            .show()
    }
    private fun VerifyTagAndVrn() {
        val scanVRNInput: String =
            binding.scanLayout.textInputLayoutVehicleno.editText.toString().trim()
        if (scanVRNInput.isEmpty()) {
            binding.scanLayout.textInputLayoutVehicleno.setError("Please enter vehicle number")
        } else if (scanVRNInput.length < 8) {
            binding.scanLayout.textInputLayoutVehicleno.setError("Please enter 8 to 10 digits VRN")
        } else {
            callPostRfidMapApi(false)
        }
    }
    fun callPostRfidMapApi(b: Boolean) {

        try {
            if (binding.btnVehicleMapping.getText() == "Verify Tag") {
                modal = RfidMappingModel(
                    "123456",
                    "",
                    binding.scanLayout.autoCompleteTextViewRfid.getText().toString().trim { it <= ' ' },
                    "False"
                )
            } else if (binding.btnVehicleMapping.getText() == "Map" && b) {
                modal = RfidMappingModel(
                    "123456",
                   binding.scanLayout.tvVrn.getText().toString().trim { it <= ' ' },
                    binding.scanLayout.autoCompleteTextViewRfid.getText().toString().trim { it <= ' ' },
                    "False"
                )
            } else if (binding.btnVehicleMapping.getText() == "Map" && b) {
                modal = RfidMappingModel(
                    "123456",
                    binding.scanLayout.tvVrn.getText().toString().trim { it <= ' ' },
                    binding.scanLayout.autoCompleteTextViewRfid.getText().toString().trim { it <= ' ' },
                    "True"
                )
            }
            val baseurl: String =
                Utils.getSharedPrefs(this@VehicalRifdMappingActivity, "apiurl").toString()
            viewModel.rfidMapping("", baseUrl = baseurl, modal)
        } catch (e: Exception) {

        }
    }
    private fun VerifyTag() {
        val scanRFIDInput= binding.scanLayout.tvRfid.editText.toString().trim()

        if (scanRFIDInput.isEmpty()) {
          binding.scanLayout. tvRfid.setError("Press trigger to Scan RFID")
        } else {
            callPostRfidMapApi(false)
        }
        //callPostRfidMapApi();
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
            Toast.makeText(this@VehicalRifdMappingActivity, status, Toast.LENGTH_SHORT).show()
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