package com.example.klachtenregistratieapp.activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.klachtenregistratieapp.R
import com.example.klachtenregistratieapp.database.AppDatabase
import com.example.klachtenregistratieapp.domain.Partij
import com.example.klachtenregistratieapp.domain.VerkoperDomein
import com.example.klachtenregistratieapp.model.VerkoperMetSuperklassePartij
import kotlin.concurrent.thread

class VerkoperToevoegenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verkoper_toevoegen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etVerkoperNaam = findViewById<EditText>(R.id.etVerkoperNaam)
        val etPartijNaam = findViewById<EditText>(R.id.etPartijNaam)
        val etPartijContact = findViewById<EditText>(R.id.etPartijContact)
        val btnOpslaanVerkoper = findViewById<Button>(R.id.btnOpslaanVerkoper)

        btnOpslaanVerkoper.setOnClickListener {
            val verkoperNaam = etVerkoperNaam.text.toString().trim()
            val partijNaam = etPartijNaam.text.toString().trim()
            val partijContact = etPartijContact.text.toString().trim()

            if (verkoperNaam.isEmpty() || partijNaam.isEmpty() || partijContact.isEmpty()) {
                Toast.makeText(
                    this,
                    "Vul alle velden in.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val partij: Partij = VerkoperDomein(
                partijID = 0,
                partijNaam = partijNaam,
                partijContact = partijContact,
                verkoperNaam = verkoperNaam
            )

            val rolTekst = partij.rolBeschrijving()

            val verkoper = VerkoperMetSuperklassePartij(
                verkoperNaam = verkoperNaam,
                partijID = 0,
                partijNaam = partijNaam,
                partijContact = partijContact
            )

            thread {
                try {
                    val db = AppDatabase.getDatabase(this)
                    db.verkoperDao().insert(verkoper)

                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Verkoper succesvol opgeslagen. $rolTekst",
                            Toast.LENGTH_LONG
                        ).show()

                        val terugNaarProduct = intent.getBooleanExtra("terugNaarProduct", false)

                        if (terugNaarProduct) {
                            finish()
                        } else {
                            etVerkoperNaam.text.clear()
                            etPartijNaam.text.clear()
                            etPartijContact.text.clear()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Opslaan mislukt: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}