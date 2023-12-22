package com.example.motherloadinorleans.view

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

fun determinerDirection(lat1: Double, lon1: Double, lat2: Double, lon2: Double, azimuth: Float): Pair<ImageVector, String> {
    val dLon = Math.toRadians(lon2 - lon1)

    val y = sin(dLon) * cos(Math.toRadians(lat2))
    val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) - sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLon)
    val bearing = (atan2(y, x) + 2 * Math.PI) % (2 * Math.PI)

    val azimuthInRadians = Math.toRadians(azimuth.toDouble())
    val adjustedBearing = (bearing - azimuthInRadians + 2 * Math.PI) % (2 * Math.PI)

    val adjustedDirection = Math.toDegrees(adjustedBearing)

    return when {
        adjustedDirection in 45.0..135.0 -> Pair(Icons.Filled.KeyboardArrowRight, "E")
        adjustedDirection in 135.0..225.0 -> Pair(Icons.Filled.KeyboardArrowDown, "S")
        adjustedDirection in 225.0..315.0 -> Pair(Icons.Filled.KeyboardArrowLeft, "O")
        else -> Pair(Icons.Filled.KeyboardArrowUp, "N")
    }
}

private const val LOCATION_PERMISSION_REQUEST_CODE = 100

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

    val currentPosition = remember { mutableStateOf(Pair(0.0f, 0.0f)) }

    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val lastAccelerometer = remember { FloatArray(3) }
    val lastMagnetometer = remember { FloatArray(3) }
    val rotationMatrix = remember { FloatArray(9) }
    val orientation = remember { FloatArray(3) }
    val azimuth = remember { mutableStateOf(0f) }

    val game_text_level_pickaxe = stringResource(id = R.string.game_text_level_pickaxe)
    val game_text_game = stringResource(id = R.string.game_text_game)
    val game_text_depth = stringResource(id = R.string.game_text_depth)
    val game_text_lat = stringResource(id = R.string.game_text_lat)
    val game_text_lon = stringResource(id = R.string.game_text_lon)
    val game_msg_dig_success = stringResource(id = R.string.game_msg_dig_success)
    val game_msg_dig_wait = stringResource(id = R.string.game_msg_dig_wait)
    val game_msg_pickaxe_bad = stringResource(id = R.string.game_msg_pickaxe_bad)
    val game_msg_bad_locations = stringResource(id = R.string.game_msg_bad_locations)
    val game_msg_error = stringResource(id = R.string.game_msg_error)
    val game_msg_dig_stay = stringResource(id = R.string.game_msg_dig_stay)
    val game_text_dig = stringResource(id = R.string.game_text_dig)
    val game_text_neighbor = stringResource(id = R.string.game_text_neighbor)
    val game_text_neighbor_name = stringResource(id = R.string.game_text_neighbor_name)
    val game_text_neighbor_direction = stringResource(id = R.string.game_text_neighbor_direction)
    val game_text_neighbor_distance = stringResource(id = R.string.game_text_neighbor_distance)

    DisposableEffect(Unit) {
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                }
                SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)
                SensorManager.getOrientation(rotationMatrix, orientation)
                azimuth.value = Math.toDegrees(orientation[0].toDouble()).toFloat()
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission( // Vérification de la permission pour la localisation
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationListener = LocationListener { location ->
                currentPosition.value = Pair(location.latitude.toFloat(), location.longitude.toFloat())
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)

            val handler = Handler(Looper.getMainLooper())
            val runnable = object : Runnable {
                override fun run() {
                    gameRepo.deplacement(session, signature, currentPosition.value.second, currentPosition.value.first) { }
                    handler.postDelayed(this, 10000)
                }
            }
            handler.post(runnable)
        } else {
            ActivityCompat.requestPermissions( // Demande de permission pour la localisation si ce n'est pas déjà fait
                (context as Activity),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

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
                        text = "$game_text_level_pickaxe : $niveauPioche",
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
                    text = game_text_game,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                )
                Text(
                    text = "$game_text_depth : $profondeur m",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                Spacer(modifier = Modifier.padding(8.dp))

                Text(
                    text = "$game_text_lat: $latitude",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                Text(
                    text = "$game_text_lon: $longitude",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )

                Spacer(modifier = Modifier.padding(8.dp))

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
                                        game_msg_dig_success,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (success == "KO - TOO FAST") {
                                    Toast.makeText(
                                        context,
                                        game_msg_dig_wait,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (success == "KO  - BAD PICKAXE") {
                                    Toast.makeText(
                                        context,
                                        game_msg_pickaxe_bad,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (success == "KO - OUT OF BOUNDS") {
                                    Toast.makeText(
                                        context,
                                        game_msg_bad_locations,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(context, "$game_msg_error : $success", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                        else {
                            Toast.makeText(context, game_msg_dig_stay, Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isButtonEnabled.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 50.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = game_text_dig,
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
                            text = "$game_text_neighbor :",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Blue,
                        )

                        LazyColumn{
                            val cinqPremiersVoisins = voisins
                                .filter { voisin -> voisin.name != name }
                                .sortedBy { voisin ->
                                    calculateDistance(longitude!!.toDouble(), latitude!!.toDouble(), voisin.position.first!!.toDouble(), voisin.position.second!!.toDouble())
                                }
                            .take(5)

                            items( cinqPremiersVoisins.size ){ index  ->
                                val voisin = cinqPremiersVoisins[index]
                                val distance = calculateDistance(longitude!!.toDouble(), latitude!!.toDouble(), voisin.position.first!!.toDouble(), voisin.position.second!!.toDouble())
                                val (directionIcon, directionLabel) = determinerDirection(latitude!!.toDouble(), longitude!!.toDouble(), voisin.position.first!!.toDouble(), voisin.position.second!!.toDouble(), azimuth.value)

                                Row {
                                    Column {
                                        Text(
                                            text = "$game_text_neighbor_name : ${voisin.name} , $game_text_neighbor_distance : $distance m",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                        )
                                        Row {
                                            Text(
                                                text = "$game_text_neighbor_direction : $directionLabel",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black,
                                            )
                                            Icon(
                                                directionIcon,
                                                contentDescription = game_text_neighbor_direction,
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