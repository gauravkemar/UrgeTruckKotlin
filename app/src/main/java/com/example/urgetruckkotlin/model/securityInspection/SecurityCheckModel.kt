package com.example.urgetruckkotlin.model.securityInspection

data class SecurityCheckModel(
    val requestId: String,
    val RFIDTagNo: String,
    val jobMilestoneId: String,
    val vehicleTransactionId: String,
    val VRN: String,
    val weighBridgeId: String
)