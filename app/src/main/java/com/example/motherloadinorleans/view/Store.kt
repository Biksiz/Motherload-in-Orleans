package com.example.motherloadinorleans.view

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import coil.compose.rememberImagePainter
import com.example.motherloadinorleans.model.Offer
import com.example.motherloadinorleans.model.StoreRepo
import com.example.motherloadinorleans.navigatePage


@Composable
fun StoreScreen(storeViewModel: StoreRepo) {
    val offers = storeViewModel.offers.observeAsState(listOf())

    LazyColumn {
        items(offers.value) { offer ->
            OfferItem(offer = offer)
        }
    }
}

@Composable
fun OfferItem(offer: Offer) {
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
    Card(backgroundColor = backgroundColor,modifier = Modifier.fillMaxWidth().padding(8.dp)) {
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

            Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
                Text(text = "Nom: ${offer.item?.name}")
                Text(text = "Quantité: ${offer.quantity}")
                Text(text = "Prix: ${offer.price}")
            }

            IconButton(onClick = { /* TODO: Afficher les détails de l'item */ }) {
                Icon(Icons.Filled.Info, contentDescription = "Info")
            }

            Button(onClick = { /* TODO: Gérer l'achat de l'item */ }) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = stringResource(id = R.string.menu_text_store))
            }
        }
    }
}

@Composable
fun Store(navController: NavController, storeRepo: StoreRepo) {
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

                    Spacer(modifier= Modifier.width(1.dp)) // Espace optionnel entre les boutons

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