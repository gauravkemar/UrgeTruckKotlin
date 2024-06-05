package com.example.urgetruckkotlin.model.trackVehical

data class JobMilestone(
    val jobMilestoneId: Int?,
    val elvId: Int?,
    val vehicleTransactionId: Int?,
    val milestoneTransactionCode: String?,
    val milestone: String?,
    val milestoneCode: String?,
    val milestoneDescription: Any?,
    val milestioneEvent: String?,
    val locationCode: String?,
    val milestoneSequence: Int?,
    val isRequiredMilestone: Boolean?,
    val isActiveMilestone: Boolean?,
    val status: String?,
    val remarks: Any?,
    val milestoneBeginTime: Any?,
    val milestoneCompletionTime: Any?,
    val isAX4Updated: Boolean?,
    val locationName: String?,
    val locationId: Int?,
    val jobMilestoneDetails: List<Any>?,
    val milestoneActionsTracking: List<MilestoneActionsTracking>?,
    val weighBridgeTransaction: List<WeighBridgeTransaction>?,
    val loadUnloadTransaction: List<Any>?,
    var isExpandable: Boolean = false
// Custom field
)