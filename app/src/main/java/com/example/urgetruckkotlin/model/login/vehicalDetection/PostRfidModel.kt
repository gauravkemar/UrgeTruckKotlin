package com.example.urgetruckkotlin.model.login.vehicalDetection



data class  PostRfidModel(
    val RequestId: String,
    val RFIDTagNo: String,
    val DevicelocationId: String,
    val VRN: String,
    val Reason: String
)
