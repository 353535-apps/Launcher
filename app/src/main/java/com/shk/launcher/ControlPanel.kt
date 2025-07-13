package com.shk.launcher

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun ControlPanel() {
    val context = LocalContext.current

    // System services
    val wifiMgr = context.applicationContext
        .getSystemService(Context.WIFI_SERVICE) as WifiManager
    val btAdapter = BluetoothAdapter.getDefaultAdapter()
    val cameraMgr = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val camId = cameraMgr.cameraIdList.firstOrNull()

    // State holders
    var wifiOn by remember { mutableStateOf(wifiMgr.isWifiEnabled) }
    var bluetoothOn by remember { mutableStateOf(btAdapter?.isEnabled == true) }
    var flashlightOn by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(16.dp))

        // Wi-Fi toggle
        SettingRow("Wi-Fi", wifiOn) { new ->
            wifiOn = new
            wifiMgr.isWifiEnabled = new
        }
        Divider()

        // Bluetooth toggle (API 27)
        SettingRow("Bluetooth", bluetoothOn) { new ->
            bluetoothOn = new
            try {
                btAdapter?.let { adapter ->
                    if (new) adapter.enable() else adapter.disable()
                }
            } catch (_: SecurityException) {
            }
        }
        Divider()

        // Flashlight toggle (with CAMERA permission check)
        SettingRow("Flashlight", flashlightOn) { new ->
            val hasCamPerm = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasCamPerm || camId == null) {
                // Open app settings for CAMERA permission or unavailable camera
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } else {
                flashlightOn = new
                try {
                    cameraMgr.setTorchMode(camId, new)
                } catch (_: Exception) {
                }
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun SettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}