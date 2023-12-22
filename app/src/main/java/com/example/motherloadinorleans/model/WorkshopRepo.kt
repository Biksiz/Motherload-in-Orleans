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

    private val _artefacts = MutableLiveData<List<Item>>()
    private val _upgrades = MutableLiveData<List<Pair<Int?,List<Pair<Item, Int?>>>>>()
    private val _niveauPickaxe = MutableLiveData<Int?>()
    val niveauPickaxe: MutableLiveData<Int?> = _niveauPickaxe
    val upgrades : LiveData<List<Pair<Int?,List<Pair<Item, Int?>>>>> = _upgrades
    val artefacts : LiveData<List<Item>> = _artefacts


    init{
        getArtefacts(session, signature)
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
                                            Log.e(TAG, "Erreur lors de la récupération de l'item")
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
                        }else{
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
}