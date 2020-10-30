package com.example.speechtotextnotes.main

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.realm.Realm

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )
    }

    val isEmpty = MutableLiveData<Boolean>()
    val hasPermission = MutableLiveData<Boolean>()

    fun notifyWhenDbIsEmpty(realm: Realm) {
        when (realm.isEmpty) {
            true -> {
                isEmpty.value = true
            }
            false -> isEmpty.value = false
        }
    }

    fun checkPermissions() {
        when (hasAllPermissionsGranted(*permissions)) {
            true -> hasPermission.value = true
            else -> hasPermission.value = false
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
