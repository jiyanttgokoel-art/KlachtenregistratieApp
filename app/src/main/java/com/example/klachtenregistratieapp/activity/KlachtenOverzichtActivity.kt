package com.example.klachtenregistratieapp.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.klachtenregistratieapp.R
import com.example.klachtenregistratieapp.database.AppDatabase
import com.example.klachtenregistratieapp.model.KlachtBijProductOpDatum
import kotlin.concurrent.thread

class KlachtenOverzichtActivity : AppCompatActivity() {

    private lateinit var listViewKlachten: ListView
    private lateinit var tvLegeStatus: TextView
    private lateinit var btnGaNaarKlachtToevoegen: Button
    private var klachtenLijst: List<KlachtBijProductOpDatum> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_klachten_overzicht)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listViewKlachten = findViewById(R.id.listViewKlachten)
        tvLegeStatus = findViewById(R.id.tvLegeStatus)
        btnGaNaarKlachtToevoegen = findViewById(R.id.btnGaNaarKlachtToevoegen)

        btnGaNaarKlachtToevoegen.setOnClickListener {
            startActivity(Intent(this, KlachtToevoegenActivity::class.java))
        }

        listViewKlachten.setOnItemClickListener { _, _, position, _ ->
            val geselecteerdeKlacht = klachtenLijst[position]

            AlertDialog.Builder(this)
                .setTitle("Kies actie")
                .setMessage("Wat wil je doen met deze klacht?")
                .setPositiveButton("Bewerken") { _, _ ->
                    val intent = Intent(this, KlachtToevoegenActivity::class.java).apply {
                        putExtra("bewerkModus", true)
                        putExtra("serienummer", geselecteerdeKlacht.serienummer)
                        putExtra("datum", geselecteerdeKlacht.datum)
                        putExtra("omschrijving", geselecteerdeKlacht.omschrijving)
                    }
                    startActivity(intent)
                }
                .setNeutralButton("Verwijderen") { _, _ ->
                    bevestigVerwijderen(geselecteerdeKlacht)
                }
                .setNegativeButton("Annuleren", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        laadKlachten()
    }

    private fun laadKlachten() {
        thread {
            try {
                val db = AppDatabase.getDatabase(this)
                klachtenLijst = db.klachtDao().getAll()

                val weergaveLijst = klachtenLijst.map {
                    "Serienummer: ${it.serienummer}\nDatum klacht: ${it.datum}\nOmschrijving: ${it.omschrijving}"
                }

                runOnUiThread {
                    val isLeeg = klachtenLijst.isEmpty()

                    tvLegeStatus.visibility = if (isLeeg) View.VISIBLE else View.GONE
                    btnGaNaarKlachtToevoegen.visibility = if (isLeeg) View.VISIBLE else View.GONE
                    listViewKlachten.visibility = if (isLeeg) View.GONE else View.VISIBLE

                    if (!isLeeg) {
                        val adapter = ArrayAdapter(
                            this,
                            android.R.layout.simple_list_item_1,
                            weergaveLijst
                        )
                        listViewKlachten.adapter = adapter
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Laden mislukt: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun bevestigVerwijderen(klacht: KlachtBijProductOpDatum) {
        AlertDialog.Builder(this)
            .setTitle("Klacht verwijderen")
            .setMessage("Weet je zeker dat je deze klacht wilt verwijderen?")
            .setPositiveButton("Ja") { _, _ ->
                verwijderKlacht(klacht)
            }
            .setNegativeButton("Nee", null)
            .show()
    }

    private fun verwijderKlacht(klacht: KlachtBijProductOpDatum) {
        thread {
            try {
                val db = AppDatabase.getDatabase(this)
                val aantalVerwijderd = db.klachtDao().deleteByCombinatie(
                    serienummer = klacht.serienummer,
                    datum = klacht.datum
                )

                runOnUiThread {
                    if (aantalVerwijderd > 0) {
                        Toast.makeText(this, "Klacht succesvol verwijderd.", Toast.LENGTH_SHORT).show()
                        laadKlachten()
                    } else {
                        Toast.makeText(this, "Geen klacht gevonden om te verwijderen.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Verwijderen mislukt: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}