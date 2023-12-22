package com.example.motherloadinorleans.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.motherloadinorleans.MotherlandApplication
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.net.URLEncoder
import java.util.concurrent.CountDownLatch
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class WorkshopRepo private constructor() {
    private val TAG = "StoreRepo"
    private val BASE_URL = "https://test.vautard.fr/creuse_srv/"

    val sharedPref = MotherlandApplication.instance.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val session = sharedPref.getString("session", "") ?: ""
    val signature = sharedPref.getString("signature", "") ?: ""

    val storeRepo = StoreRepo.getInstance()
    val userRepo = UserRepo.getInstance()

    val _user = userRepo.get_user()

    private val _artefacts = MutableLiveData<List<Item>>()
    private val _upgrades = MutableLiveData<List<Pair<Int?,List<Pair<Item, Int?>>>>>()
    private val _niveauPickaxe = MutableLiveData<Int?>()
    val niveauPickaxe: MutableLiveData<Int?> = _niveauPickaxe
    val upgrades : LiveData<List<Pair<Int?,List<Pair<Item, Int?>>>>> = _upgrades
    val artefacts : LiveData<List<Item>> = _artefacts


    init{
        getArtefacts(session, signature)
        craftPioche(session, signature)
        getStatutDuJoueur(session, signature)
    }


    companion object {
        @Volatile
        private var INSTANCE: WorkshopRepo? = null
        val instance : WorkshopRepo by lazy { WorkshopRepo()}

        fun getInstance(): WorkshopRepo {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WorkshopRepo().also { INSTANCE = it }
            }
        }
    }

    fun getArtefacts(session : String?, signature : String?){
        val encodedSession = URLEncoder.encode(session, "UTF-8")
        val encodedSignature = URLEncoder.encode(signature, "UTF-8")
        val url = "${BASE_URL}artefacts_list.php?session=$encodedSession&signature=$encodedSignature"
        val artefactsListe = ArrayList<Item>()

        val stringRequest = StringRequest (
            Request.Method.GET, url,
            {response ->
                try {
                    val docBF: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                    val docBuilder: DocumentBuilder = docBF.newDocumentBuilder()
                    val doc: Document = docBuilder.parse(response.byteInputStream())

                    val statusNode = doc.getElementsByTagName("STATUS").item(0)
                    if(statusNode != null){
                        val status = statusNode.textContent.trim()
                        if (status == "OK"){
                            val artefactsNode = doc.getElementsByTagName("ARTEFACTS").item(0)
                            val listArtefacts = artefactsNode.childNodes
                            val artefactsListeIterable = (0 until listArtefacts.length).map { listArtefacts.item(it) }
                            val countDownLatch = CountDownLatch(artefactsListeIterable.size)
                            if (artefactsListeIterable.isEmpty()){
                                _artefacts.postValue(artefactsListe)
                            }
                            if (artefactsNode != null && artefactsNode.nodeType == Node.ELEMENT_NODE){
                                artefactsListeIterable.forEach{ artefact ->
                                    val elem = artefact as Element
                                    val itemId = elem.getElementsByTagName("ID").item(0).textContent.trim()
                                    storeRepo.getItem(session, signature , itemId){ item ->
                                        if (item == null){
                                            Log.e(TAG, "Erreur lors de la récupération de l'artefact")
                                        }else{
                                            val objet = Item(
                                                itemId,
                                                item.name,
                                                item.type,
                                                item.rarity,
                                                item.imageUrl,
                                                item.descFr,
                                                item.descEn,
                                            )
                                            artefactsListe.add(objet)
                                            countDownLatch.countDown()
                                        }
                                    }
                                }
                            }else{
                                Log.e(TAG, "Node ARTEFACTS not found")
                            }
                            Thread{
                                try {
                                    countDownLatch.await()
                                    _artefacts.postValue(artefactsListe)
                                }catch (e : InterruptedException){
                                    e.printStackTrace()
                                }
                            }.start()
                        } else if (status == "KO - SESSION INVALID" || status == "KO - SESSION EXPIRED") {
                            Log.e(TAG, "Erreur artefacts session : $status")
                            userRepo.reconnexion(_user.username, _user.password){ }
                        } else{
                            Log.e(TAG, "Get artefacts error : $status")
                        }
                    }
                }catch (e: Exception){
                    Log.e(TAG, "Erreur lors de la lecture de la réponse XML", e)
                }
            },
            { error ->
                Log.e(TAG, "Get artefacts error")
                error.printStackTrace()
            })
        MotherlandApplication.instance.requestQueue?.add(stringRequest)
    }

    fun craftPioche(session : String?, signature: String?){
        val encodedSession = URLEncoder.encode(session, "UTF-8")
        val encodedSignature = URLEncoder.encode(signature, "UTF-8")
        val url = BASE_URL+"recettes_pioches.php?session=$encodedSession&signature=$encodedSignature"

        val upgradesListe = ArrayList<Pair<Int?,List<Pair<Item, Int?>>>>()
        val stringRequest = StringRequest (
            Request.Method.GET, url,
            {response ->
                try {
                    val docBF: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                    val docBuilder: DocumentBuilder = docBF.newDocumentBuilder()
                    val doc: Document = docBuilder.parse(response.byteInputStream())

                    val statusNode = doc.getElementsByTagName("STATUS").item(0)
                    if(statusNode != null) {
                        val status = statusNode.textContent.trim()

                        if (status == "OK"){
                            val upgradesNode = doc.getElementsByTagName("UPGRADES").item(0)
                            val listUpgrades = upgradesNode.childNodes
                            val upgradesListeIterable =
                                (0 until listUpgrades.length).map { listUpgrades.item(it) }
                            val countDownLatch = CountDownLatch(upgradesListeIterable.size)
                            if (upgradesListeIterable.isEmpty()) {
                                _upgrades.postValue(upgradesListe)
                            }

                            if (upgradesNode != null && upgradesNode.nodeType == Node.ELEMENT_NODE) {
                                upgradesListeIterable.forEach { upgrade ->
                                    val elem = upgrade as Element
                                    val pickaxeId = elem.getElementsByTagName("PICKAXE_ID")
                                        .item(0).textContent.toInt()
                                    val listItems =
                                        elem.getElementsByTagName("ITEMS").item(0).childNodes
                                    val itemsListeIterable =
                                        (0 until listItems.length).map { listItems.item(it) }
                                    val itemsListe = ArrayList<Pair<Item, Int?>>()
                                    if (listItems != null) {
                                        itemsListeIterable.forEach { item ->
                                            val elemItem = item as Element
                                            val itemId = elemItem.getElementsByTagName("ITEM_ID")
                                                .item(0).textContent.trim()
                                            val quantity = elemItem.getElementsByTagName("QUANTITY")
                                                .item(0).textContent.trim().toInt()
                                            storeRepo.getItem(session, signature, itemId) { item ->
                                                if (item == null) {
                                                    Log.e(
                                                        TAG,
                                                        "Erreur lors de la récupération de l'item"
                                                    )
                                                } else {
                                                    val item = Item(
                                                        itemId,
                                                        item.name,
                                                        item.type,
                                                        item.rarity,
                                                        item.imageUrl,
                                                        item.descFr,
                                                        item.descEn,
                                                    )
                                                    val pair = Pair(item, quantity)
                                                    itemsListe.add(pair)
                                                }
                                            }
                                        }
                                    } else {
                                        Log.e(TAG, "Node ITEMS not found")
                                    }
                                    upgradesListe.add(Pair(pickaxeId, itemsListe))
                                    countDownLatch.countDown()
                                }
                            } else {
                                Log.e(TAG, "Erreur lors de la lecture de la réponse XML")
                            }
                            Thread {
                                try {
                                    countDownLatch.await()
                                    _upgrades.postValue(upgradesListe)
                                } catch (e: InterruptedException) {
                                    e.printStackTrace()
                                }
                            }.start()
                        }
                        else if (status == "KO - SESSION INVALID" || status == "KO - SESSION EXPIRED") {
                            Log.e(TAG, "Erreur craft pioche session : $status")
                            userRepo.reconnexion(_user.username, _user.password){ }
                        }
                        else{
                            Log.e(TAG, "Get upgrades error : $status")
                        }
                    }
                    else{
                        Log.e(TAG, "Get upgrades error : $statusNode")
                    }
                }catch (e: Exception){
                    Log.e(TAG, "Erreur lors de la lecture de la réponse XML", e)
                }
            },
            { error ->
                Log.e(TAG, "Get upgrades error")
                error.printStackTrace()
            })
        MotherlandApplication.instance.requestQueue?.add(stringRequest)
    }


    fun getStatutDuJoueur(session: String?, signature: String?) {
        val encodedSession = URLEncoder.encode(session, "UTF-8")
        val encodedSignature = URLEncoder.encode(signature, "UTF-8")
        val url = BASE_URL+"status_joueur.php?session=$encodedSession&signature=$encodedSignature"
        val position = Pair(0f, 0f)
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

                            val niveauPickaxeNode = doc.getElementsByTagName("PICKAXE").item(0)

                            if (niveauPickaxeNode != null && niveauPickaxeNode.nodeType == Node.ELEMENT_NODE){
                                _niveauPickaxe.postValue(niveauPickaxeNode.textContent.trim().toInt())
                            }
                        } else if (status == "KO - SESSION INVALID" || status == "KO - SESSION EXPIRED") {
                            userRepo.reconnexion(_user.username, _user.password){ }
                            Log.d(TAG, "Erreur Status session - $status")
                        } else {
                            Log.e(TAG, "Get Status : Erreur - $status")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG,"Erreur lors de la lecture de la réponse XML", e)
                }
            },
            { error ->
                Log.d(TAG,"Get status error")
                error.printStackTrace()
            })

        MotherlandApplication.instance.requestQueue?.add(stringRequest)
    }


    fun upgradPioche(session: String?, signature: String? , pickaxeId : Int?, callback: (String) -> Unit){
        val encodedSession = URLEncoder.encode(session, "UTF-8")
        val encodedSignature = URLEncoder.encode(signature, "UTF-8")
        val encodedPickaxeId = URLEncoder.encode(pickaxeId.toString(), "UTF-8")
        val url = BASE_URL+"maj_pioche.php?session=$encodedSession&signature=$encodedSignature&pickaxe_id=$encodedPickaxeId"

        val stringRequest = StringRequest (
            Request.Method.GET, url,
            {response ->
                try {
                    val docBF: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                    val docBuilder: DocumentBuilder = docBF.newDocumentBuilder()
                    val doc: Document = docBuilder.parse(response.byteInputStream())

                    val statusNode = doc.getElementsByTagName("STATUS").item(0)
                    if(statusNode != null){
                        val status = statusNode.textContent.trim()
                        if(status == "OK"){
                            Log.d(TAG, "Upgrade pioche réussi!!")
                            callback("OK")
                        }else if( status == "KO - NO ITEMS"){
                            Log.e(TAG, "KO - NO ITEMS")
                            callback(status)
                        }else if( status == "KO - UNKNOWN ID") {
                            Log.e(TAG, "KO - UNKNOWN ID")
                            callback(status)
                        }else if (status == "KO - SESSION INVALID" || status == "KO - SESSION EXPIRED") {
                            userRepo.reconnexion(_user.username, _user.password){ }
                            Log.d(TAG, "Erreur Upgrade session : - $status")
                            callback(status)
                        }else{
                            Log.e(TAG, "Upgrade pioche : Erreur")
                            callback(status)
                        }
                    }
                }catch (e: Exception){
                    Log.e(TAG, "Erreur lors de la lecture de la réponse XML", e)
                }
            },
            { error ->
                Log.e(TAG, "Upgrade pioche : Erreur")
                error.printStackTrace()
                callback("ERREUR")
            })
        MotherlandApplication.instance.requestQueue?.add(stringRequest)
    }

    fun miseAJourUpgrad(){
        craftPioche(session, signature)
        getStatutDuJoueur(session, signature)
        StoreRepo.instance.getStatutDuJoueur(session, signature)
        GameRepo.instance.getStatutDuJoueur(session, signature)
    }
}