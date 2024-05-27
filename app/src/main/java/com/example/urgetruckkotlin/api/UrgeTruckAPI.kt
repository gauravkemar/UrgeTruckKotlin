package com.example.urgetruckkotlin.api


import com.example.urgetruckkotlin.helper.Constants.GET_LOCATION_DEFAULT
import com.example.urgetruckkotlin.helper.Constants.GET_LOCATION_LIST
import com.example.urgetruckkotlin.helper.Constants.GET_LOCATION_MASTER_DATA_BY_LOCATION_ID
import com.example.urgetruckkotlin.helper.Constants.HTTP_HEADER_AUTHORIZATION
import com.example.urgetruckkotlin.helper.Constants.LOGIN_URL
import com.example.urgetruckkotlin.helper.Constants.POST_RFId
import com.example.urgetruckkotlin.model.login.LoginRequest
import com.example.urgetruckkotlin.model.login.LoginResultModel
import com.example.urgetruckkotlin.model.login.vehicalDetection.PostRfidModel
import com.example.urgetruckkotlin.model.login.vehicalDetection.PostRfidResultModel
import com.example.urgetruckkotlin.model.login.vehicalDetection.getLocationList.GetLocationListResponse
import com.example.urgetruckkotlin.model.login.vehicalDetection.getlocationmasterdatabylocationId.GetLocationMasterDataByLocationId
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
    ): Response<LoginResultModel>


    //Vehical detection Getlocation list
    @GET(GET_LOCATION_DEFAULT)
    suspend fun getVehicleLocationDefaultList(
        @Header(HTTP_HEADER_AUTHORIZATION) token: String?,
        @Query("RequestId")requestId:Int?,
        @Query("ParentLocationCode")parentLocationCode:String?,
    ):Response<GetLocationListResponse>


    @GET(GET_LOCATION_LIST)
    suspend fun getVehicleLocationList(
        @Header(HTTP_HEADER_AUTHORIZATION) token: String?,
        @Query("RequestId") requestId:Int?,
        @Query("ParentLocationCode") parentLocationCode: String?,
    ):Response<GetLocationListResponse>

    @GET(GET_LOCATION_MASTER_DATA_BY_LOCATION_ID)
    suspend fun getLocationMasterDataByLocationId(
        @Header(HTTP_HEADER_AUTHORIZATION) token: String?,
        @Query("RequestId")requestId:Int?,
        @Query("LocationId")locationId:Int?,
    ):Response<ArrayList<GetLocationMasterDataByLocationId>>


    @POST(POST_RFId)
    suspend fun postRfid(
        @Header(HTTP_HEADER_AUTHORIZATION) token: String?,
             @Body postRfidModel: PostRfidModel
    ):Response<PostRfidResultModel>


}