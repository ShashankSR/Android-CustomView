package com.example.credit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreditCardViewModel : ViewModel() {

    val hintText = MutableLiveData<String>()
    val inputText = MutableLiveData<String>()
    val imageSource = MutableLiveData<Int>()
    val errorText = MutableLiveData<String>()

    fun onTextChanged() {

    }

    fun isValidCard() {

    }

    fun updateCardType() {

    }
}