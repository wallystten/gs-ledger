package com.gsledger.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this).apply {
            text = "G.S Ledger\n\nControle financeiro automático"
            textSize = 18f
            setPadding(40, 80, 40, 40)
        }

        setContentView(tv)
    }
}

