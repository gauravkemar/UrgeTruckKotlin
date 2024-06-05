package com.example.urgetruckkotlin.model.trackVehical

data class  TrackVehicleResultModel(
    val requestId: String,
    val status: String,
    val statusMessage: String,
    val vehicleTransactionDetails: VehicleTransactionDetails,

    )