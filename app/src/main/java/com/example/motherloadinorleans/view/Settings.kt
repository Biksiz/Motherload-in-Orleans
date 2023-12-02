package com.example.motherloadinorleans.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog


@Composable
fun Settings(navController: NavController) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("menu_page")
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                backgroundColor = Color.White,
                contentColor = Color.Black,
                elevation = 0.dp
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = White)
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp),
            ) {
                Text(
                    text = "Paramètres",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("change_name_page") },
                    color = Color.Transparent,
                    elevation = 0.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Changer son nom")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Changer son nom", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }


                Divider(color = Color.Gray, thickness = 1.dp)


                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDialog = true },
                    color = Color.Transparent,
                    elevation = 0.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Réinitialiser compte", tint = Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Réinitialiser compte", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                if (showDialog) {
                    Dialog(onDismissRequest = { showDialog = false }) {
                        // Custom layout for dialog
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F1F1), // Light gray
                            border = BorderStroke(2.dp, Color(0xFF333333)), // Dark gray
                            modifier = Modifier
                                .fillMaxWidth(0.8f) // Fill 80% of the screen width
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "Réinitialiser le compte", fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = "Êtes-vous sûr ?", color = Color.Black)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { /* Réinitialiser le compte */ },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                                ) {
                                    Text("Oui", color = Color.White)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showDialog = false },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE))
                                ) {
                                    Text("Non", color = Color.White)
                                }
                            }
                        }
                    }
                }

            }
        }
    )
}




@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Settings(navController = NavController(LocalContext.current))
}