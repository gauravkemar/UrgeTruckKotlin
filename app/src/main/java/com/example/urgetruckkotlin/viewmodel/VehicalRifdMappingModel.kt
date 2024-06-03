package com.example.urgetruckkotlin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.urgetruckkotlin.helper.Constants
import com.example.urgetruckkotlin.helper.Resource
import com.example.urgetruckkotlin.helper.Utils
import com.example.urgetruckkotlin.model.login.vehicalDetection.getLocationList.GetLocationListResponse

import com.example.urgetruckkotlin.model.vehicalMapping.RfidMappingModel
import com.example.urgetruckkotlin.model.vehicalMapping.RfidMappingResultModel
import com.example.urgetruckkotlin.repository.URGETRUCKRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

class VehicalRifdMappingModel (
    application: Application,
    private val rfidRepository: URGETRUCKRepository
) : AndroidViewModel(application){
    val rfidMappingMutable: MutableLiveData<Resource<RfidMappingResultModel>> = MutableLiveData()
    fun rfidMapping(token: String,baseUrl: String,rfidMappingModel: RfidMappingModel ) {
        viewModelScope.launch {
            safeAPICallPostRfidMapping(token,baseUrl,rfidMappingModel)
        }
    }

    private suspend fun safeAPICallPostRfidMapping(token: String,baseUrl: String, rfidMappingModel: RfidMappingModel) {
        rfidMappingMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = rfidRepository.rfidMapping(token,baseUrl, rfidMappingModel)
                rfidMappingMutable.postValue(handlePostRfidMappingMutable(response))
            } else {
                rfidMappingMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> rfidMappingMutable.postValue(Resource.Error(
                    Constants.CONFIG_ERROR))
                else -> rfidMappingMutable.postValue(Resource.Error("${t.message}"))
            }
        }
    }

    private fun handlePostRfidMappingMutable(response: Response<RfidMappingResultModel>): Resource<RfidMappingResultModel> {
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

                var errorStatus=it.getString("Status")
                if(errorStatus!=null)
                {
                    errorMessage = it.getString("Status")
                }
                else{
                    errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
                }

            }
        }
        return Resource.Error(errorMessage)
    }
}