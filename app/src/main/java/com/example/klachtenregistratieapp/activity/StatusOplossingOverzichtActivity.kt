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
import com.example.klachtenregistratieapp.model.KlachtStatusEnOplossing
import kotlin.concurrent.thread

class StatusOplossingOverzichtActivity : AppCompatActivity() {

    private lateinit var listViewStatus: ListView
    private lateinit var btnNieuweStatusToevoegen: Button
    private lateinit var tvLegeStatusOverzicht: TextView
    private var statusLijst: List<KlachtStatusEnOplossing> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_status_oplossing_overzicht)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listViewStatus = findViewById(R.id.listViewStatus)
        btnNieuweStatusToevoegen = findViewById(R.id.btnNieuweStatusToevoegen)
        tvLegeStatusOverzicht = findViewById(R.id.tvLegeStatusOverzicht)

        btnNieuweStatusToevoegen.setOnClickListener {
            startActivity(Intent(this, StatusOplossingActivity::class.java))
        }

        listViewStatus.setOnItemClickListener { _, _, position, _ ->
            val geselecteerdItem = statusLijst[position]

            AlertDialog.Builder(this)
                .setTitle("Kies actie")
                .setMessage("Wat wil je doen met deze status / oplossing?")
                .setPositiveButton("Bewerken") { _, _ ->
                    val intent = Intent(this, StatusOplossingActivity::class.java).apply {
                        putExtra("bewerkModus", true)
                        putExtra("serienummer", geselecteerdItem.serienummer)
                        putExtra("datum", geselecteerdItem.datum)
                        putExtra("status", geselecteerdItem.status)
                        putExtra("oplossingstype", geselecteerdItem.oplossingstype)
                    }
                    startActivity(intent)
                }
                .setNeutralButton("Verwijderen") { _, _ ->
                    bevestigVerwijderen(geselecteerdItem)
                }
                .setNegativeButton("Annuleren", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        laadStatusLijst()
    }

    private fun laadStatusLijst() {
        thread {
            try {
                val db = AppDatabase.getDatabase(this)
                statusLijst = db.statusDao().getAll()

                val weergaveLijst = statusLijst.map {
                    "Serienummer: ${it.serienummer}\nDatum klacht: ${it.datum}\nStatus: ${it.status}\nOplossingstype: ${it.oplossingstype}"
                }

                runOnUiThread {
                    val isLeeg = statusLijst.isEmpty()

                    tvLegeStatusOverzicht.visibility = if (isLeeg) View.VISIBLE else View.GONE
                    listViewStatus.visibility = if (isLeeg) View.GONE else View.VISIBLE

                    if (!isLeeg) {
                        val adapter = ArrayAdapter(
                            this,
                            android.R.layout.simple_list_item_1,
                            weergaveLijst
                        )
                        listViewStatus.adapter = adapter
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Laden mislukt: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun bevestigVerwijderen(item: KlachtStatusEnOplossing) {
        AlertDialog.Builder(this)
            .setTitle("Verwijderen")
            .setMessage("Weet je zeker dat je deze registratie wilt verwijderen?")
            .setPositiveButton("Ja") { _, _ ->
                verwijderItem(item)
            }
            .setNegativeButton("Nee", null)
            .show()
    }

    private fun verwijderItem(item: KlachtStatusEnOplossing) {
        thread {
            try {
                val db = AppDatabase.getDatabase(this)
                val aantalVerwijderd = db.statusDao().deleteByStatusEnOplossingstype(
                    status = item.status,
                    oplossingstype = item.oplossingstype
                )

                runOnUiThread {
                    if (aantalVerwijderd > 0) {
                        Toast.makeText(
                            this,
                            "Registratie verwijderd.",
                            Toast.LENGTH_SHORT
                        ).show()
                        laadStatusLijst()
                    } else {
                        Toast.makeText(
                            this,
                            "Niets verwijderd.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Verwijderen mislukt: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}