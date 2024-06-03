package com.example.urgetruckkotlin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.urgetruckkotlin.helper.Constants
import com.example.urgetruckkotlin.helper.Resource
import com.example.urgetruckkotlin.helper.Utils
import com.example.urgetruckkotlin.model.login.vehicalDetection.PostRfidModel
import com.example.urgetruckkotlin.model.login.vehicalDetection.PostRfidResultModel
import com.example.urgetruckkotlin.model.login.vehicalDetection.getLocationList.GetLocationListResponse
import com.example.urgetruckkotlin.model.login.vehicalDetection.getlocationmasterdatabylocationId.GetLocationMasterDataByLocationId
import com.example.urgetruckkotlin.repository.URGETRUCKRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException


class VehicalDetectionViewModel(
    application: Application,
    private val rfidRepository: URGETRUCKRepository
) : AndroidViewModel(application) {


    val getVehicleLocationDefaultListMutable: MutableLiveData<Resource<GetLocationListResponse>> = MutableLiveData()

    fun getVehicleLocationDefaultList(token: String,baseUrl: String,  requestId: Int, parentLocationCode: String) {
        viewModelScope.launch {
            safeAPICallGetVehicleLocationDefaultList(token,baseUrl,requestId,parentLocationCode)
        }
    }

    private suspend fun safeAPICallGetVehicleLocationDefaultList(token: String,baseUrl: String,requestId: Int, parentLocationCode: String) {
        getVehicleLocationDefaultListMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = rfidRepository.getVehicleLocationDefaultList(token,baseUrl ,requestId,parentLocationCode)
                getVehicleLocationDefaultListMutable.postValue(handleGetVehicleLocationDefaultList(response))
            } else {
                getVehicleLocationDefaultListMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> getVehicleLocationDefaultListMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
                else -> getVehicleLocationDefaultListMutable.postValue(Resource.Error("${t.message}"))
            }
        }
    }

    private fun handleGetVehicleLocationDefaultList(response: Response<GetLocationListResponse>): Resource<GetLocationListResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { appDetailsResponse ->
                return Resource.Success(appDetailsResponse)
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

/////

    val getVehicleLocationListMutable: MutableLiveData<Resource<GetLocationListResponse>> = MutableLiveData()

    fun getVehicleLocationList(token: String,baseUrl: String,  requestId: Int, parentLocationCode: String) {
        viewModelScope.launch {
            safeAPICallGetVehicleLocationList(token,baseUrl,requestId,parentLocationCode)
        }
    }
    private suspend fun safeAPICallGetVehicleLocationList(token: String,baseUrl: String,requestId: Int, parentLocationCode: String) {
        getVehicleLocationListMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = rfidRepository.getVehicleLocationList(token,baseUrl ,requestId,parentLocationCode)
                getVehicleLocationListMutable.postValue(handleGetVehicleLocationList(response))
            } else {
                getVehicleLocationListMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> getVehicleLocationListMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
                else -> getVehicleLocationListMutable.postValue(Resource.Error("${t.message}"))
            }
        }
    }
    private fun handleGetVehicleLocationList(response: Response<GetLocationListResponse>): Resource<GetLocationListResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { appDetailsResponse ->
                return Resource.Success(appDetailsResponse)
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

/////

    val getLocationMasterDataByLocationIdMutable: MutableLiveData<Resource<ArrayList<GetLocationMasterDataByLocationId>>> = MutableLiveData()

    fun getLocationMasterDataByLocationId(token: String,baseUrl: String,  requestId: Int, locationId: Int) {
        viewModelScope.launch {
            safeAPICallGetLocationMasterDataByLocationId(token,baseUrl,requestId,locationId)
        }
    }
    private suspend fun safeAPICallGetLocationMasterDataByLocationId(token: String,baseUrl: String,requestId: Int, locationId: Int) {
        getLocationMasterDataByLocationIdMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = rfidRepository.getLocationMasterDataByLocationId(token,baseUrl ,requestId,locationId)
                getLocationMasterDataByLocationIdMutable.postValue(handleGetLocationMasterDataByLocationId(response))
            } else {
                getLocationMasterDataByLocationIdMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> getLocationMasterDataByLocationIdMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
                else -> getLocationMasterDataByLocationIdMutable.postValue(Resource.Error("${t.message}"))
            }
        }
    }
    private fun handleGetLocationMasterDataByLocationId(response: Response<ArrayList<GetLocationMasterDataByLocationId>>): Resource<ArrayList<GetLocationMasterDataByLocationId>> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { appDetailsResponse ->
                return Resource.Success(appDetailsResponse)
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





    //get Vehicle status
    val postrfIDMutableLiveData: MutableLiveData<Resource<PostRfidResultModel>> = MutableLiveData()

    fun postrfID(
        token: String,
        baseUrl: String,
        postRfidModel: PostRfidModel
    ) {
        viewModelScope.launch {
            safeAPICallPostrfID(token, baseUrl, postRfidModel)
        }
    }

    private suspend fun safeAPICallPostrfID(
        token: String,
        baseUrl: String,
        postRfidModel: PostRfidModel
    ) {
        postrfIDMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = rfidRepository.postrfID(token, baseUrl, postRfidModel)
                postrfIDMutableLiveData.postValue(handlePostrfIDResponse(response))
            } else {
                postrfIDMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    postrfIDMutableLiveData.postValue(Resource.Error("${t.message}"))
                }

                else -> postrfIDMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handlePostrfIDResponse(response: Response<PostRfidResultModel>): Resource<PostRfidResultModel> {
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

            }
        }
        return Resource.Error(errorMessage)
    }



}

