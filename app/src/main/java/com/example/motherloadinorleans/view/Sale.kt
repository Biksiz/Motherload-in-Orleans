package com.example.motherloadinorleans.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motherloadinorleans.R

@Composable
fun Sale(navController: NavController) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()

    Scaffold (
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("menu_page")
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.parameters_button_return))
                    }
                },
                backgroundColor = Color.White,
                contentColor = Color.Black,
                elevation = 0.dp
            )
        },
        content = {
            Column(
                modifier= Modifier.fillMaxSize(),
                horizontalAlignment= Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement= Arrangement.Center,
                    modifier= Modifier.padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            navController.navigate("store_page")
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF005CE7),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Acheter")
                    }

                    Spacer(modifier= Modifier.width(1.dp)) // Espace optionnel entre les boutons

                    Button(
                        onClick = {
                            /*rien*/
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF4C9DFF),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Text("Vendre")
                    }
                }
                Text(text = "Vendre: à compléter")
            }
        }
    )
}

@Preview
@Composable
fun SalePreview() {
    Sale(navController = NavController(LocalContext.current))
}