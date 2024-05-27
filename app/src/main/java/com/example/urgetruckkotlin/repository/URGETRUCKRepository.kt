package com.example.urgetruckkotlin.repository

import com.example.urgetruckkotlin.api.RetrofitInstance
import com.example.urgetruckkotlin.helper.Constants.HTTP_HEADER_AUTHORIZATION
import com.example.urgetruckkotlin.model.login.LoginRequest
import com.example.urgetruckkotlin.model.login.vehicalDetection.PostRfidModel
import com.example.urgetruckkotlin.model.login.vehicalDetection.Rfid
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Query

class URGETRUCKRepository {

    suspend fun login(
        baseUrl: String,
        loginRequest: LoginRequest
    ) = RetrofitInstance.api(baseUrl).login(loginRequest)

    suspend fun postrfID(
        token:String,
        baseUrl: String,
        @Body
         postRfidModel: PostRfidModel
    )=RetrofitInstance.api(baseUrl).postRfid(token,postRfidModel)


suspend fun getlocationDefault(  token:String,
                                 baseUrl: String,
                                 @Body
                                 getLocationListResponse    : GetLocationListResponse
                                 @Query

)
=RetrofitInstance.api(baseUrl).getLocationMasterDefaultData(token,getLocationListResponse)
}