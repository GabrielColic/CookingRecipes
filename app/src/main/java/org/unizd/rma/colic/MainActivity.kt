package org.unizd.rma.colic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.unizd.rma.colic.data.CookingRecipeRepository
import org.unizd.rma.colic.ui.view.CookingRecipeViewModel
import org.unizd.rma.colic.ui.view.RecipeEditScreen
import org.unizd.rma.colic.ui.view.RecipeListScreen
import org.unizd.rma.colic.ui.view.VMFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as App
        val repo: CookingRecipeRepository = app.repo
        val vm = ViewModelProvider(this, VMFactory(repo))[CookingRecipeViewModel::class.java]

        setContent {
            val nav = rememberNavController()
            NavHost(navController = nav, startDestination = "list") {
                composable("list") {
                    RecipeListScreen(vm) { id -> nav.navigate("edit/${id ?: -1}") }
                }
                composable(
                    "edit/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { backStackEntry ->
                    val idArg = backStackEntry.arguments?.getInt("id") ?: -1
                    val id = if (idArg == -1) null else idArg
                    RecipeEditScreen(vm, id) { nav.popBackStack() }
                }
            }
        }
    }
}
