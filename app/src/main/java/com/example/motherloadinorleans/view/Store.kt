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
        Text(text = stringResource(id = R.string.store_text_no_offers))
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

    val store_text_item_name = stringResource(id = R.string.store_text_item_name)
    val store_text_item_quantity = stringResource(id = R.string.store_text_item_quantity)
    val store_text_item_price = stringResource(id = R.string.store_text_item_price)
    val store_text_item_info = stringResource(id = R.string.store_text_item_info)
    val store_text_buy_success = stringResource(id = R.string.store_text_buy_success)
    val store_text_buy_no_money = stringResource(id = R.string.store_text_buy_no_money)
    val store_text_buy_no_offer = stringResource(id = R.string.store_text_buy_no_offer)
    val store_text_buy_error = stringResource(id = R.string.store_text_buy_error)
    val store_text_item_image = stringResource(id = R.string.store_text_item_image)

    if (showDetailsDialog.value) {
        ItemDetailsDialog(offer = offer, onDismiss = { showDetailsDialog.value = false })
    }

    val backgroundColor = when (offer.item?.rarity) {
        1 -> Color.Gray
        2 -> Color(0xFF33E251)
        3 -> Color(0xFF258DCC)
        4 -> Color(0xFFA955CE)
        5 -> Color(0xFFEDBB12)
        else -> Color.White
    }

    val textColor = when (offer.item?.rarity) {
        1 -> Color.Black
        2 -> Color.Black
        3 -> Color.White
        4 -> Color.White
        5 -> Color.Black
        else -> Color.Black
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
                contentDescription = store_text_item_image,
                modifier = imageModifier
            )

            Column(modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f)) {
                Text(text = "$store_text_item_name: ${offer.item?.name}", color = textColor)
                Text(text = "$store_text_item_quantity: ${offer.quantity}", color = textColor)
                Text(text = "$store_text_item_price: ${offer.price} €", color = textColor)
            }

            IconButton(onClick = { showDetailsDialog.value = true }) {
                Icon(Icons.Filled.Info, contentDescription = store_text_item_info, tint = textColor)
            }

            Button(onClick = {
                StoreRepo.instance.acheterItem(session, signature, offer.offerId ?: "") { success ->
                    if (success == "OK") {
                        Toast.makeText(context, store_text_buy_success, Toast.LENGTH_SHORT).show()
                        StoreRepo.instance.miseAJourAcheter(session,signature ,offer.offerId ?: "")
                    }
                    else if (success == "KO - NO MONEY") {
                        Toast.makeText(context, store_text_buy_no_money, Toast.LENGTH_SHORT).show()
                    }
                    else if (success == "KO - UNKNOWN ID") {
                        Toast.makeText(context, store_text_buy_no_offer, Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(context, store_text_buy_error, Toast.LENGTH_SHORT).show()
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
    val store_text_minerai = stringResource(id = R.string.store_text_minerai)
    val store_text_artefact = stringResource(id = R.string.store_text_artefact)
    val store_text_buy_unknown = stringResource(id = R.string.store_text_buy_unknown)
    val store_text_item_details = stringResource(id = R.string.store_text_item_details)
    val store_text_item_name = stringResource(id = R.string.store_text_item_name)
    val store_text_item_type = stringResource(id = R.string.store_text_item_type)
    val store_text_item_rarity = stringResource(id = R.string.store_text_item_rarity)
    val store_text_item_description = stringResource(id = R.string.store_text_item_description)
    val store_btn_close = stringResource(id = R.string.store_btn_close)

    val imageUrl = "https://test.vautard.fr/creuse_imgs/${offer.item?.imageUrl}"
    val isFrench = Locale.getDefault().language == Locale.FRENCH.language
    val description = if (isFrench) offer.item?.descFr else offer.item?.descEn
    val type = if (offer.item?.type == "M") store_text_minerai else if (offer.item?.type == "A") store_text_artefact else store_text_buy_unknown

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(store_text_item_details, style = MaterialTheme.typography.h6)
                Spacer(Modifier.height(8.dp))
                Image(
                    painter = rememberImagePainter(data = imageUrl),
                    contentDescription = "Item Image",
                    modifier = Modifier
                        .size(128.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(8.dp))
                Text("$store_text_item_name: ${offer.item?.name}")
                Text("$store_text_item_type: $type")
                Text("$store_text_item_rarity: ${offer.item?.rarity}")
                Text("$store_text_item_description: $description")
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(store_btn_close)
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

    val store_text_buy = stringResource(id = R.string.store_text_buy)
    val store_text_sell = stringResource(id = R.string.store_text_sell)
    val store_text_money = stringResource(id = R.string.store_text_money)

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
                        Text(store_text_buy)
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
                        Text(store_text_sell)
                    }
                }
                Text(text = "$store_text_money: ${money.value} €")
                Spacer(modifier = Modifier.height(8.dp))
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