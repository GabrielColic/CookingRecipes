package org.unizd.rma.colic.ui.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import org.unizd.rma.colic.data.model.CookingRecipe
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.material3.HorizontalDivider
import androidx.core.graphics.createBitmap

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
                    leadingContent = {
                        Image(
                            bitmap = r.image.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(56.dp)
                        )
                    },
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

            OutlinedButton(onClick = { pickImage.launch("image/*") }) {
                Text(if (image == null) "Pick Image" else "Change Image")
            }
            image?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(160.dp))
            }
        }
    }
}

private fun uriToBitmap(context: Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= 28) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }
}

private fun Date.format(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(this)

private fun placeholderBitmap(title: String): Bitmap {
    val size = 256
    val bmp = createBitmap(size, size)
    val canvas = Canvas(bmp)

    canvas.drawColor(Color.LTGRAY)

    val initial = title.firstOrNull()?.uppercaseChar()?.toString() ?: "R"
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = size * 0.45f
        textAlign = Paint.Align.CENTER
    }
    val x = size / 2f
    val y = (size / 2f) - ((paint.descent() + paint.ascent()) / 2f)
    canvas.drawText(initial, x, y, paint)

    return bmp
}

