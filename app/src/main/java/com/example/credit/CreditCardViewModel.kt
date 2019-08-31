package com.example.credit

import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.regex.Pattern

class CreditCardViewModel(val resources: Resources) : ViewModel(),
    CredCardTextView.CreditCardInterface {

    val hintText = MutableLiveData<String>().apply { value = "XXXXXXXXXXXXXXXX" }
    val inputText = MutableLiveData<String>()
    val imageSource = MutableLiveData<Int>().apply { value = R.drawable.ic_credit_card }
    val errorText = MutableLiveData<String>()

    override fun onTextChanged(input: String): Unit {
        when (CardType.detect(input)) {
            CardType.VISA -> {
                imageSource.value = R.drawable.ic_visa
            }
            CardType.MASTERCARD -> {
                imageSource.value = R.drawable.ic_mastercard
            }
            CardType.AMERICAN_EXPRESS -> {
                imageSource.value = R.drawable.ic_american_express
            }
            CardType.DINERS_CLUB -> {
                imageSource.value = R.drawable.ic_dinners_club
            }
            CardType.DISCOVER -> {
                imageSource.value = R.drawable.ic_discover
            }
            CardType.JCB -> {
                imageSource.value = R.drawable.ic_jcb
            }
            CardType.UNKNOWN -> {
                imageSource.value = R.drawable.ic_credit_card
            }
        }
        if (input.length == 16) {
            errorText.value =
                if (!isValidCard(input)) {
                    imageSource.value = R.drawable.ic_credit_card
                    resources.getString(R.string.card_not_found)
                } else {
                    ""
                }
        } else if (errorText.value?.isEmpty() == false) {
            errorText.value = ""
        }
        handleInputText(input)
    }

    private fun handleInputText(inputString: String) {
        if (inputString.length <= 16) {
            inputText.value = formatInputString(inputString)
            hintText.value = formatHintString(inputString.length)
        }
    }

    private fun formatInputString(inputString: String): String =
        StringBuilder().apply {
            for (i in 0 until inputString.length) {
                append(inputString[i])
            }
        }.toString()

    private fun formatHintString(inputStringLength: Int) =
        StringBuilder().apply {
            for (i in 0 until 16) {
                if (i >= inputStringLength) {
                    append('X')
                } else {
                    append(' ')
                }
            }
        }.toString()


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

    // Regex referred from https://stackoverflow.com/questions/72768/how-do-you-detect-credit-card-type-based-on-number
    enum class CardType {

        UNKNOWN,
        VISA("^4[0-9]{6,}\$"),
        MASTERCARD("^5[1-5][0-9]{5,}|222[1-9][0-9]{3,}|22[3-9][0-9]{4,}|2[3-6][0-9]{5,}|27[01][0-9]{4,}|2720[0-9]{3,}\$"),
        AMERICAN_EXPRESS("^3[47][0-9]{0,}\$"),
        DINERS_CLUB("^3(?:0[0-5]|[68][0-9])[0-9]{4,}\$"),
        DISCOVER("^6(?:011|5[0-9]{2})[0-9]{3,}\$"),
        JCB("^(?:2131|1800|35[0-9]{3})[0-9]{3,}\$");

        private var pattern: Pattern? = null

        constructor() {
            this.pattern = null
        }

        constructor(pattern: String) {
            this.pattern = Pattern.compile(pattern)
        }

        companion object {

            fun detect(cardNumber: String): CardType {

                for (cardType in values()) {
                    if (null == cardType.pattern) continue
                    if (cardType.pattern!!.matcher(cardNumber).matches()) return cardType
                }

                return UNKNOWN
            }
        }

    }
}