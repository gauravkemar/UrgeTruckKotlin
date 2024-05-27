package com.example.urgetruckkotlin.model.login.vehicalDetection.getLocationList

data class Location(
    val createdBy: String,
    val createdDate: String,
    val currentQueue: List<Any>,
    val detectableBy: String,
    val displayName: String,
    val isActive: Boolean,
    val locationCode: String,
    val locationId: Int,
    val locationName: String,
    val locationType: Any,
    val maxQueueSize: Int,
    val minQueueSize: Int,
    val modifiedBy: Any,
    val modifiedDate: Any,
    val parentLocationCode: Any,
    val sequence: Int,
    val weighBridgeMaster: List<Any>
)