package com.example.motherloadinorleans.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class StoreRepo private constructor() {
    private val TAG = "StoreRepo"
    private val BASE_URL = "https://test.vautard.fr/creuse_srv/"
    private val userRepo = UserRepo.getInstance()
    private val _offers = MutableLiveData<List<Offer>>()
    val offers: LiveData<List<Offer>> = _offers

    init {
        recupererOffres()
    }

    companion object {
        @Volatile
        private var INSTANCE: StoreRepo? = null

        fun getInstance(): StoreRepo {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoreRepo().also { INSTANCE = it }
            }
        }
    }

    private fun recupererOffres() {
        // à completer
    }

    private fun acheterItem(offerId: String) {
        // à completer
    }

    private fun miseAJourAcheter(response: String) {
        // à completer
    }

}