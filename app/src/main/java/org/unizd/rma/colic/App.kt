package org.unizd.rma.colic

import android.app.Application
import androidx.room.Room
import org.unizd.rma.colic.data.*

class App : Application() {
    lateinit var db: AppDatabase
        private set
    lateinit var repo: CookingRecipeRepository
        private set

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDatabase::class.java, "recipes.db").build()
        repo = CookingRecipeRepository(db.recipeDao())
    }
}
