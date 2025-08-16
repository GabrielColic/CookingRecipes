package org.unizd.rma.colic.ui.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import org.unizd.rma.colic.data.model.CookingRecipe
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.saveable.rememberSaveable
import java.io.File

@Composable
fun RecipeListScreen(
    vm: CookingRecipeViewModel,
    onEdit: (Int?) -> Unit
) {
    val items by vm.items.collectAsStateWithLifecycle()
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onEdit(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { p ->
        LazyColumn(Modifier.padding(p).fillMaxSize()) {
            items(items.size) { i ->
                val r = items[i]
                ListItem(
                    headlineContent = { Text(r.title) },
                    supportingContent = { Text("${r.author} • ${r.difficulty} • ${r.dateAdded.format()}") },
                    leadingContent = { SafeRecipeThumbnail(r.image, size = 56.dp) },
                    trailingContent = {
                        IconButton(onClick = { vm.delete(r) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    },
                    modifier = Modifier.clickable { onEdit(r.id) }
                )
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
fun RecipeEditScreen(
    vm: CookingRecipeViewModel,
    recipeId: Int?,
    onDone: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    val difficulties = listOf("Easy", "Medium", "Hard")
    var difficulty by remember { mutableStateOf(difficulties.first()) }
    var dateAdded by remember { mutableStateOf(Date()) }
    var image by remember { mutableStateOf<Bitmap?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current

    val pickImage = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let { image = uriToBitmap(context, it) }
    }

    var cameraTempPath by rememberSaveable { mutableStateOf<String?>(null) }
    val takePicture = rememberLauncherForActivityResult(TakePicture()) { success: Boolean ->
        val path = cameraTempPath
        if (success && path != null) {
            val file = File(path)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            runCatching {
                image = uriToBitmap(context, uri)
            }.onFailure {
                image = placeholderBitmap(title)
            }
            file.delete()
        }
        cameraTempPath = null
    }

    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = dateAdded.time,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis <= System.currentTimeMillis()
        }
    )
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(recipeId) {
        if (recipeId != null) {
            vm.byId(recipeId).collectLatest { r ->
                r?.let {
                    title = it.title
                    author = it.author
                    difficulty = it.difficulty
                    dateAdded = it.dateAdded
                    dateState.selectedDateMillis = it.dateAdded.time
                    image = it.image
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val bmp = image ?: placeholderBitmap(title)
                val millis = dateState.selectedDateMillis ?: dateAdded.time
                if (recipeId == null) {
                    vm.create(title.trim(), author.trim(), difficulty, Date(millis), bmp)
                } else {
                    vm.update(
                        CookingRecipe(
                            id = recipeId,
                            title = title.trim(),
                            author = author.trim(),
                            difficulty = difficulty,
                            dateAdded = Date(millis),
                            image = bmp
                        )
                    )
                }
                onDone()
            }) { Icon(Icons.Default.Save, contentDescription = "Save") }
        }
    ) { p ->
        Column(
            Modifier.padding(p).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Author") },
                modifier = Modifier.fillMaxWidth()
            )

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = difficulty,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Difficulty") },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    difficulties.forEach { opt ->
                        DropdownMenuItem(text = { Text(opt) }, onClick = { difficulty = opt; expanded = false })
                    }
                }
            }

            OutlinedTextField(
                value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date(dateState.selectedDateMillis ?: dateAdded.time)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date Created") },
                trailingIcon = { TextButton(onClick = { showDatePicker = true }) { Text("Pick") } },
                modifier = Modifier.fillMaxWidth()
            )
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } },
                    dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
                ) {
                    DatePicker(state = dateState)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = {
                    val dir = File(context.cacheDir, "images").apply { mkdirs() }
                    val file = File(dir, "shot_${System.currentTimeMillis()}.jpg")
                    cameraTempPath = file.absolutePath  // ← save path in a saveable state
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    takePicture.launch(uri)
                }) {
                    Text("Take Photo")
                }
                OutlinedButton(onClick = { pickImage.launch("image/*") }) {
                    Text(if (image == null) "Pick Image" else "Change Image")
                }
            }

            image?.let { SafeRecipeThumbnail(it, size = 160.dp) }
        }
    }
}

private fun uriToBitmap(context: Context, uri: Uri): Bitmap {
    val maxDim = 1024

    val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use {
        android.graphics.BitmapFactory.decodeStream(it, null, bounds)
    }

    val inSample = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDim, maxDim)
    val opts = android.graphics.BitmapFactory.Options().apply {
        inJustDecodeBounds = false
        inSampleSize = inSample
        inPreferredConfig = Bitmap.Config.RGB_565
    }
    val sampled = context.contentResolver.openInputStream(uri)?.use {
        android.graphics.BitmapFactory.decodeStream(it, null, opts)
    } ?: error("Decode failed for $uri")

    val oriented = applyExifOrientation(context, uri, sampled)

    val w = oriented.width
    val h = oriented.height
    val scale = maxOf(w.toFloat() / maxDim, h.toFloat() / maxDim, 1f)
    return if (scale > 1f) {
        val nw = (w / scale).toInt()
        val nh = (h / scale).toInt()
        oriented.scale(nw, nh).also { if (it !== oriented) oriented.recycle() }
    } else oriented
}

private fun applyExifOrientation(context: Context, uri: Uri, bmp: Bitmap): Bitmap {
    val exif = runCatching {
        context.contentResolver.openInputStream(uri)?.use { ExifInterface(it) }
    }.getOrNull() ?: return bmp

    val o = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    val m = Matrix()
    when (o) {
        ExifInterface.ORIENTATION_ROTATE_90       -> m.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180      -> m.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270      -> m.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.preScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL   -> m.preScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE       -> { m.postRotate(90f);  m.preScale(-1f, 1f) }
        ExifInterface.ORIENTATION_TRANSVERSE      -> { m.postRotate(270f); m.preScale(-1f, 1f) }
        else -> return bmp
    }
    return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true).also {
        if (it !== bmp) bmp.recycle()
    }
}

private fun calculateInSampleSize(
    width: Int,
    height: Int,
    reqWidth: Int,
    reqHeight: Int
): Int {
    var inSampleSize = 1
    var halfW = width / 2
    var halfH = height / 2
    while (halfW / inSampleSize >= reqWidth && halfH / inSampleSize >= reqHeight) {
        inSampleSize *= 2
    }
    return maxOf(1, inSampleSize)
}

@Composable
private fun SafeRecipeThumbnail(bmp: Bitmap?, size: Dp = 56.dp) {
    if (bmp == null) { Box(Modifier.size(size)) { } ; return }
    runCatching {
        Image(bitmap = bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.size(size))
    }.onFailure { Box(Modifier.size(size)) { } }
}

private fun Date.format(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(this)

private fun placeholderBitmap(title: String): Bitmap {
    val size = 256
    val bmp = createBitmap(size, size)
    val canvas = android.graphics.Canvas(bmp)
    canvas.drawColor(android.graphics.Color.LTGRAY)
    val initial = title.firstOrNull()?.uppercaseChar()?.toString() ?: "R"
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.DKGRAY
        textSize = size * 0.45f
        textAlign = android.graphics.Paint.Align.CENTER
    }
    val x = size / 2f
    val y = (size / 2f) - ((paint.descent() + paint.ascent()) / 2f)
    canvas.drawText(initial, x, y, paint)
    return bmp
}
