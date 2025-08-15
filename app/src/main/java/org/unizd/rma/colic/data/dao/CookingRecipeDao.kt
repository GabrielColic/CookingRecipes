package org.unizd.rma.colic.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.unizd.rma.colic.data.model.CookingRecipe

@Dao
interface CookingRecipeDao {
    @Query("SELECT * FROM recipes ORDER BY title COLLATE NOCASE")
    fun getAll(): Flow<List<CookingRecipe>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getById(id: Int): Flow<CookingRecipe?>

    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insert(item: CookingRecipe): Long

    @Update
    suspend fun update(item: CookingRecipe)

    @Delete
    suspend fun delete(item: CookingRecipe)

    @Upsert
    suspend fun upsert(item: CookingRecipe)
}