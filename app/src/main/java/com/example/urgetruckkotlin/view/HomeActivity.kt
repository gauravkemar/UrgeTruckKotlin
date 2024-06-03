package com.example.urgetruckkotlin.view

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.urgetruckkotlin.MainActivity
import com.example.urgetruckkotlin.R
import com.example.urgetruckkotlin.databinding.ActivityHomeBinding
import com.example.urgetruckkotlin.databinding.ActivityLoginBinding
import com.example.urgetruckkotlin.helper.Constants
import com.example.urgetruckkotlin.helper.Resource
import com.example.urgetruckkotlin.helper.SessionManager
import com.example.urgetruckkotlin.helper.Utils
import com.example.urgetruckkotlin.model.login.LoginRequest
import com.example.urgetruckkotlin.repository.URGETRUCKRepository
import com.example.urgetruckkotlin.viewmodel.LoginViewModel
import com.example.urgetruckkotlin.viewmodel.LoginViewmodelFactory
import es.dmoral.toasty.Toasty

class HomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding
    private lateinit var session: SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this,R.layout.activity_home)
        session = SessionManager(this)


        val layout_toolbar = findViewById<View>(R.id.layout_toolbar)
        val settingBtn = layout_toolbar.findViewById<ImageView>(R.id.ivRightSettings)
         binding.layoutToolbar.ivLeftToolbar.visibility=View.VISIBLE
         binding.layoutToolbar.ivLeftToolbar.setImageResource(R.drawable.ut_logo_with_outline)
         binding .layoutToolbar.ivRightToolbar.visibility=View.VISIBLE
         binding .layoutToolbar.ivRightToolbar.setOnClickListener { view: View? ->      showLogoutDialog()}



        binding.cardviewScanrfid.setOnClickListener {
            startActivity(Intent(this@HomeActivity,VehicleDetectionActivity::class.java))
        }
        binding.cardviewTagfid.setOnClickListener {
            startActivity(Intent(this@HomeActivity,VehicalRifdMappingActivity::class.java))
        }
        binding.cardviewSecuritycheck.setOnClickListener {
            startActivity(Intent(this@HomeActivity,SecurityInspectionActivity::class.java))
        }
        binding.cardviewPhysicalcheck.setOnClickListener {
            startActivity(Intent(this@HomeActivity,ExitClearanceNewActivity::class.java))
        }
        binding.cardviewTrack.setOnClickListener {
            startActivity(Intent(this@HomeActivity,TrackVehicleActivity::class.java))
        }
        binding.cardviewStartInvoice.setOnClickListener {
            startActivity(Intent(this@HomeActivity,AdminActivity::class.java))
        }



        if (Utils.getSharedPrefs(this, "isadmin").equals("true")) {

            settingBtn.visibility = View.VISIBLE
        }




        val toolbarText = layout_toolbar.findViewById<TextView>(R.id.toolbarText)
        toolbarText.setText(Utils.getSharedPrefs(this, "username"))

        settingBtn.setOnClickListener {
       showLogoutDialog()
        }



    }



    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { dialog, which ->

                logout()
            }
            .setNegativeButton("Cancel") { dialog, which ->

                dialog.dismiss()
            }
            .setCancelable(false)

        val dialog = builder.create()
        dialog.show()
    }
    private fun logout(){
        session.logoutUser()
        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
        finish()
    }

}
