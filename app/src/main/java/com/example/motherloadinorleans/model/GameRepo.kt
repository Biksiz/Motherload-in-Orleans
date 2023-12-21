package com.example.motherloadinorleans.model

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.motherloadinorleans.MotherlandApplication
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.net.URLEncoder
import java.util.concurrent.CountDownLatch
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.xml.sax.InputSource

class GameRepo private constructor(){
    private val TAG = "GameRepo"
    private val BASE_URL = "https://test.vautard.fr/creuse_srv/"

    private val _voisin = MutableLiveData<List<Voisin>>()
    private val _profondeur = MutableLiveData<String>()
    private val _itemId = MutableLiveData<String>()
    private val _position = MutableLiveData<Pair<Float?, Float?>>()
    private val _niveauPickaxe = MutableLiveData<Int>()

    val voisin: MutableLiveData<List<Voisin>> get() = _voisin
    val profondeur: MutableLiveData<String> get() = _profondeur
    val itemId: MutableLiveData<String> get() = _itemId
    val position: MutableLiveData<Pair<Float?, Float?>> get() = _position
    val niveauPickaxe: MutableLiveData<Int> get() = _niveauPickaxe

    val sharedPref = MotherlandApplication.instance.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val session = sharedPref.getString("session", "") ?: ""
    val signature = sharedPref.getString("signature", "") ?: ""

    init {
        setProfondeur("0")
        Log.e(TAG, "profondeur : ${profondeur.value}")
        getStatutDuJoueur(session, signature)
    }




    companion object {
        @Volatile
        private var INSTANCE: GameRepo? = null
        val instance: GameRepo by lazy { GameRepo() }

        fun getInstance(): GameRepo {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GameRepo().also { INSTANCE = it }
            }
        }
    }

    fun setProfondeur(profondeur: String){
        _profondeur.postValue(profondeur)
    }

    fun deplacement(session : String?, signature : String?, lon : Float?, lat : Float?, callback: (String) -> Unit){
        val encodedSession = URLEncoder.encode(session, "UTF-8")
        val encodedSignature = URLEncoder.encode(signature, "UTF-8")
        val encodedLon = URLEncoder.encode(lon.toString(), "UTF-8")
        val encodedLat = URLEncoder.encode(lat.toString(), "UTF-8")
        val url = BASE_URL+"deplace.php?session=$encodedSession&signature=$encodedSignature&lon=$encodedLon&lat=$encodedLat"
        val voisinListe = ArrayList<Voisin>()

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
                            Log.d(TAG, "Déplacement réussi !")
                            Log.d(TAG, "Latitude : $lat, Longitude : $lon")
                            val voisinNode = doc.getElementsByTagName("VOISINS").item(0)
                            val listeVoisin = voisinNode.childNodes
                            val voisinListIterable = (0 until listeVoisin.length ).map { listeVoisin.item(it)}
                            val countDownLatch = CountDownLatch(voisinListIterable.size)
                            if (voisinListIterable.isEmpty()){
                                _voisin.postValue(voisinListe)
                            }
                            if (voisinNode != null && voisinNode.nodeType == Node.ELEMENT_NODE){
                                voisinListIterable.forEach{ voisin ->
                                    val elem = voisin as Element
                                    if(voisin == null){
                                        Log.e(TAG, "Erreur lors de la récupération de voisin")
                                    }else{
                                        val objet = Voisin(
                                            elem.getElementsByTagName("NOM").item(0)?.textContent,
                                            Pair(
                                                elem.getElementsByTagName("LONGITUDE").item(0)?.textContent?.toFloat(),
                                                elem.getElementsByTagName("LATITUDE").item(0)?.textContent?.toFloat()
                                            ),
                                        )
                                        voisinListe.add(objet)
                                        countDownLatch.countDown()
                                    }
                                }
                                callback("OK")
                            }else {
                                Log.e(TAG, "Noeud 'VOISINS' introuvable dans la réponse XML")
                            }
                            Thread{
                                try {
                                    countDownLatch.await()
                                    Log.d(TAG, "voisinListe : $voisinListe")
                                    _voisin.postValue(voisinListe)
                                }catch (e: InterruptedException){
                                    e.printStackTrace()
                                }
                            }.start()
                        }else if (status == "KO - BAD LOCATION FORMAT"){
                            Log.e(TAG, "Déplacement : Erreur - $status")
                            callback("KO - BAD LOCATION FORMAT")
                        }else {
                            Log.e(TAG, "Vente : Erreur - $status")
                            callback(status)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG,"Erreur lors de la lecture de la réponse XML", e)
                    callback("KO - BAD LOCATION FORMAT")
                }
            },
            { error ->
                Log.d(TAG,"Déplacement error")
                error.printStackTrace()
                callback("ERROR")
            })
        MotherlandApplication.instance.requestQueue?.add(stringRequest)
    }

    fun creuser(session : String?, signature : String?, lon : Float?, lat : Float?, callback: (String) -> Unit){
        val encodedSession = URLEncoder.encode(session, "UTF-8")
        val encodedSignature = URLEncoder.encode(signature, "UTF-8")
        val encodedLon = URLEncoder.encode(lon.toString(), "UTF-8")
        val encodedLat = URLEncoder.encode(lat.toString(), "UTF-8")
        val url = BASE_URL+"creuse.php?session=$encodedSession&signature=$encodedSignature&lon=$encodedLon&lat=$encodedLat"
        val voisinListe = ArrayList<Voisin>()

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
                            Log.d(TAG, "Creuser réussi !")
                            val profondeurNode = doc.getElementsByTagName("DEPTH").item(0)

                            val itemIdNode = doc.getElementsByTagName("ITEM_ID").item(0)
                            if (itemIdNode != null && itemIdNode.nodeType == Node.ELEMENT_NODE){
                                Toast.makeText(MotherlandApplication.instance, "Vous avez trouvé un objet !", Toast.LENGTH_SHORT).show()
                            }

                            val voisinNode = doc.getElementsByTagName("VOISINS").item(0)
                            val listeVoisin = voisinNode.childNodes
                            val voisinListIterable = (0 until listeVoisin.length ).map { listeVoisin.item(it)}
                            val countDownLatch = CountDownLatch(voisinListIterable.size)
                            if (voisinListIterable.isEmpty()){
                                _voisin.postValue(voisinListe)
                            }

                            if (profondeurNode != null && voisinNode != null && voisinNode.nodeType == Node.ELEMENT_NODE){
                                voisinListIterable.forEach{ voisin ->
                                    val elem = voisin as Element
                                    if(voisin == null){
                                        Log.e(TAG, "Erreur lors de la récupération de voisin")
                                    }else{
                                        val objet = Voisin(
                                            elem.getElementsByTagName("NOM").item(0)?.textContent,
                                            Pair(
                                                elem.getElementsByTagName("LONGITUDE").item(0)?.textContent?.toFloat(),
                                                elem.getElementsByTagName("LATITUDE").item(0)?.textContent?.toFloat()
                                            ),
                                        )
                                        voisinListe.add(objet)
                                        countDownLatch.countDown()
                                    }
                                }
                            }else{
                                Log.e(TAG, "Noeud 'PROFONDEUR' ou 'VOISINS' introuvable dans la réponse XML")
                            }
                            Thread{
                                try {
                                    countDownLatch.await()
                                    _voisin.postValue(voisinListe)
                                    setProfondeur(profondeurNode.textContent.trim())
                                }catch (e: InterruptedException){
                                    e.printStackTrace()
                                }
                            }.start()
                        }else if (status == "KO - TOO FAST"){
                            Log.e(TAG, "Creuser : Erreur - $status")
                            callback("KO - TOO FAST")
                        }else if (status == "KO  - BAD PICKAXE"){
                            Log.e(TAG, "Creuser : Erreur - $status")
                            callback("KO  - BAD PICKAXE")
                        }else {
                            Log.e(TAG, "Creuser : Erreur - $status")
                            callback(status)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG,"Erreur lors de la lecture de la réponse XML", e)
                    callback("KO - BAD LOCATION FORMAT")
                }
            },
            { error ->
                Log.d(TAG,"Creuser error")
                error.printStackTrace()
                callback("ERROR")
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
                            val positionNode = doc.getElementsByTagName("POSITION").item(0)
                            val lon = positionNode.childNodes.item(0).textContent.toFloat()
                            val lat = positionNode.childNodes.item(1).textContent.toFloat()

                            val niveauPickaxeNode = doc.getElementsByTagName("PICKAXE").item(0)

                            if (niveauPickaxeNode != null && niveauPickaxeNode.nodeType == Node.ELEMENT_NODE && positionNode != null && positionNode.nodeType == Node.ELEMENT_NODE){
                                _position.postValue(Pair(lon, lat))
                                _niveauPickaxe.postValue(niveauPickaxeNode.textContent.trim().toInt())
                                deplacement(session, signature, lon, lat, callback = { status ->
                                    if (status == "OK"){
                                        Log.d(TAG, "Déplacement réussi !")
                                    }else if (status == "KO - BAD LOCATION FORMAT"){
                                        Log.e(TAG, "Déplacement : Erreur - $status")
                                    }else {
                                        Log.e(TAG, "Déplacement : Erreur - $status")
                                    }
                                })
                            }
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
}