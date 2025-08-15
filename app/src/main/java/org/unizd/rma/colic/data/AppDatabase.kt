package org.unizd.rma.colic.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.unizd.rma.colic.data.dao.CookingRecipeDao
import org.unizd.rma.colic.data.model.CookingRecipe

@Database(entities = [CookingRecipe::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): CookingRecipeDao
}
