package com.example.urgetruckkotlin.api


import com.example.urgetruckkotlin.helper.Constants.GET_LOCATION_MASTER_DATA_BY_LOCATION_ID
import com.example.urgetruckkotlin.helper.Constants.GET_LOCATION_MASTER_URL
import com.example.urgetruckkotlin.helper.Constants.HTTP_HEADER_AUTHORIZATION
import com.example.urgetruckkotlin.helper.Constants.LOGIN_URL
import com.example.urgetruckkotlin.helper.Constants.POST_RFId
import com.example.urgetruckkotlin.model.login.LoginRequest
import com.example.urgetruckkotlin.model.login.LoginResponse
import com.example.urgetruckkotlin.model.login.vehicalDetection.PostRfidModel
import com.example.urgetruckkotlin.model.login.vehicalDetection.PostRfidResultModel
import com.example.urgetruckkotlin.model.login.vehicalDetection.Rfid
import com.example.urgetruckkotlin.model.login.vehicalDetection.getLocationList.GetLocationListResponse
import com.example.urgetruckkotlin.model.login.vehicalDetection.getLocationList.Location
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface UrgeTruckAPI {
    @POST(LOGIN_URL)
    suspend fun login(
        @Body
        loginRequest: LoginRequest
    ): Response<LoginResponse>


    //Vehical detection Getlocation list
    @GET(GET_LOCATION_MASTER_URL)
    suspend fun getLocationMasterData(
        @Header(HTTP_HEADER_AUTHORIZATION) token: String?,
        @Query("RequestId")requestId:Int?,
        @Query("ParentLocationCode")parentLocationCode:String?,
        @Body
        getLocationListRequest: Location
    ):Response<GetLocationListResponse>


    @GET(GET_LOCATION_MASTER_URL)
    suspend fun getLocationMasterDefaultData(
        @Header(HTTP_HEADER_AUTHORIZATION) token: String?,
        @Query("RequestId")requestId:Int?,
        @Query("ParentLocationCode")parentLocationCode:Int?,
        @Body
        getLocationListRequest: Location
    ):Response<GetLocationListResponse>

    @GET(GET_LOCATION_MASTER_DATA_BY_LOCATION_ID)
    suspend fun getLocationMasterDataByLocationId(
        @Header(HTTP_HEADER_AUTHORIZATION) token: String?,
        @Query("RequestId")requestId:Int?,
        @Query("LocationId")locationId:Int?,
        @Body
        getLocationListRequest: Location
    ):Response<GetLocationListResponse>


    @POST(POST_RFId)
    suspend fun postRfid(
        @Header(HTTP_HEADER_AUTHORIZATION) token: String?,
             @Body postRfidModel: PostRfidModel
    ):Response<PostRfidResultModel>


}