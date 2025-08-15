package org.unizd.rma.colic.data

import kotlinx.coroutines.flow.Flow
import org.unizd.rma.colic.data.dao.CookingRecipeDao
import org.unizd.rma.colic.data.model.CookingRecipe

class CookingRecipeRepository(private val dao: CookingRecipeDao) {
    fun list(): Flow<List<CookingRecipe>> = dao.getAll()
    fun get(id: Int): Flow<CookingRecipe?> = dao.getById(id)
    suspend fun create(r: CookingRecipe) = dao.insert(r)
    suspend fun edit(r: CookingRecipe) = dao.update(r)
    suspend fun remove(r: CookingRecipe) = dao.delete(r)
    suspend fun upsert(r: CookingRecipe) = dao.upsert(r)
}
