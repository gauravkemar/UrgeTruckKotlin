package com.example.urgetruckkotlin.view

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.urgetruckkotlin.MainActivity
import com.example.urgetruckkotlin.R
import com.example.urgetruckkotlin.databinding.ActivityLoginBinding
import com.example.urgetruckkotlin.helper.Constants
import com.example.urgetruckkotlin.helper.Constants.KEY_ISLOGGEDIN
import com.example.urgetruckkotlin.helper.Resource
import com.example.urgetruckkotlin.helper.SessionManager
import com.example.urgetruckkotlin.helper.Utils
import com.example.urgetruckkotlin.model.login.LoginRequest
import com.example.urgetruckkotlin.repository.URGETRUCKRepository
import com.example.urgetruckkotlin.viewmodel.LoginViewModel
import com.example.urgetruckkotlin.viewmodel.LoginViewmodelFactory
import es.dmoral.toasty.Toasty

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        session = SessionManager(this)
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        val urgeTruckRepository = URGETRUCKRepository()
        val viewModelProviderFactory = LoginViewmodelFactory(application, urgeTruckRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[LoginViewModel::class.java]
        session = SessionManager(this)
        binding.buttonLogin.setOnClickListener {

            if (binding.tvusername.text.toString()
                    .trim() == "admin" && binding.tvpassword.text.toString().trim() == "utmobile"
            ) {
                Utils.setSharedPrefs(this@LoginActivity, "isadmin", "true")
                Utils.setSharedPrefs(this@LoginActivity, "username", "Administrator")
                Utils.setSharedPrefs(this@LoginActivity, "token", "local")
                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                finish()
            } else {
                login()
            }

            //startActivity(Intent(this@LoginActivity,VinRfidMappingActivity::class.java))
        }

        viewModel.loginMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            session.getLoginSession(
                                resultResponse.userName,
                                resultResponse.jwtToken
                            )
                            Utils.setSharedPrefsBoolean(this@LoginActivity, KEY_ISLOGGEDIN, true)
                            startActivity()

                        } catch (e: Exception) {
                            Toasty.warning(
                                this@LoginActivity,
                                "hello" + e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }

                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { errorMessage ->
                        Toasty.error(
                            this@LoginActivity,
                            "Login failed - \nError Message: $errorMessage"
                        ).show()
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }

                else -> {}
            }
        }
    }

    fun startActivity() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }

    private fun clear() {
        binding.tvusername.setText("")
        binding.tvpassword.setText("")
    }

    private fun showProgressBar() {
        progress.show()
    }

    private fun hideProgressBar() {
        progress.cancel()
    }

    private fun showErrorMessage(message: String) {
        Toasty.warning(this@LoginActivity, message, Toasty.LENGTH_SHORT).show()
    }

    private fun validateInput(userId: String, password: String): String? {
        return when {
            userId.isEmpty() || password.isEmpty() -> "Please enter valid credentials"
            userId.length < 5 -> "Please enter at least 5 characters for the username"
            password.length < 6 -> "Please enter a password with more than 6 characters"
            else -> null
        }
    }

    fun login() {
        try {
            // Fetching user credentials from input fields
            val userId = binding.tvusername.text.toString().trim()
            val password = binding.tvpassword.text.toString().trim()

            // Validate user input
            val validationMessage = validateInput(userId, password)
            if (validationMessage == null) {
                val loginRequest = LoginRequest(password, userId)
                val baseurl = Utils.getSharedPrefs(this@LoginActivity, "apiurl")
                viewModel.login(baseurl.toString(), loginRequest)


            } else {
                showErrorMessage(validationMessage)
            }
        } catch (e: Exception) {
            showErrorMessage(e.printStackTrace().toString())
        }
    }

}
