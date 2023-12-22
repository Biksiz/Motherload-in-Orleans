package com.example.motherloadinorleans.view

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motherloadinorleans.R
import com.example.motherloadinorleans.model.UserRepo
import com.example.motherloadinorleans.ui.theme.Purple500

@Composable
fun ChangeName(navController: NavController) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val nameVal = remember { mutableStateOf("") }

    val repository = UserRepo.getInstance()

    val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val session = sharedPref.getString("session", "") ?: ""
    val signature = sharedPref.getString("signature", "") ?: ""

    val changename_msg_success = stringResource(id = R.string.changename_msg_success)
    val changename_msg_error = stringResource(id = R.string.changename_msg_error)
    val changename_msg_name_empty = stringResource(id = R.string.changename_msg_name_empty)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("settings_page")
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
                    text = stringResource(id = R.string.changename_text_changename),
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    color = Black,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                TextField(
                    value = nameVal.value,
                    onValueChange = { nameVal.value = it },
                    label = { Text(stringResource(id = R.string.changename_edit_newname)) },
                    placeholder = { Text(stringResource(id = R.string.changename_edit_newname)) },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Purple500,
                        unfocusedIndicatorColor = Color.Black,
                        disabledIndicatorColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 20.dp, bottom = 0.dp, end = 20.dp)
                )
                Spacer(modifier = Modifier.padding(20.dp))
                Button(
                    onClick = {
                      when {
                          nameVal.value.isEmpty() -> {
                              Toast.makeText(
                                  context,
                                  changename_msg_name_empty,
                                  Toast.LENGTH_LONG
                              ).show()
                          }
                          else -> {
                              repository.change_username(session, signature, nameVal.value) { success ->
                                  if (success) {
                                      with(sharedPref.edit()) {
                                          putString("name", nameVal.value)
                                          apply()
                                      }
                                      navController.navigate("settings_page")
                                      Toast.makeText(
                                          context,
                                          changename_msg_success,
                                          Toast.LENGTH_LONG
                                      ).show()
                                  } else {
                                      Toast.makeText(
                                          context,
                                          changename_msg_error,
                                          Toast.LENGTH_LONG
                                      ).show()
                                  }
                              }
                          }
                      }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 0.dp, bottom = 20.dp, end = 20.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Purple500,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = stringResource(id = R.string.changename_text_changename))
                }
            }
        }
    )
}



@Preview
@Composable
fun ChangeNamePreview() {
    ChangeName(navController = NavController(LocalContext.current))
}