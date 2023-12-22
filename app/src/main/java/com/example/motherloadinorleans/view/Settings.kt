package com.example.motherloadinorleans.view

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.ExitToApp
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.example.motherloadinorleans.R
import com.example.motherloadinorleans.model.UserRepo


@Composable
fun Settings(navController: NavController) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    var showDialog by remember { mutableStateOf(false) }

    val repository = UserRepo.getInstance()

    val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val session = sharedPref.getString("session", "") ?: ""
    val signature = sharedPref.getString("signature", "") ?: ""

    val signOutMessage = stringResource(id = R.string.parameters_msg_signout)

    val parameters_msg_reinitialize_success = stringResource(id = R.string.parameters_msg_reinitialize_success)
    val parameters_msg_reinitialize_error = stringResource(id = R.string.parameters_msg_reinitialize_error)

    Scaffold(
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
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = White)
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.parameters_text_parameters),
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
                        Icon(Icons.Filled.AccountCircle, contentDescription = stringResource(id = R.string.parameters_text_changename))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.parameters_text_changename), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }


                Divider(color = Color.Gray, thickness = 1.dp)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("login_page") {
                                popUpTo("login_page") { inclusive = true }
                            }
                            with (sharedPref.edit()) {
                                putString("username", "")
                                putString("password", "")
                                putString("session", "")
                                putString("signature", "")
                                putString("name", "")
                                apply()
                            }
                            repository.clear_user()
                            repository.reconnexionFailed.postValue(true)
                            Toast.makeText(context, signOutMessage, Toast.LENGTH_LONG).show()
                           },
                    color = Color.Transparent,
                    elevation = 0.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = stringResource(id = R.string.parameters_text_signout))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.parameters_text_signout), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                        Icon(Icons.Filled.Close, contentDescription = stringResource(id = R.string.parameters_text_reinitializeaccount), tint = Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.parameters_text_reinitializeaccount), color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                if (showDialog) {
                    Dialog(onDismissRequest = { showDialog = false }) {
                        // Custom layout for dialog
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F1F1),
                            border = BorderStroke(2.dp, Color(0xFF333333)),
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = stringResource(id = R.string.parameters_text_reinitializeaccount), fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = stringResource(id = R.string.parameters_dialog_areYouSure), color = Color.Black)
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = {
                                                repository.reinitialize_account(session, signature) { success ->
                                                    if (success) {
                                                        showDialog = false
                                                        Toast.makeText(
                                                            context,
                                                            parameters_msg_reinitialize_success,
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        navController.navigate("login_page") {
                                                            popUpTo("menu_page") { inclusive = true }
                                                        }
                                                    } else {
                                                        showDialog = false
                                                        Toast.makeText(
                                                            context,
                                                            parameters_msg_reinitialize_error,
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                }
                                            },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                                ) {
                                    Text(stringResource(id = R.string.yes), color = Color.White)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showDialog = false },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE))
                                ) {
                                    Text(stringResource(id = R.string.no), color = Color.White)
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