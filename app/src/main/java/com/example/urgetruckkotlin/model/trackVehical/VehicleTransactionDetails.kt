package com.example.urgetruckkotlin.model.trackVehical


data class VehicleTransactionDetails(

    val vehicleTransactionId: Int?,

    val vehicleTransactionCode: String?,

    val vrn: String?,

    val driverId: Int?,

    val rfidTagNumber: String?,

    val tranType: Int?,

    val shipmentNo: String?,

    val gateEntryNo: String?,

    val transactionDate: String?,

    val transactionStartTime: Any?, // Change to appropriate type if known

    val transactionEndTime: Any?, // Change to appropriate type if known

    val isActive: Boolean?,

    val tranStatus: String?,

    val remarks: Any?, // Change to appropriate type if known

    val driverName: String?,

    val phoneNumber: String?,
    val jobMilestones: ArrayList<JobMilestone>,
    // Omitting jobMilestones as requested
)