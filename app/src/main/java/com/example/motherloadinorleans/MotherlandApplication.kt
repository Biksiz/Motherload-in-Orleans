package com.example.motherloadinorleans

import android.app.Application
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class MotherlandApplication: Application() {
    companion object {
        // L'accessseur statique. Ça ressemble à la manière dont on fait un singleton en kotlin,
        // mais notez que instance n'est pas settée ici.
        lateinit var instance: MotherlandApplication
            private set
    }


    var requestQueue: RequestQueue? = null
        private set

    override fun onCreate() {
        super.onCreate()
        // L'unique instance de chatM1Application a été créé (et on est dedans :-) ). On peut donc
        // renseigner l'attribut instance.
        instance = this

        // POur Volley : on créé une unique requestQueue qui durera tant que l'application durera.
        // Ça évite d'en recréer une à chaque fois.
        requestQueue = Volley.newRequestQueue(this)
    }
}