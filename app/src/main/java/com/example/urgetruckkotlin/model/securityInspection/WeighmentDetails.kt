package com.example.urgetruckkotlin.model.securityInspection

data class WeighmentDetails(
    val vrn: String,
    val vehicleTransactionId: String,
    val jobMilestoneId: String,
    val weighmentType: String,
    val expectedWeight: String,
    val actualWeight: String,

    )