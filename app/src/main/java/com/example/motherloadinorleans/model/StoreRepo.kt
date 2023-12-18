package com.example.motherloadinorleans.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.toolbox.StringRequest
import com.android.volley.Request
import com.android.volley.Response
import org.json.JSONArray
import android.content.Context
import android.util.Log
import com.android.volley.toolbox.JsonObjectRequest
import com.example.motherloadinorleans.MotherlandApplication
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.net.URLEncoder
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import androidx.compose.ui.platform.LocalContext
import java.util.concurrent.CountDownLatch


class StoreRepo private constructor() {
    private val TAG = "StoreRepo"
    private val BASE_URL = "https://test.vautard.fr/creuse_srv/"
    private val userRepo = UserRepo.getInstance()
    private val _offers = MutableLiveData<List<Offer>>()
    val offers: LiveData<List<Offer>> = _offers

    val sharedPref = MotherlandApplication.instance.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val session = sharedPref.getString("session", "") ?: ""
    val signature = sharedPref.getString("signature", "") ?: ""

    init {

        Log.e(TAG , "on là les zbi: $session , $signature")
        recupererOffres(session, signature)
    }

    companion object {
        @Volatile
        private var INSTANCE: StoreRepo? = null
        val instance: StoreRepo by lazy { StoreRepo() }

        fun getInstance(): StoreRepo {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoreRepo().also { INSTANCE = it }
            }
        }
    }

    fun getItem(session: String?, signature: String?, itemId: String?, callback: (Item?) -> Unit) {
        val encodedSession = URLEncoder.encode(session, "UTF-8")
        val encodedSignature = URLEncoder.encode(signature, "UTF-8")
        val encodedItemId = URLEncoder.encode(itemId, "UTF-8")
        val url = BASE_URL + "item_detail.php?session=$encodedSession&signature=$encodedSignature&item_id=$encodedItemId"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val docBF: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                    val docBuilder = docBF.newDocumentBuilder()
                    val doc = docBuilder.parse(InputSource(response.byteInputStream()))

                    val statusNode = doc.getElementsByTagName("STATUS").item(0)
                    if (statusNode != null && statusNode.textContent.trim() == "OK") {
                        val itemNode = doc.getElementsByTagName("PARAMS").item(0)
                        val elem = itemNode as Element

                        if (itemNode != null && itemNode.nodeType == Node.ELEMENT_NODE) {
                            val item = Item(
                                itemId,
                                elem.getElementsByTagName("NOM").item(0)?.textContent,
                                elem.getElementsByTagName("TYPE").item(0)?.textContent,
                                elem.getElementsByTagName("RARETE").item(0)?.textContent?.toInt(),
                                elem.getElementsByTagName("IMAGE").item(0)?.textContent,
                                elem.getElementsByTagName("DESC_FR").item(0)?.textContent,
                                elem.getElementsByTagName("DESC_EN").item(0)?.textContent
                            )
                            callback(item)  // Utilisation du callback pour retourner l'item
                        } else {
                            Log.e(TAG, "Noeud 'ITEM' introuvable dans la réponse XML")
                            callback(null)
                        }
                    } else {
                        val status = statusNode?.textContent?.trim()
                        Log.e(TAG, "Get Item : Erreur - $status")
                        callback(null)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de la lecture de la réponse XML", e)
                    callback(null)
                }
            },
            { error ->
                Log.d(TAG, "Get Item error")
                error.printStackTrace()
                callback(null)
            })

        MotherlandApplication.instance.requestQueue?.add(stringRequest)
    }

    fun recupererOffres(session: String?, signature: String?) {
        val encodedSession = URLEncoder.encode(session, "UTF-8")
        val encodedSignature = URLEncoder.encode(signature, "UTF-8")
        val url = BASE_URL+"market_list.php?session=$encodedSession&signature=$encodedSignature"
        val offersList = ArrayList<Offer>()
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val docBF: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                    val docBuilder: DocumentBuilder = docBF.newDocumentBuilder()
                    val doc: Document = docBuilder.parse(response.byteInputStream())

                    val statusNode = doc.getElementsByTagName("STATUS").item(0)
                    if (statusNode != null) {
                        val status = statusNode.textContent.trim()
                        if (status == "OK") {
                            Log.d(TAG, "Appel Get Offers réussie !")
                            val paramsNode = doc.getElementsByTagName("PARAMS").item(0)
                            val offersNode = paramsNode.firstChild
                            val listeOffer = offersNode.childNodes
                            Log.e(TAG, "liste des offres : $listeOffer")
                            Log.e(TAG, "offer Node  : $offersNode")
                            Log.e(TAG, "paramNode  : $paramsNode")
                            val offerListIterable = (0 until listeOffer.length).map { listeOffer.item(it) }
                            val countDownLatch = CountDownLatch(offerListIterable.size)
                            if (offerListIterable.isEmpty()) {
                                _offers.postValue(offersList)
                            }

                            if (offersNode != null && offersNode.nodeType == Node.ELEMENT_NODE) {
                                Log.e(TAG, "offerListIterable  : $offerListIterable")
                                offerListIterable.forEach{ offre ->
                                    val elem = offre as Element
                                    val offerId = elem.getElementsByTagName("OFFER_ID").item(0)?.textContent
                                    val itemId = elem.getElementsByTagName("ITEM_ID").item(0)?.textContent
                                    val quantity = elem.getElementsByTagName("QUANTITE").item(0)?.textContent?.toInt()
                                    val price = elem.getElementsByTagName("PRIX").item(0)?.textContent?.toDouble()
                                    Log.e(TAG, "offerId  : $offerId")
                                    getItem(session, signature, itemId){ item ->
                                        Log.e(TAG, "item  : $item")
                                        if(item == null) {
                                            Log.e(TAG, "Erreur lors de la récupération de l'item")
                                        }else {
                                            val offer = Offer(
                                                offerId,
                                                itemId,
                                                item,
                                                quantity,
                                                price
                                            )
                                            offersList.add(offer)
                                            countDownLatch.countDown()
                                        }
                                    }
                                }
                            }
                            else {
                                Log.e(TAG, "Noeud 'PARAMS' introuvable dans la réponse XML")
                            }
                            Thread {
                                try {
                                    countDownLatch.await()
                                    _offers.postValue(offersList)
                                    Log.e(TAG, "offres 1: $offersList")
                                } catch (e: InterruptedException) {
                                    e.printStackTrace()
                                }
                            }.start()
                            Log.e(TAG, "offres 2: $offersList")

                        } else {
                            Log.e(TAG, "Get Offers : Erreur - $status")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG,"Erreur lors de la lecture de la réponse XML", e)
                }

            },
            { error ->
                Log.d(TAG,"Get offers error")
                error.printStackTrace()
            })

        MotherlandApplication.instance.requestQueue?.add(stringRequest)
    }

    private fun acheterItem(offerId: String) {
        // à completer

    }

    private fun miseAJourAcheter(offerId: String) {
        val updatedOffers = _offers.value?.filterNot { it.offerId == offerId }
        _offers.postValue(updatedOffers)
    }

}