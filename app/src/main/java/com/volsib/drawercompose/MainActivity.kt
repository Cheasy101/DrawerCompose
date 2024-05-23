package com.volsib.drawercompose

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.lifecycleScope
import com.volsib.drawercompose.data.DrawerDatabase
import com.volsib.drawercompose.data.Picture
import com.volsib.drawercompose.ui.BottomPanel
import com.volsib.drawercompose.ui.PathData
import com.volsib.drawercompose.ui.SaveDrawingDialog
import com.volsib.drawercompose.ui.theme.DrawerComposeTheme
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val pathData = remember { mutableStateOf(PathData()) }
            val pathList = remember { mutableStateListOf(PathData()) }
            val isBackPressed = remember { mutableStateOf(false) }
            val showDialog = remember { mutableStateOf(false) }

            DrawerComposeTheme {
                Column {
                    DrawCanvas(pathData, pathList, isBackPressed)
                    BottomPanel (
                        onColorClick = { color ->
                            pathData.value = pathData.value.copy(
                                color = color
                            )
                        },
                        onLineWidthChange = { lineWidth ->
                            pathData.value = pathData.value.copy(
                                lineWidth = lineWidth
                            )
                        },
                        onTransparencyChange = { transparency ->
                            pathData.value = pathData.value.copy(
                                transparency = transparency
                            )
                        },
                        onBackClick = {
                            pathList.removeIf { pathData ->
                                pathList[pathList.size - 1] == pathData
                            }
                            isBackPressed.value = true
                        },
                        onSaveClick = {
                            showDialog.value = true
                        }
                    )
                }
                if (showDialog.value) {
                    SaveDrawingDialog(
                        onDismiss = { showDialog.value = false },
                        onSave = { name ->
                            lifecycleScope.launch {
                                // Save the drawing with the name
                                val displayMetrics = this@MainActivity.resources.displayMetrics
                                val bitmap = captureBitmapFromCanvas(
                                    pathList,
                                    displayMetrics.widthPixels,
                                    (displayMetrics.heightPixels * 0.7F).toInt() // Adjust to match DrawCanvas height
                                )

                                saveBitmapToGallery(this@MainActivity, bitmap, "$name.png")

                                val imageByteArray = bitmapToByteArray(bitmap)
                                val picture = Picture(name = name, image = imageByteArray)
                                val pictureDao = DrawerDatabase
                                    .getDatabase(this@MainActivity)
                                    .pictureDao()
                                pictureDao.insert(picture)
                                showDialog.value = false
                            }
                        }
                    )
                }
            }
        }
    }
    private fun captureBitmapFromCanvas(pathList: List<PathData>, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        pathList.forEach { pathData ->
            val paint = android.graphics.Paint().apply {
                color = pathData.color.toArgb()
                strokeWidth = pathData.lineWidth
                style = android.graphics.Paint.Style.STROKE
                strokeCap = android.graphics.Paint.Cap.ROUND
                alpha = ((100 - pathData.transparency) / 100 * 255).toInt()
            }
            canvas.drawPath(pathData.path.asAndroidPath(), paint)
        }

        return bitmap
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String) {
        val contentResolver = context.contentResolver
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val imageUri = contentResolver.insert(imageCollection, contentValues)

        imageUri?.let { uri ->
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(uri, contentValues, null, null)
            }
            Log.d("mylog", "Bitmap saved to gallery: $uri")
        } ?: run {
            Log.e("mylog", "Failed to save bitmap to gallery")
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}

@Composable
fun DrawCanvas(
    pathData: MutableState<PathData>,
    pathList: SnapshotStateList<PathData>,
    isBackPressed: MutableState<Boolean>
) {
    var tempPath by remember { mutableStateOf<Path?>(null) }
    val tempPathList = remember { mutableStateListOf<PathData>() }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.70f)
            .pointerInput(true) {
                detectDragGestures(
                    onDragStart = {
                        tempPath = Path()
                        tempPathList.clear()  // Clear the tempPathList when starting a new path
                        isBackPressed.value = false  // Reset the flag when drawing starts
                    },
                    onDragEnd = {
                        tempPath?.let {
                            pathList.add(pathData.value.copy(path = it))
                        }
                        tempPath = null
                        tempPathList.clear()
                    }
                ) { change, dragAmount ->
                    tempPath?.moveTo(
                        change.position.x - dragAmount.x,
                        change.position.y - dragAmount.y
                    )
                    tempPath?.lineTo(
                        change.position.x,
                        change.position.y
                    )
                    tempPathList.clear()
                    tempPathList.add(pathData.value.copy(path = tempPath!!))
                }
            }
    ) {
        pathList.forEach { pathData ->
            drawPath(
                path = pathData.path,
                color = pathData.color,
                alpha = (100 - pathData.transparency) / 100f,
                style = Stroke(
                    width = pathData.lineWidth,
                    cap = StrokeCap.Round
                )
            )
        }
        tempPathList.forEach { pathData ->
            drawPath(
                pathData.path,
                color = pathData.color,
                alpha = (100 - pathData.transparency) / 100f,
                style = Stroke(
                    width = pathData.lineWidth,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}