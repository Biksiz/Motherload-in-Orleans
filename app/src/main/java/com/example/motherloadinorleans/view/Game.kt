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

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val startPoint = Location("locationA")
    startPoint.latitude = lat1
    startPoint.longitude = lon1

    val endPoint = Location("locationB")
    endPoint.latitude = lat2
    endPoint.longitude = lon2

    return startPoint.distanceTo(endPoint) // La distance est en mètres
}


@Composable
fun Game( navController: NavController, gameRepo: GameRepo) {

    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val session = sharedPref.getString("session", "") ?: ""
    val signature = sharedPref.getString("signature", "") ?: ""

    val scaffoldState = rememberScaffoldState()
    val profondeur = gameRepo.profondeur.observeAsState().value
    val position = gameRepo.position.observeAsState().value
    val latitude = position?.first
    val longitude = position?.second
    val niveauPioche = gameRepo.niveauPickaxe.observeAsState().value

    var voisins = gameRepo.voisin.observeAsState().value ?: listOf()


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
                        text = "Niveau pioche : $niveauPioche",
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
        ) {
            Text(
                text = "Jeu",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
            )
        }
        Box(modifier = Modifier.fillMaxSize()){
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(it),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Profondeur : $profondeur",
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

                // Bouton pour "creuser"
                Button(
                    onClick =
                    {
                        gameRepo.creuser(session, signature, longitude, latitude){ success ->
                            if (success == "OK"){
                                Toast.makeText(context, "creusement réussi !", Toast.LENGTH_SHORT).show()
                            }else if (success == "KO - TOO FAST"){
                                Toast.makeText(context, "TOO Fast !", Toast.LENGTH_SHORT).show()
                            }else if(success == "KO - BAD PICKAXE"){
                                Toast.makeText(context, "Mauvaise pioche !", Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(context, "$success", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
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
                // Liste des voisins
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Voisins",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Blue,
                        )

                        LazyColumn{
                            val cinqPremiersVoisins = voisins.take(5)
                            items( cinqPremiersVoisins.size ){ index  ->
                                Row {
                                    Text(
                                        text = "Nom : ${voisins[index].name} , Distance : ${calculateDistance(latitude!!.toDouble(), longitude!!.toDouble(), voisins[index].position.first!!.toDouble(), voisins[index].position.second!!.toDouble())} m",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                    )
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