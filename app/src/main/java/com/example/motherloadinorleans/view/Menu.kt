package com.example.motherloadinorleans.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.motherloadinorleans.R



@Composable
fun Menu(navController: NavController) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("settings_page")
                    }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Paramètres")
                    }
                },
                backgroundColor = Color.White,
                contentColor = Color.Black,
                elevation = 0.dp
            )
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = White),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                /*Navigation page*/
                            },
                            modifier = Modifier.weight(1f)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.ShoppingCart, contentDescription = "Magasin")
                                Text("Magasin")
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                /*Navigation page*/
                            },
                            modifier = Modifier.weight(1f)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Build, contentDescription = "Atelier")
                                Text("Atelier")
                            }
                        }
                    }

                    Button(
                        onClick ={
                            /*Navigation page*/
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red, contentColor = Color.White)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Jouer")
                            Text("Jouer")
                        }
                    }
                }
            }
        }
    )
}

@Preview
@Composable
fun MenuPreview() {
    Menu(navController = NavController(LocalContext.current))
}