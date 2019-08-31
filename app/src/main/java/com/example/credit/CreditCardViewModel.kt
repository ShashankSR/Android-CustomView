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

    fun isValidCard(cardNumber: String): Boolean {
        var sum = 0
        var alternate = false
        for (i in cardNumber.length - 1 downTo 0) {
            var n = Integer.parseInt(cardNumber.substring(i, i + 1))
            if (alternate) {
                n *= 2
                if (n > 9) {
                    n = n % 10 + 1
                }
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    fun updateCardType() {

    }
}