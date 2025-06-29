package com.example.applaporfik.fragment.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AdminReportViewModel : ViewModel() {

    private val _shouldReload = MutableLiveData<Boolean>()
    val shouldReload: LiveData<Boolean> get() = _shouldReload

    fun triggerReload() {
        _shouldReload.value = true
    }

    fun resetReload() {
        _shouldReload.value = false
    }
}
