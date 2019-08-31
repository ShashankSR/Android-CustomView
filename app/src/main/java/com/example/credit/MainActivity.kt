package com.example.credit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.credit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    val creditCardViewModel = CreditCardViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityMainBinding>(
            this, R.layout.activity_main
        ).apply {
            credViewModel = creditCardViewModel
            lifecycleOwner = this@MainActivity
        }
    }
}
