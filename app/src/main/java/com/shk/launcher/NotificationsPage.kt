package com.shk.launcher

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.service.notification.StatusBarNotification
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat

@Composable
fun NotificationsPage() {
    val context = LocalContext.current

    val hasAccess = remember {
        NotificationManagerCompat
            .getEnabledListenerPackages(context)
            .contains(context.packageName)
    }

    LaunchedEffect(hasAccess) {
        if (!hasAccess) {
            context.startActivity(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    val allNotifications by NotificationRepository.notifications.collectAsState()
    val localList = remember { mutableStateListOf<StatusBarNotification>() }

    LaunchedEffect(allNotifications) {
        localList.clear()
        localList.addAll(allNotifications)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (localList.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { localList.clear() }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Notifications", style = MaterialTheme.typography.titleMedium)
            }
            Divider()

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(localList) { index, sbn ->
                    NotificationRow(sbn) {
                        localList.removeAt(index)
                    }
                    Divider()
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No notifications", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun NotificationRow(
    sbn: StatusBarNotification,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val pi = sbn.notification.contentIntent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 100f || dragAmount < -100f) onDismiss()
                }
            }
            .clickable {
                try {
                    pi?.send()
                } catch (_: Exception) {
                    val launchIntent = context.packageManager
                        .getLaunchIntentForPackage(sbn.packageName)
                    launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(
                        launchIntent ?: Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", sbn.packageName, null)
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val title = sbn.notification.extras.getString("android.title") ?: sbn.packageName
        val text = sbn.notification.extras.getCharSequence("android.text")?.toString()
            ?: sbn.notification.extras.getCharSequence("android.bigText")?.toString()
            ?: ""

        Text(title, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}