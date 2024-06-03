package com.example.urgetruckkotlin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.urgetruckkotlin.helper.Constants
import com.example.urgetruckkotlin.helper.Resource
import com.example.urgetruckkotlin.helper.Utils
import com.example.urgetruckkotlin.model.securityInspection.WBResponseModel
import com.example.urgetruckkotlin.repository.URGETRUCKRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class WbListViewModel (
    application: Application,

    private val rfidRepository: URGETRUCKRepository
) : AndroidViewModel(application) {
    val getAllWeighBridgeListMutableLiveData: MutableLiveData<Resource<ArrayList<WBResponseModel>>> =
        MutableLiveData()

    fun getAllWeighBridgeList(
        token: String,
        baseUrl: String,

        ) {
        viewModelScope.launch {
            safeAPICallGetAllWeighBridgeList(token, baseUrl)
        }
    }

    private fun handleDtmsgetAllWeighBridgeListResponse(response: Response<ArrayList<WBResponseModel>>): Resource<ArrayList<WBResponseModel>> {
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

    private suspend fun safeAPICallGetAllWeighBridgeList(token: String, baseUrl: String) {
        getAllWeighBridgeListMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = rfidRepository.getAllWeighBridgeList(token, baseUrl)
                getAllWeighBridgeListMutableLiveData.postValue(
                    handleDtmsgetAllWeighBridgeListResponse(response)
                )
            } else {
                getAllWeighBridgeListMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getAllWeighBridgeListMutableLiveData.postValue(Resource.Error("${t.message}"))
                }

                else -> getAllWeighBridgeListMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }}