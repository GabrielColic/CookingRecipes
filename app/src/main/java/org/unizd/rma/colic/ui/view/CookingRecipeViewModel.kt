package org.unizd.rma.colic.ui.view

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.unizd.rma.colic.data.model.CookingRecipe
import org.unizd.rma.colic.data.CookingRecipeRepository
import java.util.Date

class CookingRecipeViewModel(private val repo: CookingRecipeRepository) : ViewModel() {
    val items: StateFlow<List<CookingRecipe>> = repo.list()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun byId(id: Int) = repo.get(id)

    fun create(title: String, author: String, difficulty: String, date: Date, image: Bitmap) =
        viewModelScope.launch { repo.create(CookingRecipe(title = title, author = author, difficulty = difficulty, dateAdded = date, image = image)) }

    fun update(model: CookingRecipe) = viewModelScope.launch { repo.edit(model) }
    fun delete(model: CookingRecipe) = viewModelScope.launch { repo.remove(model) }
}

@Suppress("UNCHECKED_CAST")
class VMFactory(private val repo: CookingRecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CookingRecipeViewModel(repo) as T
}
