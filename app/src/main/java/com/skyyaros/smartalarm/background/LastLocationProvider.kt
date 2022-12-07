package com.skyyaros.smartalarm.background

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.delay
import java.lang.IllegalStateException
import java.util.NoSuchElementException

class LastLocationProvider (private val context: Context) {

    suspend fun getLocation(): Result<Location> {
        if (!hasLocationPermission()) {
            return Result.failure(IllegalStateException("Missing location permission"))
        }
        val lastLocationTask = getLocationTask()
        while (!lastLocationTask.isComplete) {
            delay(100)
        }
        val location = lastLocationTask.getLocation()
        return if (location != null) {
            Result.success(location)
        } else {
            Result.failure(NoSuchElementException("No location found"))
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocationTask(): Task<Location> {
        return LocationServices
            .getFusedLocationProviderClient(context)
            .getCurrentLocation(
                CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMaxUpdateAgeMillis(1000*60*2)
                    .build(),
                null
            )
    }

    private fun Task<Location>.getLocation(): Location? {
        return if (isSuccessful && result != null) {
            result!!
        } else {
            null
        }
    }

    private fun hasLocationPermission(): Boolean {
        val backgroundLocation = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        return (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
                && backgroundLocation
    }
}