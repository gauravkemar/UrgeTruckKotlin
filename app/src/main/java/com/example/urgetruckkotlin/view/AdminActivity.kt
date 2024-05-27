package com.example.urgetruckkotlin.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.urgetruckkotlin.R
import com.example.urgetruckkotlin.databinding.ActivityAdminBinding
import com.example.urgetruckkotlin.helper.SessionManager
import com.example.urgetruckkotlin.helper.Utils
import com.google.android.ads.mediationtestsuite.activities.HomeActivity

class AdminActivity : AppCompatActivity() {
    lateinit var binding: ActivityAdminBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_admin)
        session = SessionManager(this)
        val layout_toolbar = findViewById<View>(R.id.layout_toolbar)
        val toolbarText = layout_toolbar.findViewById<TextView>(R.id.toolbarText)
        toolbarText.setText(getString(R.string.admin))

        val settingBtn = layout_toolbar.findViewById<ImageView>(R.id.ivRightSettings)

        val ivLogo: ImageView = layout_toolbar.findViewById<ImageView>(R.id.ivLogoLeftToolbar)
        ivLogo.visibility = View.VISIBLE

        binding.btAntenna.setOnClickListener { checkinputAntenna() }
        binding.btSubmit.setOnClickListener { checkinputUrl() }

        ivLogo.setImageResource(R.drawable.ut_logo_with_outline)
        ivLogo.setOnClickListener { view: View? ->
            startActivity(
                Intent(
                    this@AdminActivity,
                    HomeActivity::class.java
                )
            )
            finishAffinity()
        }
    }

    private fun checkinputUrl() {
        val url: String = binding.edServerIp.getText().toString().trim { it <= ' ' }
        if (url == "") {
            binding.edServerIp.setError("Please enter ip address")
        } else {
            Utils.setSharedPrefs(this@AdminActivity, "token", "")
            Utils.setSharedPrefs(this@AdminActivity, "username", "")
            Utils.setSharedPrefs(this@AdminActivity, "isadmin", "")
            Utils.setSharedPrefs(this@AdminActivity, "apiurl", url)
            showCustomDialogFinish(
                this@AdminActivity,
                "Base Url Updated. Changes will take place after Re-Login"
            )
        }
    }

    fun showCustomDialogFinish(context: Context?, message: String?) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { dialogInterface, i ->
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }
            .show()
    }

    private fun checkinputAntenna() {
        val antenna: String = binding.tvantenna.text.toString().trim { it <= ' ' }
        if (antenna.isEmpty()) {
            binding.textinputantenna.setError("Please enter the Antenna Power")
        } else if (antenna.toInt() > 300) {
            binding.textinputantenna.setError("Entered Antenna Power Should be less than 300")
        } else {

            Utils.getSharedPrefs(this@AdminActivity, "antennapower", antenna)

            session.showCustomDialogFinish(
                this@AdminActivity,
                "Antenna Power Successfully Updated"
            )
        }
    }

}
