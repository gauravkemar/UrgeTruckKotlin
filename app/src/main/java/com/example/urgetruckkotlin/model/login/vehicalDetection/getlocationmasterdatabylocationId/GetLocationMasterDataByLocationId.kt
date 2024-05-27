package com.example.urgetruckkotlin.model.login.vehicalDetection.getlocationmasterdatabylocationId

data class GetLocationMasterDataByLocationId(
    val accessURL: Any,
    val antenna: Int,
    val createdBy: String,
    val createdDate: String,
    val deviceIP: String,
    val deviceLocationMappingId: Int,
    val deviceName: String,
    val deviceStatus: List<Any>,
    val deviceType: String,
    val direction: String,
    val gpoManager: List<Any>,
    val isActive: Boolean,
    val lane: String,
    val ledNoOfLines: Any,
    val location: Location,
    val locationId: Int,
    val modifiedBy: String,
    val modifiedDate: String,
    val portNo: Any,
    val remark: String,
    val snapCaptureURL: Any
)