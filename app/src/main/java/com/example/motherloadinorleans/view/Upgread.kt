package com.example.motherloadinorleans.view

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.motherloadinorleans.R
import com.example.motherloadinorleans.model.Item
import com.example.motherloadinorleans.model.WorkshopRepo

@Composable
fun UpgreadScreen(workshopRepo: WorkshopRepo){
    val upgrades = workshopRepo.upgrades.observeAsState(listOf())
    val monNiveauPickaxe = workshopRepo.niveauPickaxe.observeAsState(0)

    if (upgrades.value.isEmpty()){
        Text(text = "Aucune amélioration disponible")
    }else{
        LazyColumn{
            items(upgrades.value){ upgrade ->
                if (upgrade.first!!.toInt() > monNiveauPickaxe.value!!) {
                    upgrade(upgrade = upgrade, workshopRepo = workshopRepo)
                }
            }
        }
    }
}

@Composable
fun upgrade(upgrade: Pair<Int?,List<Pair<Item, Int?>>>, workshopRepo: WorkshopRepo){
    val context = LocalContext.current

    val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val session = sharedPref.getString("session", "") ?: ""
    val signature = sharedPref.getString("signature", "") ?: ""

    val showDetailsDialog = remember { mutableStateOf(false) }

    if (showDetailsDialog.value) {
        UpdateDetailsDialog(update = upgrade, onDismiss = { showDetailsDialog.value = false })
    }
    val backgroundColor = when (upgrade.first) {
        2 -> Color(0xFF33E251)
        3 -> Color(0xFF258DCC)
        4 -> Color(0xFFA955CE)
        5 -> Color(0xFFEDBB12)
        else -> Color.Gray
    }

    val textColor = when (upgrade.first) {
        2 -> Color.Black
        3 -> Color.White
        4 -> Color.White
        5 -> Color.Black
        else -> Color.Black
    }


    Card(backgroundColor = backgroundColor,modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Column{
            Row(modifier = Modifier.padding(16.dp)) {
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                ) {
                    Text(text = "Amélioration pioche au niveau ${upgrade.first}", color = textColor)
                    IconButton(onClick = { showDetailsDialog.value = true }) {
                        Icon(Icons.Filled.Info, contentDescription = "Info")
                    }
                }
                Button(onClick = {
                    workshopRepo.upgradPioche(session, signature, upgrade.first?: 0){ success ->
                        if (success == "OK") {
                            Toast.makeText(context, "Upgrade réussi ", Toast.LENGTH_SHORT).show()
                            workshopRepo.miseAJourUpgrad()
                        }else if(success == "KO - NO ITEMS"){
                            Toast.makeText(context, "Pas assez d'items ", Toast.LENGTH_SHORT).show()
                        }else if(success == "KO - UNKNOWN ID"){
                            Toast.makeText(context, "KO - UNKNOWN ID", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(context, "Erreur lors de l'upgrade ", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Icon(Icons.Filled.Build, contentDescription = stringResource(id = R.string.menu_text_store))
                }
            }
        }
    }
}

@Composable
fun UpdateDetailsDialog(update : Pair<Int?,List<Pair<Item ,Int?>>>, onDismiss: () -> Unit){

    Dialog(onDismissRequest = onDismiss){
        Card(modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Amélioration pioche au niveau ${update.first}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Items requis :")
                val items = update.second
                val itemsListe = remember { mutableStateOf(items) }
                LazyColumn{
                    items(itemsListe.value){ pair ->
                        val (item, quantity) = pair

                        val backgroundColor = when (item.rarity) {
                            1 -> Color.Gray
                            2 -> Color(0xFF33E251)
                            3 -> Color(0xFF258DCC)
                            4 -> Color(0xFFA955CE)
                            5 -> Color(0xFFEDBB12)
                            else -> Color.White
                        }

                        val textColor = when (item.rarity) {
                            1 -> Color.Black
                            2 -> Color.Black
                            3 -> Color.White
                            4 -> Color.White
                            5 -> Color.Black
                            else -> Color.Black
                        }

                        val imageUrl = "https://test.vautard.fr/creuse_imgs/${item.imageUrl}"

                        val imageModifier = Modifier
                            .size(45.dp)
                            .clip(RoundedCornerShape(45.dp))

                        Card(backgroundColor = backgroundColor,modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)) {
                            Column {
                                Row(modifier = Modifier.padding(16.dp)) {
                                    Image(
                                        painter = rememberImagePainter(
                                            data = imageUrl,
                                            builder = {
                                                error(R.drawable.error_placeholder)
                                                placeholder(R.drawable.loading_placeholder)
                                            }
                                        ),
                                        contentDescription = "Item Image",
                                        modifier = imageModifier
                                    )

                                    Column(
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .weight(1f)
                                    ) {
                                        Text(text = "Nom: ${item.name}")
                                        Text(text = "Quantité: ${quantity}")

                                    }
                                }
                            }
                        }
                    }
                }
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("FERMER")
                }
            }
        }
    }
}



@Composable
fun Upgreades(navController: NavController, workshopRepo: WorkshopRepo){
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
                            /*rien*/
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF4C9DFF),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Text("Upgrade")
                    }

                    Spacer(modifier= Modifier.width(1.dp))

                    Button(
                        onClick = {
                            navController.navigate("album_page")
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF005CE7),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Album")
                    }
                }
                UpgreadScreen(workshopRepo = workshopRepo)
            }
        }
    )
}


@Preview
@Composable
fun UpgreadPreview() {
    Upgreades(navController = NavController(LocalContext.current), workshopRepo = WorkshopRepo.instance)
}
