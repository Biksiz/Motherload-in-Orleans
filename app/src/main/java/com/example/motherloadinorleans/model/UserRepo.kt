package com.example.motherloadinorleans.model

import android.content.Context
import android.util.Log
import com.android.volley.toolbox.StringRequest
import java.net.URLEncoder
import com.android.volley.Request
import com.example.motherloadinorleans.MotherlandApplication
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.security.MessageDigest



class UserRepo  private constructor() {
    private val TAG = "UserRepo"
    private val BASE_URL = "https://test.vautard.fr/creuse_srv/"


    companion object {
        @Volatile
        private var INSTANCE: UserRepo? = null

        fun getInstance(): UserRepo {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepo().also { INSTANCE = it }
            }
        }
    }

    fun hashPassword(password: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val bytes = messageDigest.digest(password.toByteArray())
        val stringBuilder = StringBuilder()

        for (byte in bytes) {
            stringBuilder.append(String.format("%02x", byte))
        }

        return stringBuilder.toString()
    }

    private val _user = User("","","","")
    /**private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages
    private val mListe = ArrayList<Message>()
    private var mAutoIncrement: Int = 0*/

    // La méthode d'ajout de message se contente désormais d'appeler le webservice.
    fun connexion(username: String, password: String, callback: (Boolean) -> Unit){
        // On URLencode auteur et contenu du message pour s'assurer qu'ils pourront bien être passés
        // en GET au webservice (i.e. directement dnas l'url)
        val encodedUser = URLEncoder.encode(username, "UTF-8")
        val encodedPassword =hashPassword(password)
        val url = BASE_URL+"connexion.php?login=$encodedUser&passwd=$encodedPassword"

        // Requête réseau utilisant Volley : le WS est appelé et ce q'il a renvoyé est retourné sous
        // forme d'une chaîne de caractères
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response -> // la réponse retournée par le WS si succès
                try {
                    val docBF: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                    val docBuilder: DocumentBuilder = docBF.newDocumentBuilder()
                    val doc: Document = docBuilder.parse(response.byteInputStream())

                    // On vérifie le status
                    val statusNode = doc.getElementsByTagName("STATUS").item(0)
                    if (statusNode != null) {
                        val status = statusNode.textContent.trim()

                        if (status == "OK") {
                            val paramsNode = doc.getElementsByTagName("PARAMS").item(0)
                            if (paramsNode != null && paramsNode.nodeType == Node.ELEMENT_NODE) {
                                val elem = paramsNode as Element
                                val session = elem.getElementsByTagName("SESSION").item(0)?.textContent
                                val signature = elem.getElementsByTagName("SIGNATURE").item(0)?.textContent

                                if (!session.isNullOrEmpty() && !signature.isNullOrEmpty()) {
                                    _user.username = username
                                    _user.password = password
                                    _user.session = session
                                    _user.signature = signature
                                    println("status2 : $status")
                                    println("session : $session")
                                    println("signature : $signature")
                                    Log.d(TAG, "Connexion réussie !")
                                    val sharedPref = MotherlandApplication.instance.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                    with (sharedPref.edit()) {
                                        putString("username", username)
                                        putString("password", password)
                                        putString("session", session)
                                        putString("signature", signature)
                                        putString("name", username)
                                        apply()
                                    }
                                    callback(true)
                                    val storeRepo = StoreRepo.getInstance()
                                    storeRepo.recupererOffres(session, signature)
                                    storeRepo.getStatutDuJoueur(session, signature)
                                }
                            } else {
                                Log.e(TAG, "Noeud 'PARAMS' introuvable dans la réponse XML")
                                callback(false)
                            }
                        } else {
                            Log.e(TAG, "connexion : Erreur - $status")
                            callback(false)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG,"Erreur lors de la lecture de la réponse XML", e)
                    callback(false)
                }
            },
            { error ->
                Log.d(TAG,"connexion error")
                error.printStackTrace()
                callback(false)
            })

        MotherlandApplication.instance.requestQueue?.add(stringRequest)

    }


    fun reinitialize_account(session: String, signature: String, callback: (Boolean) -> Unit){
        val encodedSession = URLEncoder.encode(session, "UTF-8")
        val encodedSignature = URLEncoder.encode(signature, "UTF-8")
        val url = BASE_URL+"reinit_joueur.php?session=$encodedSession&signature=$encodedSignature"

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
                            Log.d(TAG, "Réinitialisation réussie !")
                            callback(true)
                        } else {
                            Log.e(TAG, "Réinitialisation : Erreur - $status")
                            callback(false)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG,"Erreur lors de la lecture de la réponse XML", e)
                    callback(false)
                }
            },
            { error ->
                Log.d(TAG,"Réinitialisation error")
                error.printStackTrace()
                callback(false)
            })

        MotherlandApplication.instance.requestQueue?.add(stringRequest)
    }

    fun change_username(session: String, signature: String, nom: String, callback: (Boolean) -> Unit){
        val encodedSession = URLEncoder.encode(session, "UTF-8")
        val encodedSignature = URLEncoder.encode(signature, "UTF-8")
        val encodedNom = URLEncoder.encode(nom, "UTF-8")
        val url = BASE_URL+"changenom.php?session=$encodedSession&signature=$encodedSignature&nom=$encodedNom"

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
                            Log.d(TAG, "Changement de nom réussi !")
                            callback(true)
                        } else {
                            Log.e(TAG, "changement de nom : Erreur - $status")
                            callback(false)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG,"Erreur lors de la lecture de la réponse XML", e)
                    callback(false)
                }
            },
            { error ->
                Log.d(TAG,"changement de nom error")
                error.printStackTrace()
                callback(false)
            })

        MotherlandApplication.instance.requestQueue?.add(stringRequest)
    }


}