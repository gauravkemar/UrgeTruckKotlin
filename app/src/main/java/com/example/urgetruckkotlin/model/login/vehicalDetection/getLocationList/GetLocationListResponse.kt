package com.example.urgetruckkotlin.model.login.vehicalDetection.getLocationList

data class GetLocationListResponse(
    val locations: List<Location>,
    val status: Int,
    val statusMessage: Any
)