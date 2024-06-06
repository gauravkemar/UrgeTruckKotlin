package com.example.urgetruckkotlin.repository

import com.example.urgetruckkotlin.api.RetrofitInstance
import com.example.urgetruckkotlin.model.login.LoginRequest
import com.example.urgetruckkotlin.model.login.vehicalDetection.PostRfidModel
import com.example.urgetruckkotlin.model.trackVehical.TrackVehicleModel
import com.example.urgetruckkotlin.model.vehicalMapping.RfidMappingModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Part
import retrofit2.http.Query

class URGETRUCKRepository {

    suspend fun login(
        baseUrl: String,
        loginRequest: LoginRequest
    ) = RetrofitInstance.api(baseUrl).login(loginRequest)

    suspend fun postrfID(
        token: String,
        baseUrl: String,
        @Body
        postRfidModel: PostRfidModel
    ) = RetrofitInstance.api(baseUrl).postRfid(token, postRfidModel)

    suspend fun rfidMapping(
        token: String,
        baseUrl: String,
        @Body
        rfidMappingModel: RfidMappingModel
    ) = RetrofitInstance.api(baseUrl).rfidMapping(token, rfidMappingModel)


    //new vehicle detection


    suspend fun getVehicleLocationDefaultList(
        token: String,
        baseUrl: String,
        @Query("RequestId") requestId: Int,
        @Query("ParentLocationCode") parentLocationCode: String?
    ) = RetrofitInstance.api(baseUrl)
        .getVehicleLocationDefaultList(token, requestId, parentLocationCode)

    suspend fun getVehicleLocationList(
        token: String,
        baseUrl: String,
        @Query("RequestId") requestId: Int,
        @Query("ParentLocationCode") parentLocationCode: String?
    ) = RetrofitInstance.api(baseUrl).getVehicleLocationList(token, requestId, parentLocationCode)

    suspend fun getLocationMasterDataByLocationId(
        token: String,
        baseUrl: String,
        @Query("RequestId") RequestID: Int,
        @Query("LocationId") locationId: Int
    ) = RetrofitInstance.api(baseUrl)
        .getLocationMasterDataByLocationId(token, RequestID, locationId)

    suspend fun getAllWeighBridgeList(
        token: String,
        baseUrl: String,

        ) = RetrofitInstance.api(baseUrl)
        .getAllWeighBridgeList(token)


    suspend fun getWeightDetails(
        token: String,
        baseUrl: String,
        @Query("RequestId") requestId: Int,
        @Query("RFIDTagNo") rfid: String,
        @Query("VRN") vrn: String
    ) = RetrofitInstance.api(baseUrl).getWeightDetails(token,requestId,rfid,vrn)

    //Track vehical
    suspend fun getTrackVehicleDetails(
        token: String,
        baseUrl: String,
        @Body
        trackVehicleModel: TrackVehicleModel,
//        @Query("RequestId") requestId: Int,
//        @Query("RFIDTagNo") rfid: String,
//        @Query("VRN") vrn: String
    ) = RetrofitInstance.api(baseUrl).getTrackVehicleDetails(token,trackVehicleModel)


    suspend fun postSecurityCheck(
        token: String,
        baseUrl: String,
        @Part files: List<MultipartBody.Part>,
        @Part("JSONData") jsonData: RequestBody
    ) = RetrofitInstance.api(baseUrl).postSecurityCheck(token,files,jsonData)




}