package com.example.motherloadinorleans.view

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.border

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motherloadinorleans.R
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import coil.compose.rememberImagePainter
import com.example.motherloadinorleans.model.Offer
import com.example.motherloadinorleans.model.StoreRepo
import com.example.motherloadinorleans.navigatePage
import java.util.Locale


@Composable
fun StoreScreen(storeViewModel: StoreRepo) {
    val offers = storeViewModel.offers.observeAsState(listOf())

    if (offers.value.isEmpty()) {
        Text(text = "Aucune offre disponible")
    }

    LazyColumn {
        items(offers.value) { offer ->
            OfferItem(offer = offer, storeViewModel = storeViewModel)
        }
    }
}

@Composable
fun OfferItem(offer: Offer, storeViewModel: StoreRepo) {
    val context = LocalContext.current

    val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val session = sharedPref.getString("session", "") ?: ""
    val signature = sharedPref.getString("signature", "") ?: ""

    val showDetailsDialog = remember { mutableStateOf(false) }

    if (showDetailsDialog.value) {
        ItemDetailsDialog(offer = offer, onDismiss = { showDetailsDialog.value = false })
    }

    val backgroundColor = when (offer.item?.rarity) {
        1 -> Color.Gray
        2 -> Color.Green
        3 -> Color.Blue
        4 -> Color.Magenta
        5 -> Color.Yellow
        else -> Color.White
    }
    val imageUrl = "https://test.vautard.fr/creuse_imgs/${offer.item?.imageUrl}"

    val imageModifier = Modifier
        .size(60.dp)
        .clip(RoundedCornerShape(8.dp))

    Card(backgroundColor = backgroundColor,modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
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

            Column(modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f)) {
                Text(text = "Nom: ${offer.item?.name}")
                Text(text = "Quantité: ${offer.quantity}")
                Text(text = "Prix: ${offer.price}")
            }

            IconButton(onClick = { showDetailsDialog.value = true }) {
                Icon(Icons.Filled.Info, contentDescription = "Info")
            }

            Button(onClick = {
                StoreRepo.instance.acheterItem(session, signature, offer.offerId ?: "") { success ->
                    if (success == "OK") {
                        Toast.makeText(context, "Achat réussi !", Toast.LENGTH_SHORT).show()
                        StoreRepo.instance.miseAJourAcheter(session,signature ,offer.offerId ?: "")
                    }
                    else if (success == "KO - NO MONEY") {
                        Toast.makeText(context, "Pas assez de solde !", Toast.LENGTH_SHORT).show()
                    }
                    else if (success == "KO - UNKNOWN ID") {
                        Toast.makeText(context, "L'offre n'existe plus !", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(context, "Erreur lors de l'achat !", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = stringResource(id = R.string.menu_text_store))
            }
        }
    }
}

@Composable
fun ItemDetailsDialog(offer: Offer, onDismiss: () -> Unit) {
    val imageUrl = "https://test.vautard.fr/creuse_imgs/${offer.item?.imageUrl}"
    val isFrench = Locale.getDefault().language == Locale.FRENCH.language
    val description = if (isFrench) offer.item?.descFr else offer.item?.descEn
    val type = if (offer.item?.type == "M") "Minerai" else if (offer.item?.type == "A") "Artefact" else "Inconnu"

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
                Text("Nom: ${offer.item?.name}")
                Text("Type: $type")
                Text("Rareté: ${offer.item?.rarity}")
                Text("Description: $description")
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("FERMER")
                }
            }
        }
    }
}

@Composable
fun Store(navController: NavController, storeRepo: StoreRepo) {
    val money = storeRepo.money.observeAsState(0)
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
                        Text("Acheter")
                    }

                    Spacer(modifier= Modifier.width(1.dp))

                    Button(
                        onClick = {
                            navController.navigate("sale_page")
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF005CE7),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Vendre")
                    }
                }
                Text(text = "Mon argent: ${money.value}")
                StoreScreen(storeViewModel = storeRepo)
            }
        }
    )
}

@Preview
@Composable
fun StorePreview() {
    Store(navController = NavController(LocalContext.current), storeRepo = StoreRepo.instance)
}