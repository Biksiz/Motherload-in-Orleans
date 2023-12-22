package com.example.motherloadinorleans.view


import android.content.Context
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.ui.platform.textInputServiceFactory
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.motherloadinorleans.model.StoreRepo
import com.example.motherloadinorleans.model.WorkshopRepo
import coil.compose.rememberImagePainter
import com.example.motherloadinorleans.R
import com.example.motherloadinorleans.model.Item
import java.util.Locale




@Composable
fun SaleScreenv(workshopRepo: WorkshopRepo){
    val artefacts = workshopRepo.artefacts.observeAsState(listOf())

    if ( artefacts.value.isEmpty()){
        Text(text ="Aucun artefact disponible")
    }else{
        LazyColumn{
            items(artefacts.value){ artefact ->
                artefact(artefact = artefact, workshopRepo = workshopRepo)
            }
        }
    }
}


@Composable
fun artefact(artefact: Item, workshopRepo: WorkshopRepo){
    val context = LocalContext.current

    val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val session = sharedPref.getString("session", "") ?: ""
    val signature = sharedPref.getString("signature", "") ?: ""

    val showDetailsDialog = remember { mutableStateOf(false) }

    if (showDetailsDialog.value) {
        ArtefactDetailsDialog(item = artefact, onDismiss = { showDetailsDialog.value = false })
    }
    val backgroundColor = when (artefact.rarity) {
        1 -> Color.Gray
        2 -> Color.Green
        3 -> Color.Blue
        4 -> Color.Magenta
        5 -> Color.Yellow
        else -> Color.White
    }
    val imageUrl = "https://test.vautard.fr/creuse_imgs/${artefact.imageUrl}"

    val imageModifier = Modifier
        .size(60.dp)
        .clip(RoundedCornerShape(8.dp))

    Card(backgroundColor = backgroundColor,modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Column{
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
                    Text(text = "Nom: ${artefact.name}")
                }
                IconButton(onClick = { showDetailsDialog.value = true }) {
                    Icon(Icons.Filled.Info, contentDescription = "Info")
                }
            }
        }
    }
}

@Composable
fun ArtefactDetailsDialog(item: Item, onDismiss: () -> Unit) {
    val imageUrl = "https://test.vautard.fr/creuse_imgs/${item.imageUrl}"
    val isFrench = Locale.getDefault().language == Locale.FRENCH.language
    val description = if (isFrench) item?.descFr else item?.descEn
    val type = if (item.type == "M") "Minerai" else if (item.type == "A") "Artefact" else "Inconnu"

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Détails de l'item", style = MaterialTheme.typography.h6)
                Spacer(Modifier.height(8.dp))
                Image(
                    painter = rememberImagePainter(data = imageUrl),
                    contentDescription = "Item Image",
                    modifier = Modifier
                        .size(128.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(8.dp))
                Text("Nom: ${item.name}")
                Text("Type: $type")
                Text("Rareté: ${item.rarity}")
                Text("Description: $description")
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("FERMER")
                }
            }
        }
    }
}


@Composable
fun Album(navController: NavController, workshopRepo: WorkshopRepo){
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()

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
                modifier= Modifier.fillMaxSize(),
                horizontalAlignment= Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement= Arrangement.Center,
                    modifier= Modifier.padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            navController.navigate("upgread_page")
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF005CE7),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Upgrade")
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
                        Text("Album")
                    }
                }
                SaleScreenv(workshopRepo = workshopRepo)
            }
        }
    )
}



@Preview
@Composable
fun AlbumPreview() {
    Album(navController = NavController(LocalContext.current), workshopRepo = WorkshopRepo.instance)
}