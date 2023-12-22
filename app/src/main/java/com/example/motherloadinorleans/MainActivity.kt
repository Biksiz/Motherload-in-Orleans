package com.example.motherloadinorleans

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.motherloadinorleans.model.GameRepo
import com.example.motherloadinorleans.model.StoreRepo
import com.example.motherloadinorleans.model.UserRepo
import com.example.motherloadinorleans.model.WorkshopRepo
import com.example.motherloadinorleans.ui.theme.MotherLoadInOrleansComposeTheme
import com.example.motherloadinorleans.view.Album
import com.example.motherloadinorleans.view.ChangeName
import com.example.motherloadinorleans.view.LoginPage
import com.example.motherloadinorleans.view.Menu
import com.example.motherloadinorleans.view.Settings
import com.example.motherloadinorleans.view.Store
import com.example.motherloadinorleans.view.Sale
import com.example.motherloadinorleans.view.Game
import com.example.motherloadinorleans.view.Upgreades
import kotlinx.coroutines.*

class MainActivity : ComponentActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MotherLoadInOrleansComposeTheme {
                navigatePage()
            }
        }

        lancerRoutineDeReconnexion()
    }

    override fun onResume() {
        super.onResume()
        // L'activity repasse en avant plan : on relance la mise à jour des messages
    }

    override fun onPause() {
        // L'activity passe en arrière-plan : on coupe la mise à jour des messages :
        // Pour ce faire, on vire de la file d'attente le job qui était posté.
        super.onPause()
    }

    private fun lancerRoutineDeReconnexion() {
        val userRepo = UserRepo.getInstance() // Assurez-vous d'obtenir l'instance correctement

        launch {
            while (isActive) {
                delay(4 * 60 * 1000) // Attendre 4 minutes

                val currentUser = userRepo.get_user()

                userRepo.reconnexion(currentUser.username, currentUser.password) { success ->
                    if (!success) {
                        userRepo.reconnexionFailed.postValue(true)
                    }
                    else {
                        userRepo.reconnexionFailed.postValue(false)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}

@Composable
fun navigatePage() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login_page",
        builder = {
            composable("menu_page", content = { Menu(navController = navController) })
            composable("settings_page", content = { Settings(navController = navController) })
            composable("change_name_page", content = { ChangeName(navController = navController) })
            composable("store_page", content = { Store(navController = navController, storeRepo = StoreRepo.instance) })
            composable("sale_page", content = { Sale(navController = navController, storeRepo = StoreRepo.instance) })
            composable("login_page", content = { LoginPage(navController = navController) })
            composable("game_page", content = { Game(navController = navController, gameRepo = GameRepo.instance) })
            composable("album_page", content = { Album(navController = navController, workshopRepo = WorkshopRepo.instance) })
            composable("upgread_page", content = { Upgreades(navController = navController, workshopRepo = WorkshopRepo.instance) })
        }
    )
}


