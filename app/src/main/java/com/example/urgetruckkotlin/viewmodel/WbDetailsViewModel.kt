package com.example.urgetruckkotlin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.urgetruckkotlin.helper.Constants
import com.example.urgetruckkotlin.helper.Resource
import com.example.urgetruckkotlin.helper.Utils

import com.example.urgetruckkotlin.model.securityInspection.WeightDetailsResultModel
import com.example.urgetruckkotlin.repository.URGETRUCKRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class WbDetailsViewModel(
    application: Application,

    private val rfidRepository: URGETRUCKRepository
) : AndroidViewModel(application) {

    val getWeightDetailsMutableLiveData: MutableLiveData<Resource<WeightDetailsResultModel>> =
        MutableLiveData()

    fun getWeightDetails(
        token: String,
        baseUrl: String,
        requestId:Int,
        rfid: String,
        vrn: String

    ) {
        viewModelScope.launch {
            safeAPICallGetWeightDetails(token, baseUrl, requestId, rfid, vrn)
        }
    }

    private fun handleDtmsGetWeightDetails(response: Response<WeightDetailsResultModel>): Resource<WeightDetailsResultModel> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { Response ->
                return Resource.Success(Response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }

    private suspend fun safeAPICallGetWeightDetails(
        token: String,
        baseUrl: String,
        requestId: Int,
        rfid: String,
        vrn: String
    ) {
        getWeightDetailsMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = rfidRepository.getWeightDetails(token, baseUrl, requestId, rfid, vrn)
                getWeightDetailsMutableLiveData.postValue(
                    handleDtmsGetWeightDetails(response)
                )
            } else {
                getWeightDetailsMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getWeightDetailsMutableLiveData.postValue(Resource.Error("${t.message}"))
                }

                else -> getWeightDetailsMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
}
