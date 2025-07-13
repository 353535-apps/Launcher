package com.shk.launcher

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

@Composable
fun FavoritesScreen(
    allApps: List<AppInfo>,
    favorites: SnapshotStateList<AppInfo> // ✅ Now correctly typed
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 72.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(favorites) { app ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        val intent = context.packageManager
                            .getLaunchIntentForPackage(app.packageName)
                        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent?.let { context.startActivity(it) }
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

        FloatingActionButton(
            onClick = {
                val pickable = allApps.filterNot { favorites.contains(it) }
                val toAdd = pickable.randomOrNull()
                toAdd?.let { favorites.add(it) }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
    }
}