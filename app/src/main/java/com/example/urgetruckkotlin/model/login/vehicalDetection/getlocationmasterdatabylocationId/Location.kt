package com.example.urgetruckkotlin.model.login.vehicalDetection.getlocationmasterdatabylocationId

data class Location(
    val createdBy: String,
    val createdDate: String,
    val currentQueue: List<Any>,
    val detectableBy: String,
    val deviceLocationMapping: List<DeviceLocationMapping>,
    val displayName: String,
    val isActive: Boolean,
    val ledNotification: List<Any>,
    val locationClosingTime: List<Any>,
    val locationCode: String,
    val locationId: Int,
    val locationName: String,
    val locationStatusHistory: List<Any>,
    val locationType: String,
    val maxQueueSize: Int,
    val minQueueSize: Int,
    val modifiedBy: String,
    val modifiedDate: String,
    val packerMaster: List<Any>,
    val parentLocationCode: String,
    val sequence: Int,
    val weighBridgeMaster: List<Any>,
    val weighbridgeAllocationPerferences: List<Any>
)