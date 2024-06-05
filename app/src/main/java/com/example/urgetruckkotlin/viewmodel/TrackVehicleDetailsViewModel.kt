package com.example.urgetruckkotlin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.urgetruckkotlin.helper.Constants
import com.example.urgetruckkotlin.helper.Resource
import com.example.urgetruckkotlin.helper.Utils

import com.example.urgetruckkotlin.model.trackVehical.TrackVehicleModel
import com.example.urgetruckkotlin.model.trackVehical.TrackVehicleResultModel
import com.example.urgetruckkotlin.repository.URGETRUCKRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class TrackVehicleDetailsViewModel(
    application: Application,

    private val rfidRepository: URGETRUCKRepository
) : AndroidViewModel(application) {

    val getVehicleTrackingDetailsMutableLiveData: MutableLiveData<Resource<TrackVehicleResultModel>> =
        MutableLiveData()

    fun getTrackVehicleDetails(
        token: String,
        baseUrl: String,
        trackVehicleModel: TrackVehicleModel


    ) {
        viewModelScope.launch {
            safeAPICallGetVehicleTrackingDetails(token, baseUrl, trackVehicleModel)

        }
    }

    private fun handleTrackVehicleModel(response: Response<TrackVehicleResultModel>): Resource<TrackVehicleResultModel> {
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

    private suspend fun safeAPICallGetVehicleTrackingDetails(
        token: String,
        baseUrl: String,
        trackVehicleModel: TrackVehicleModel

    ) {
        getVehicleTrackingDetailsMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response =
                    rfidRepository.getTrackVehicleDetails(token, baseUrl, trackVehicleModel)

                getVehicleTrackingDetailsMutableLiveData.postValue(
                    handleTrackVehicleModel(response)
                )
            } else {
                getVehicleTrackingDetailsMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getVehicleTrackingDetailsMutableLiveData.postValue(Resource.Error("${t.message}"))
                }

                else -> getVehicleTrackingDetailsMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
}
