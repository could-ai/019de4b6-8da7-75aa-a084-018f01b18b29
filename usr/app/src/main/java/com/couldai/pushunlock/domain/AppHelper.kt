package com.couldai.pushunlock.domain

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

data class InstalledApp(
    val packageName: String,
    val appName: String
)

fun getInstalledApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
    return packages.filter { 
        (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 && it.packageName != context.packageName
    }.map {
        InstalledApp(it.packageName, it.loadLabel(pm).toString())
    }.sortedBy { it.appName }
}
