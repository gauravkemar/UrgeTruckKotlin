package com.example.urgetruckkotlin.model.trackVehical

data class MilestoneActionsTracking(
    val milestoneActionsTrackingId: Int?,
    val jobMilestoneId: Int?,
    val milestoneAction: String?,
    val actionCode: String?,
    val status: String?,
    val isRequired: Boolean?,
    val isActive: Boolean?,
    val isDependent: Boolean?,
    val dependentActionId: Boolean?,
    val isDependentOnAllPrevious: Boolean?,
    val sequence: Int?,
    val deActivatedBy: Any?,
    val remarks: Any?,
    val isSelected: Boolean?,
    val isShown: Boolean?,
    val isRemark: Boolean?,
    val completionTime: String?
)