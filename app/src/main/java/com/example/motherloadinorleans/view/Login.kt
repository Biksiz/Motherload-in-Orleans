package com.example.motherloadinorleans.view

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigation.NavController
import com.example.motherloadinorleans.R
import com.example.motherloadinorleans.model.UserRepo
import com.example.motherloadinorleans.ui.theme.Purple500

@Composable
fun LoginPage(navController: NavController) {
    val repository = UserRepo.getInstance()

    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val emailVal = remember { mutableStateOf("") }
    val passwordVal = remember { mutableStateOf("") }

    val passwordVisiblity = remember { mutableStateOf(false) }

    val login_msg_username_empty = stringResource(id = R.string.login_msg_username_empty)
    val login_msg_password_empty = stringResource(id = R.string.login_msg_password_empty)
    val login_msg_login_error = stringResource(id = R.string.login_msg_login_error)
    val login_msg_login_success = stringResource(id = R.string.login_msg_login_success)

    BackHandler {
        /* rien car on ne veut pas que l'utilisateur puisse revenir en arrière */
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = White), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.background(White),
                contentAlignment = Alignment.TopCenter
            ) {
                Image(
                    modifier = Modifier
                        .width(400.dp)
                        .height(200.dp)
                        .padding(10.dp),
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Login Image",
                    contentScale = ContentScale.Fit,
                )
            }

            Spacer(modifier = Modifier.padding(20.dp))


            Scaffold(modifier = Modifier.fillMaxSize(), scaffoldState = scaffoldState) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(25.dp))
                        .background(White)
                        .padding(10.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.login_text_login),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )

                    Spacer(modifier = Modifier.padding(20.dp))


                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        OutlinedTextField(
                            value = emailVal.value,
                            onValueChange = { emailVal.value = it },
                            label = { Text(text = stringResource(id = R.string.login_text_id), color = Black) },
                            placeholder = { Text(text = stringResource(id = R.string.login_text_id), color = Black) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(0.8f),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = Black, textColor = Black
                            )
                        )

                        OutlinedTextField(
                            value = passwordVal.value,
                            onValueChange = { passwordVal.value = it },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = Black, textColor = Black
                            ),
                            trailingIcon = {
                                IconButton(onClick = {
                                    passwordVisiblity.value = !passwordVisiblity.value
                                }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_baseline_remove_red_eye_24),
                                        contentDescription = stringResource(id = R.string.login_text_password),
                                        tint = if (passwordVisiblity.value) Purple500 else Color.Gray
                                    )
                                }
                            },
                            label = { Text(text = stringResource(id = R.string.login_text_password), color = Black) },
                            placeholder = { Text(text = stringResource(id = R.string.login_text_password), color = Black) },
                            singleLine = true,
                            visualTransformation = if (passwordVisiblity.value) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )

                        Spacer(modifier = Modifier.padding(20.dp))

                        Button(
                            colors = ButtonDefaults.buttonColors(backgroundColor = White),
                            onClick = {
                                when {
                                    emailVal.value.isEmpty() -> {
                                        Toast.makeText(
                                            context,
                                            login_msg_username_empty,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    passwordVal.value.isEmpty() -> {
                                        Toast.makeText(
                                            context,
                                            login_msg_password_empty,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    else -> {
                                        repository.connexion(emailVal.value, passwordVal.value) { connexionReussie ->
                                            if (connexionReussie) {
                                                navController.navigate("menu_page")
                                                Toast.makeText(
                                                    context,
                                                    login_msg_login_success,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    login_msg_login_error,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                }

                            }, modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp)
                        ) {
                            Text(text = stringResource(id = R.string.login_text_signin), fontSize = 20.sp,color = Black)
                        }


                    }
                }
            }

        }
    }
}

@Preview
@Composable
fun PreviewLoginPage() {
    LoginPage(navController = NavController(LocalContext.current))
}