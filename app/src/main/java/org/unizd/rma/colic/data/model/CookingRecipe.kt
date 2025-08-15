package org.unizd.rma.colic.data.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "recipes")
data class CookingRecipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val difficulty: String,
    val dateAdded: Date,
    val image: Bitmap
)