package com.example.urgetruckkotlin.view

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.urgetruckkotlin.R
import com.example.urgetruckkotlin.databinding.ActivityExitClearanceNewBinding
import com.example.urgetruckkotlin.databinding.ActivityLoginBinding
import com.example.urgetruckkotlin.helper.RFIDHandlerForExiteClearance
import com.example.urgetruckkotlin.helper.RFIDHandlerForVehicleDetection
import com.example.urgetruckkotlin.helper.SessionManager
import com.example.urgetruckkotlin.repository.URGETRUCKRepository
import com.zebra.rfid.api3.TagData

class ExitClearanceNewActivity : AppCompatActivity() ,
    RFIDHandlerForExiteClearance.ResponseHandlerInterface{
    lateinit var binding: ActivityExitClearanceNewBinding
    lateinit var TagDataSet: ArrayList<String>
    private lateinit var session: SessionManager

    //rfid
    private var mediaPlayer: MediaPlayer? = null
    var rfidHandler: RFIDHandlerForExiteClearance? = null
    var isRFIDInit = false
    var resumeFlag = false



    private lateinit var progress: ProgressDialog
    private fun initReader() {
        rfidHandler = RFIDHandlerForExiteClearance()
        rfidHandler!!.init(this)
    }

    private fun defaulReaderOn() {
        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            isRFIDInit = true
            Thread.sleep(1000)
            initReader()
        }}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_exit_clearance_new)
        session = SessionManager(this)
        progress = ProgressDialog(this)
        progress.setMessage("Loading...")
        TagDataSet = ArrayList()
        val urgeTruckRepository = URGETRUCKRepository()
        binding.layoutToolbar.toolbarText.setText("Exit Clearance")
        mediaPlayer = MediaPlayer.create(this, R.raw.scanner_sound)
        binding.layoutToolbar.ivLogoLeftToolbar.visibility = View.VISIBLE
        binding.layoutToolbar.ivLogoLeftToolbar.setImageResource(R.drawable.ut_logo_with_outline)
        binding.layoutToolbar.ivLogoLeftToolbar.setOnClickListener { view: View? ->
            startActivity(
                Intent(
                    this@ExitClearanceNewActivity,
                    HomeActivity::class.java
                )
            )
            finishAffinity()
        }
        defaulReaderOn()

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
            Toast.makeText(this@ExitClearanceNewActivity, status, Toast.LENGTH_SHORT).show()
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