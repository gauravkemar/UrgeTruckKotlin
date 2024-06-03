package com.example.urgetruckkotlin.model.login

data class  LoginResultModel (
    val id: Int,
    val firstName : String,
    val lastName :String,
    val email : String,
    val role : String,
    val isVerified :Boolean,
    val jwtToken :String,
    val refreshToken:String,
    val userName :String,
    val mobileNumber : String,
    val message :String
)