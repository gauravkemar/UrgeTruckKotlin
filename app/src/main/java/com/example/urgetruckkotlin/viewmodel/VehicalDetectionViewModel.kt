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
import com.example.urgetruckkotlin.repository.URGETRUCKRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response


class VehicalDetectionViewModel(
    application: Application,
    private val rfidRepository: URGETRUCKRepository
) : AndroidViewModel(application) {
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
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }


    val getParentLocationDefaultDataMutableLiveData: MutableLiveData<Resource<PostRfidResultModel>> =
        MutableLiveData()

    fun getParentLocationDefault(
        token: String,
        baseUrl: String,
        getParentLocationDefaultModel: GetLocationListResponse
    ) {
        viewModelScope.launch {
            safeAPICallgetParentLocationDefault(token, baseUrl,getParentLocationDefaultModel)
        }
    }

    private suspend fun safeAPICallgetParentLocationDefault(
        token: String,
        baseUrl: String,
        getParentLocationDefaultModel: GetLocationListResponse
    ) {
        getParentLocationDefaultDataMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = rfidRepository.postrfID(token, baseUrl, getParentLocationDefaultModel)
                getParentLocationDefaultDataMutableLiveData.postValue(handlegetParentLocationDefaultResponse(response))
            } else {
                getParentLocationDefaultDataMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getParentLocationDefaultDataMutableLiveData.postValue(Resource.Error("${t.message}"))
                }

                else -> getParentLocationDefaultDataMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handlegetParentLocationDefaultResponse(response: Response<getParentLocationDefaultResultModel>): Resource<getParentLocationDefaultResultModel> {
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
}

