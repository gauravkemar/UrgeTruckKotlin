package com.example.urgetruckkotlin.repository

import com.example.urgetruckkotlin.api.RetrofitInstance
import com.example.urgetruckkotlin.model.login.LoginRequest
import com.example.urgetruckkotlin.model.login.vehicalDetection.PostRfidModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
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




    //new vehicle detection



    suspend fun getVehicleLocationDefaultList(
        token:String,
        baseUrl: String,
        @Query("RequestId") requestId: Int,
        @Query("ParentLocationCode") parentLocationCode: String?
    )=RetrofitInstance.api(baseUrl).getVehicleLocationDefaultList(token,requestId,parentLocationCode)

    suspend fun getVehicleLocationList(
        token:String,
        baseUrl: String,
        @Query("RequestId") requestId: Int,
        @Query("ParentLocationCode") parentLocationCode: String?
    )=RetrofitInstance.api(baseUrl).getVehicleLocationList(token,requestId,parentLocationCode)

    suspend fun getLocationMasterDataByLocationId(
        token:String,
        baseUrl: String,
        @Query("RequestId") RequestID: Int,
        @Query("LocationId") locationId: Int
    )=RetrofitInstance.api(baseUrl).getLocationMasterDataByLocationId(token,RequestID,locationId)




}