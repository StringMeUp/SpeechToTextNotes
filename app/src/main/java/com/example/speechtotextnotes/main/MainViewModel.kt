package com.example.speechtotextnotes.main

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.realm.Realm

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )
    }

    //limit access to this val
    private val _isEmpty = MutableLiveData<Boolean>()

    //expose it via get in activity
    val isEmpty: LiveData<Boolean>
        get() = _isEmpty

    //same situation here
    private val _hasPermission = MutableLiveData<Boolean>()
    val hasPermission: LiveData<Boolean>
        get() = _hasPermission

    fun notifyWhenDbIsEmpty(realm: Realm) {
        when (realm.isEmpty) {
            true -> {
                _isEmpty.value = true
            }
            false -> _isEmpty.value = false
        }
    }

    fun checkPermissions() {
        when (hasAllPermissionsGranted(*permissions)) {
            true -> _hasPermission.value = true
            else -> _hasPermission.value = false
        }
    }

    //helper function
    private fun hasAllPermissionsGranted(vararg permission: String): Boolean {
        permission.forEach {
            return ActivityCompat.checkSelfPermission(
                getApplication(),
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }
}
