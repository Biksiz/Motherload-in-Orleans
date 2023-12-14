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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.motherloadinorleans.ui.theme.MotherLoadInOrleansComposeTheme
import com.example.motherloadinorleans.view.ChangeName
import com.example.motherloadinorleans.view.LoginPage
import com.example.motherloadinorleans.view.Menu
import com.example.motherloadinorleans.view.Settings
import com.example.motherloadinorleans.view.Store
import com.example.motherloadinorleans.view.Sale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MotherLoadInOrleansComposeTheme {
                navigatePage()
            }
        }
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
}

@Composable
fun navigatePage() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login_page",
        builder = {
            composable("login_page", content = { LoginPage(navController = navController) })
            composable("menu_page", content = { Menu(navController = navController) })
            composable("settings_page", content = { Settings(navController = navController) })
            composable("change_name_page", content = { ChangeName(navController = navController) })
            composable("store_page", content = { Store(navController = navController) })
            composable("sale_page", content = { Store(navController = navController) })
        }
    )
}


