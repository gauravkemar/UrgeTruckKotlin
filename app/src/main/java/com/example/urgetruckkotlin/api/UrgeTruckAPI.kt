package com.example.urgetruckkotlin.api


import com.example.urgetruckkotlin.helper.Constants.GET_ALL_WEIGHBRIDGE_LIST
import com.example.urgetruckkotlin.helper.Constants.GET_LOCATION_DEFAULT
import com.example.urgetruckkotlin.helper.Constants.GET_LOCATION_LIST
import com.example.urgetruckkotlin.helper.Constants.GET_LOCATION_MASTER_DATA_BY_LOCATION_ID
import com.example.urgetruckkotlin.helper.Constants.GET_WEIGHMENT_DETAILS
import com.example.urgetruckkotlin.helper.Constants.HTTP_HEADER_AUTHORIZATION
import com.example.urgetruckkotlin.helper.Constants.LOGIN_URL
import com.example.urgetruckkotlin.helper.Constants.POST_EXIT_CLEARANCE
import com.example.urgetruckkotlin.helper.Constants.POST_RFId
import com.example.urgetruckkotlin.helper.Constants.POST_VEHICAL_TRACKING_REQUEST
import com.example.urgetruckkotlin.helper.Constants.Post_RFIDMAPPING
import com.example.urgetruckkotlin.model.login.LoginRequest
import com.example.urgetruckkotlin.model.login.LoginResultModel
import com.example.urgetruckkotlin.model.login.vehicalDetection.PostRfidModel
import com.example.urgetruckkotlin.model.login.vehicalDetection.PostRfidResultModel
import com.example.urgetruckkotlin.model.login.vehicalDetection.getLocationList.GetLocationListResponse
import com.example.urgetruckkotlin.model.login.vehicalDetection.getlocationmasterdatabylocationId.GetLocationMasterDataByLocationId
import com.example.urgetruckkotlin.model.securityInspection.SecurityCheckResultModel
import com.example.urgetruckkotlin.model.securityInspection.WBResponseModel
import com.example.urgetruckkotlin.model.securityInspection.WeighmentDetails
import com.example.urgetruckkotlin.model.securityInspection.WeightDetailsResultModel
import com.example.urgetruckkotlin.model.trackVehical.TrackVehicleModel
import com.example.urgetruckkotlin.model.trackVehical.TrackVehicleResultModel
import com.example.urgetruckkotlin.model.vehicalMapping.RfidMappingModel
import com.example.urgetruckkotlin.model.vehicalMapping.RfidMappingResultModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface UrgeTruckAPI {
    @POST(LOGIN_URL)
    suspend fun login(
        @Body
        loginRequest: LoginRequest
    ): Response<LoginResultModel>

    //vehical Rfid mapping
    @POST(Post_RFIDMAPPING)
    suspend fun rfidMapping(
        @Header(HTTP_HEADER_AUTHORIZATION) token: String?,
        @Body
        rfidMappingModel: RfidMappingModel
    ): Response<RfidMappingResultModel>


    //Vehical detection Getlocation list
    @GET(GET_LOCATION_DEFAULT)
    suspend fun getVehicleLocationDefaultList(
        @Header(HTTP_HEADER_AUTHORIZATION) token: String?,
        @Query("RequestId") requestId: Int?,
        @Query("ParentLocationCode") parentLocationCode: String?,
    ): Response<GetLocationListResponse>


    @GET(GET_LOCATION_LIST)
    suspend fun getVehicleLocationList(
        @Header(HTTP_HEADER_AUTHORIZATION) token: String?,
        @Query("RequestId") requestId: Int?,
        @Query("ParentLocationCode") parentLocationCode: String?,
    ): Response<GetLocationListResponse>

    @GET(GET_LOCATION_MASTER_DATA_BY_LOCATION_ID)
    suspend fun getLocationMasterDataByLocationId(
        @Header(HTTP_HEADER_AUTHORIZATION) token: String?,
        @Query("RequestId") requestId: Int?,
        @Query("LocationId") locationId: Int?,
    ): Response<ArrayList<GetLocationMasterDataByLocationId>>


    @POST(POST_RFId)
    suspend fun postRfid(
        @Header(HTTP_HEADER_AUTHORIZATION) token: String?,
        @Body postRfidModel: PostRfidModel
    ): Response<PostRfidResultModel>

    //Security Inspection
    @GET(GET_ALL_WEIGHBRIDGE_LIST)
    suspend fun getAllWeighBridgeList(
        @Header(HTTP_HEADER_AUTHORIZATION) token: String?,
    ): Response<ArrayList<WBResponseModel>>

    @GET(GET_WEIGHMENT_DETAILS)
    suspend fun getWeightDetails(
        @Header(HTTP_HEADER_AUTHORIZATION) token: String?,
        @Query("RequestId") requestId: Int?,
        @Query("RFIDTagNo") rfid: String?,
        @Query("VRN") vrn: String?
    ): Response<WeightDetailsResultModel>

    //Track Vehical
    @POST(POST_VEHICAL_TRACKING_REQUEST)
    suspend fun getTrackVehicleDetails(
        @Header(HTTP_HEADER_AUTHORIZATION) token: String?,
        @Body trackVehicleModel: TrackVehicleModel,
//        @Query("RequestId") requestId: Int?,
//        @Query("RFIDTagNo") rfid: String?,
//        @Query("VRN") vrn: String?
    ): Response<TrackVehicleResultModel>

    @Multipart
    @POST(POST_EXIT_CLEARANCE)
    suspend fun postSecurityCheck(
        @Header(HTTP_HEADER_AUTHORIZATION) token: String?,
        @Part files: List<MultipartBody.Part>,
        @Part("JSONData") name: RequestBody
    ): Response<SecurityCheckResultModel>


}