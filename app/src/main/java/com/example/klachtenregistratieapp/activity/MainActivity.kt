package com.example.klachtenregistratieapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.klachtenregistratieapp.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnConsumentToevoegen = findViewById<Button>(R.id.btnConsumentToevoegen)
        val btnVerkoperToevoegen = findViewById<Button>(R.id.btnVerkoperToevoegen)
        val btnProductToevoegen = findViewById<Button>(R.id.btnProductToevoegen)
        val btnKlachtToevoegen = findViewById<Button>(R.id.btnKlachtToevoegen)
        val btnStatusOplossing = findViewById<Button>(R.id.btnStatusOplossing)
        val btnBewijsToevoegen = findViewById<Button>(R.id.btnBewijsToevoegen)
        val btnKlachtenOverzicht = findViewById<Button>(R.id.btnKlachtenOverzicht)

        btnConsumentToevoegen.setOnClickListener {
            startActivity(Intent(this, ConsumentToevoegenActivity::class.java))
        }

        btnVerkoperToevoegen.setOnClickListener {
            startActivity(Intent(this, VerkoperToevoegenActivity::class.java))
        }

        btnProductToevoegen.setOnClickListener {
            startActivity(Intent(this, ProductToevoegenActivity::class.java))
        }

        btnKlachtToevoegen.setOnClickListener {
            startActivity(Intent(this, KlachtToevoegenActivity::class.java))
        }

        btnStatusOplossing.setOnClickListener {
            startActivity(Intent(this, StatusOplossingOverzichtActivity::class.java))
        }

        btnBewijsToevoegen.setOnClickListener {
            startActivity(Intent(this, BewijsToevoegenActivity::class.java))
        }

        btnKlachtenOverzicht.setOnClickListener {
            startActivity(Intent(this, KlachtenOverzichtActivity::class.java))
        }
    }
}