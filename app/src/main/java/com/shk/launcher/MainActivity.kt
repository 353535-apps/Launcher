package com.shk.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.shk.launcher.ui.theme.LauncherTheme

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val apps = getLaunchableApps()
        setContent {
            LauncherTheme { LauncherPager(apps) }
        }
    }

    private fun getLaunchableApps(): List<AppInfo> {
        val pm = packageManager
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherPager(apps: List<AppInfo>) {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        pageSpacing = 16.dp,
        flingBehavior = PagerDefaults.flingBehavior(pagerState)
    ) { page ->
        when (page) {
            0 -> ControlPanel()
            1 -> AppGrid(apps)    // swipe up/down here
            2 -> NotificationsPage()
        }
    }
}

@Composable
fun AppGrid(apps: List<AppInfo>) {
    val context = LocalContext.current
    val pm = context.packageManager

    Box(modifier = Modifier.fillMaxSize()) {
        // Invisible overlay to catch vertical swipes
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        when {
                            dragAmount > 50f -> { // swipe down
                                Intent(context, FavoritesActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .also(context::startActivity)
                            }
                            dragAmount < -50f -> { // swipe up
                                Intent(context, RecentsActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .also(context::startActivity)
                            }
                        }
                    }
                }
        )

        // Your actual grid of apps
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 72.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(apps) { app ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        pm.getLaunchIntentForPackage(app.packageName)
                            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ?.let(context::startActivity)
                    }
                ) {
                    Image(
                        bitmap = app.icon.toBitmap().asImageBitmap(),
                        contentDescription = app.name,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(app.name, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}