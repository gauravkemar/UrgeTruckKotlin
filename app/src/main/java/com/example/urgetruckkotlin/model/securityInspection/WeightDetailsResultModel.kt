package com.example.urgetruckkotlin.model.securityInspection

data class WeightDetailsResultModel(
    val requestId: String,
    val status: String,
    val statusMassage: String,
    val weighmentDetails: WeighmentDetails


)