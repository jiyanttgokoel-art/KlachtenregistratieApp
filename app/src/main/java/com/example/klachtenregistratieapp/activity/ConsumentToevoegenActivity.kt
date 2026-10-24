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
import com.example.klachtenregistratieapp.domain.ConsumentDomein
import com.example.klachtenregistratieapp.domain.Partij
import com.example.klachtenregistratieapp.model.ConsumentMetSuperklassePartij
import kotlin.concurrent.thread

class ConsumentToevoegenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_consument_toevoegen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etConsumentNaam = findViewById<EditText>(R.id.etConsumentNaam)
        val etPartijNaam = findViewById<EditText>(R.id.etPartijNaam)
        val etPartijContact = findViewById<EditText>(R.id.etPartijContact)
        val btnOpslaanConsument = findViewById<Button>(R.id.btnOpslaanConsument)

        btnOpslaanConsument.setOnClickListener {
            val consumentNaam = etConsumentNaam.text.toString().trim()
            val partijNaam = etPartijNaam.text.toString().trim()
            val partijContact = etPartijContact.text.toString().trim()

            if (consumentNaam.isEmpty() || partijNaam.isEmpty() || partijContact.isEmpty()) {
                Toast.makeText(
                    this,
                    "Vul alle velden in.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Blok 2 / OOP-laag gebruiken
            val partij: Partij = ConsumentDomein(
                partijID = 0,
                partijNaam = partijNaam,
                partijContact = partijContact,
                consumentNaam = consumentNaam
            )

            val rolTekst = partij.rolBeschrijving()

            val consument = ConsumentMetSuperklassePartij(
                consumentNaam = consumentNaam,
                partijID = 0,
                partijNaam = partijNaam,
                partijContact = partijContact
            )

            thread {
                try {
                    val db = AppDatabase.getDatabase(this)
                    db.consumentDao().insert(consument)

                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Consument succesvol opgeslagen. $rolTekst",
                            Toast.LENGTH_LONG
                        ).show()

                        etConsumentNaam.text.clear()
                        etPartijNaam.text.clear()
                        etPartijContact.text.clear()
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