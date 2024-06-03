package com.example.urgetruckkotlin.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.urgetruckkotlin.repository.URGETRUCKRepository

class VehicalRifdMappingModelFactory (
    private val application: Application,
    private val rfidRepository: URGETRUCKRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VehicalRifdMappingModel(application,rfidRepository) as T
    }
}