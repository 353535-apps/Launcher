package com.shk.launcher

import android.content.pm.PackageManager

object LauncherUtils {
    fun getLaunchableApps(pm: PackageManager): List<AppInfo> {
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .map {
                AppInfo(
                    name = pm.getApplicationLabel(it).toString(),
                    packageName = it.packageName,
                    icon = pm.getApplicationIcon(it)
                )
            }
    }
}