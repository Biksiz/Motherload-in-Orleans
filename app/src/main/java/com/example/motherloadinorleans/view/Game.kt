package com.example.motherloadinorleans.view

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.motherloadinorleans.R
import com.example.motherloadinorleans.model.GameRepo
import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.delay
import kotlin.math.*

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val startPoint = Location("locationA")
    startPoint.latitude = lat1
    startPoint.longitude = lon1

    val endPoint = Location("locationB")
    endPoint.latitude = lat2
    endPoint.longitude = lon2

    return startPoint.distanceTo(endPoint) // La distance est en mètres
}

fun determinerDirection(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Pair<ImageVector, String> {
    val dLon = Math.toRadians(lon2 - lon1)

    val y = sin(dLon) * cos(Math.toRadians(lat2))
    val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) - sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLon)
    val bearing = atan2(y, x)

    val direction = (Math.toDegrees(bearing) + 360) % 360 // La direction est en degrés

    return when {
        direction in 45.0..135.0 -> Pair(Icons.Filled.KeyboardArrowRight, "E")
        direction in 135.0..225.0 -> Pair(Icons.Filled.KeyboardArrowDown, "S")
        direction in 225.0..315.0 -> Pair(Icons.Filled.KeyboardArrowLeft, "O")
        else -> Pair(Icons.Filled.KeyboardArrowUp, "N")
    }
}

@Composable
fun Game( navController: NavController, gameRepo: GameRepo) {

    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val session = sharedPref.getString("session", "") ?: ""
    val signature = sharedPref.getString("signature", "") ?: ""
    val name = sharedPref.getString("name", "") ?: ""

    val scaffoldState = rememberScaffoldState()
    val profondeur = gameRepo.profondeur.observeAsState().value
    val position = gameRepo.position.observeAsState().value
    val latitude = position?.first
    val longitude = position?.second
    val niveauPioche = gameRepo.niveauPickaxe.observeAsState().value

    var voisins = gameRepo.voisin.observeAsState().value ?: listOf()

    val isButtonEnabled = remember { mutableStateOf(true) }
    val startTimer = remember { mutableStateOf(false) }

    LaunchedEffect(startTimer.value) {
        if (startTimer.value) {
            delay(10000) // Attendre 10 secondes
            isButtonEnabled.value = true
            startTimer.value = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("menu_page")
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.parameters_button_return))
                    }
                },
                actions = {
                    Text(
                        text = "Niveau de la pioche : $niveauPioche",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            )
        },
        scaffoldState = scaffoldState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {}
        Box(modifier = Modifier.fillMaxSize()){
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(it),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Jeu",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                )
                Text(
                    text = "Profondeur du trou : $profondeur m",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                Spacer(modifier = Modifier.padding(16.dp))
                Text(
                    text = "Lat: $latitude",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                Text(
                    text = "Lon: $longitude",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                Spacer(modifier = Modifier.padding(16.dp))

                Button(
                    onClick =
                    {
                        if (isButtonEnabled.value) {
                            isButtonEnabled.value = false
                            startTimer.value = true

                            gameRepo.creuser(session, signature, longitude, latitude) { success ->
                                if (success == "OK") {
                                    Toast.makeText(
                                        context,
                                        "Creusement réussi !",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (success == "KO - TOO FAST") {
                                    Toast.makeText(
                                        context,
                                        "Attendez un peu pour creuser !",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (success == "KO  - BAD PICKAXE") {
                                    Toast.makeText(
                                        context,
                                        "Votre pioche est trop faible !",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(context, "Erreur : $success", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                        else {
                            Toast.makeText(context, "Patientez un peu pour creuser !", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isButtonEnabled.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 50.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Creuser",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Voisins les plus proches :",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Blue,
                        )

                        LazyColumn{
                            val cinqPremiersVoisins = voisins
                                .filter { voisin -> voisin.name != name }
                                .sortedBy { voisin ->
                                    calculateDistance(latitude!!.toDouble(), longitude!!.toDouble(), voisin.position.first!!.toDouble(), voisin.position.second!!.toDouble())
                                }
                            .take(5)

                            items( cinqPremiersVoisins.size ){ index  ->
                                val voisin = cinqPremiersVoisins[index]
                                val distance = calculateDistance(latitude!!.toDouble(), longitude!!.toDouble(), voisin.position.first!!.toDouble(), voisin.position.second!!.toDouble())
                                val (directionIcon, directionLabel) = determinerDirection(latitude!!.toDouble(), longitude!!.toDouble(), voisin.position.first!!.toDouble(), voisin.position.second!!.toDouble())

                                Row {
                                    Column {
                                        Text(
                                            text = "Nom : ${voisin.name} , Distance : $distance m",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                        )
                                        Row {
                                            Text(
                                                text = "Direction : $directionLabel",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black,
                                            )
                                            Icon(
                                                directionIcon,
                                                contentDescription = "Direction",
                                                modifier = Modifier.size(24.dp),
                                                tint = Color.Black
                                            )
                                        }
                                        if (index != cinqPremiersVoisins.size - 1)
                                        {
                                            Divider(color = Color.Gray, thickness = 1.dp)
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}




@Preview
@Composable
fun PreviewGamePage() {
    Game(navController = NavController(LocalContext.current), gameRepo = GameRepo.instance)
}