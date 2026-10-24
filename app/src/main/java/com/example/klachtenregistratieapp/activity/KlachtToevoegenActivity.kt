package com.example.klachtenregistratieapp.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.klachtenregistratieapp.R
import com.example.klachtenregistratieapp.database.AppDatabase
import com.example.klachtenregistratieapp.model.KlachtBijProductOpDatum
import com.example.klachtenregistratieapp.model.ProductAankoopBijVerkoper
import java.util.Calendar
import java.util.Locale
import kotlin.concurrent.thread

class KlachtToevoegenActivity : AppCompatActivity() {

    private var origineleDatum: String? = null
    private var origineelSerienummer: String? = null
    private var productenLijst: List<ProductAankoopBijVerkoper> = emptyList()

    private lateinit var tvTitelKlacht: TextView
    private lateinit var tvGeenProducten: TextView
    private lateinit var btnGaNaarProduct: Button
    private lateinit var spinnerProduct: Spinner
    private lateinit var etDatum: EditText
    private lateinit var etOmschrijving: EditText
    private lateinit var btnOpslaanKlacht: Button
    private lateinit var btnBijwerkenKlacht: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_klacht_toevoegen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvTitelKlacht = findViewById(R.id.tvTitelKlacht)
        tvGeenProducten = findViewById(R.id.tvGeenProducten)
        btnGaNaarProduct = findViewById(R.id.btnGaNaarProduct)
        spinnerProduct = findViewById(R.id.spinnerProduct)
        etDatum = findViewById(R.id.etDatum)
        etOmschrijving = findViewById(R.id.etOmschrijving)
        btnOpslaanKlacht = findViewById(R.id.btnOpslaanKlacht)
        btnBijwerkenKlacht = findViewById(R.id.btnBijwerkenKlacht)

        instellenDatePicker(etDatum)

        btnGaNaarProduct.setOnClickListener {
            val intent = Intent(this, ProductToevoegenActivity::class.java)
            intent.putExtra("terugNaarKlacht", true)
            startActivity(intent)
        }

        val bewerkModus = intent.getBooleanExtra("bewerkModus", false)
        val terugNaarStatus = intent.getBooleanExtra("terugNaarStatus", false)

        if (bewerkModus) {
            origineleDatum = intent.getStringExtra("datum")
            origineelSerienummer = intent.getStringExtra("serienummer")

            val serienummer = intent.getStringExtra("serienummer") ?: ""
            val datum = intent.getStringExtra("datum") ?: ""
            val omschrijving = intent.getStringExtra("omschrijving") ?: ""

            tvTitelKlacht.text = "Klacht bewerken"
            etDatum.setText(datum)
            etOmschrijving.setText(omschrijving)

            btnOpslaanKlacht.visibility = View.GONE
            btnBijwerkenKlacht.visibility = View.VISIBLE

            laadProductenInSpinner(geselecteerdSerienummer = serienummer)
        } else {
            tvTitelKlacht.text = "Klacht registreren"
            btnOpslaanKlacht.visibility = View.VISIBLE
            btnBijwerkenKlacht.visibility = View.GONE

            val kalender = Calendar.getInstance()
            val datumVandaag = String.format(
                Locale.getDefault(),
                "%02d-%02d-%04d",
                kalender.get(Calendar.DAY_OF_MONTH),
                kalender.get(Calendar.MONTH) + 1,
                kalender.get(Calendar.YEAR)
            )
            etDatum.setText(datumVandaag)

            laadProductenInSpinner(geselecteerdSerienummer = null)
        }

        btnOpslaanKlacht.setOnClickListener {
            val geselecteerdProduct = spinnerProduct.selectedItem?.toString() ?: ""
            val datum = etDatum.text.toString().trim()
            val omschrijving = etOmschrijving.text.toString().trim()

            if (productenLijst.isEmpty()) {
                Toast.makeText(
                    this,
                    "Registreer eerst een product voordat je een klacht toevoegt.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (geselecteerdProduct.startsWith("Geen producten")) {
                Toast.makeText(
                    this,
                    "Kies eerst een geldig product.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (datum.isEmpty() || omschrijving.isEmpty()) {
                Toast.makeText(this, "Vul alle velden in.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val serienummer = geselecteerdProduct.substringBefore(" - ")

            thread {
                try {
                    val db = AppDatabase.getDatabase(this)
                    val bestaandeKlacht = db.klachtDao().getByCombinatie(
                        serienummer = serienummer,
                        datum = datum
                    )

                    if (bestaandeKlacht != null) {
                        runOnUiThread {
                            Toast.makeText(
                                this,
                                "Voor dit product bestaat al een klacht op deze datum.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        val klacht = KlachtBijProductOpDatum(
                            datum = datum,
                            serienummer = serienummer,
                            omschrijving = omschrijving
                        )

                        db.klachtDao().insert(klacht)

                        runOnUiThread {
                            Toast.makeText(
                                this,
                                "Klacht succesvol opgeslagen.",
                                Toast.LENGTH_SHORT
                            ).show()

                            if (terugNaarStatus) {
                                finish()
                            } else {
                                etOmschrijving.text.clear()

                                val kalender = Calendar.getInstance()
                                val datumVandaag = String.format(
                                    Locale.getDefault(),
                                    "%02d-%02d-%04d",
                                    kalender.get(Calendar.DAY_OF_MONTH),
                                    kalender.get(Calendar.MONTH) + 1,
                                    kalender.get(Calendar.YEAR)
                                )
                                etDatum.setText(datumVandaag)

                                if (spinnerProduct.adapter.count > 0) {
                                    spinnerProduct.setSelection(0)
                                }
                            }
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

        btnBijwerkenKlacht.setOnClickListener {
            val geselecteerdProduct = spinnerProduct.selectedItem?.toString() ?: ""
            val nieuweDatum = etDatum.text.toString().trim()
            val omschrijving = etOmschrijving.text.toString().trim()
            val oudeDatum = origineleDatum
            val oudSerienummer = origineelSerienummer

            if (productenLijst.isEmpty()) {
                Toast.makeText(this, "Geen producten beschikbaar.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (geselecteerdProduct.startsWith("Geen producten")) {
                Toast.makeText(this, "Kies eerst een geldig product.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nieuweDatum.isEmpty() || omschrijving.isEmpty() || oudeDatum.isNullOrEmpty() || oudSerienummer.isNullOrEmpty()) {
                Toast.makeText(this, "Bijwerken mislukt: onvolledige gegevens.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nieuwSerienummer = geselecteerdProduct.substringBefore(" - ")

            thread {
                try {
                    val db = AppDatabase.getDatabase(this)

                    val combinatieBestaatAl = db.klachtDao().getByCombinatie(
                        serienummer = nieuwSerienummer,
                        datum = nieuweDatum
                    )

                    val isZelfdeRecord = oudSerienummer == nieuwSerienummer && oudeDatum == nieuweDatum

                    if (combinatieBestaatAl != null && !isZelfdeRecord) {
                        runOnUiThread {
                            Toast.makeText(
                                this,
                                "Er bestaat al een klacht voor dit product op deze datum.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        val aantalAangepast = db.klachtDao().update(
                            oudSerienummer = oudSerienummer,
                            oudeDatum = oudeDatum,
                            nieuwSerienummer = nieuwSerienummer,
                            nieuweDatum = nieuweDatum,
                            omschrijving = omschrijving
                        )

                        runOnUiThread {
                            if (aantalAangepast > 0) {
                                Toast.makeText(this, "Klacht succesvol bijgewerkt.", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, "Geen klacht gevonden om bij te werken.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this, "Bijwerken mislukt: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val geselecteerdSerienummer = if (intent.getBooleanExtra("bewerkModus", false)) {
            origineelSerienummer
        } else {
            null
        }

        laadProductenInSpinner(geselecteerdSerienummer)
    }

    private fun laadProductenInSpinner(geselecteerdSerienummer: String?) {
        thread {
            val db = AppDatabase.getDatabase(this)
            productenLijst = db.productDao().getAll()

            val weergaveLijst = if (productenLijst.isEmpty()) {
                listOf("Geen producten beschikbaar")
            } else {
                productenLijst.map { "${it.serienummer} - ${it.productNaam}" }
            }

            runOnUiThread {
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    weergaveLijst
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerProduct.adapter = adapter

                val geenProducten = productenLijst.isEmpty()
                tvGeenProducten.visibility = if (geenProducten) View.VISIBLE else View.GONE
                spinnerProduct.visibility = if (geenProducten) View.GONE else View.VISIBLE

                btnGaNaarProduct.visibility = View.VISIBLE

                if (btnOpslaanKlacht.visibility == View.VISIBLE) {
                    btnOpslaanKlacht.isEnabled = !geenProducten
                }

                if (btnBijwerkenKlacht.visibility == View.VISIBLE) {
                    btnBijwerkenKlacht.isEnabled = !geenProducten
                }

                if (!geselecteerdSerienummer.isNullOrEmpty()) {
                    val index = productenLijst.indexOfFirst { it.serienummer == geselecteerdSerienummer }
                    if (index >= 0) {
                        spinnerProduct.setSelection(index)
                    }
                }
            }
        }
    }

    private fun instellenDatePicker(editText: EditText) {
        editText.setOnClickListener {
            val kalender = Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(
                this,
                { _, jaar, maand, dag ->
                    val datumTekst = String.format(
                        Locale.getDefault(),
                        "%02d-%02d-%04d",
                        dag,
                        maand + 1,
                        jaar
                    )
                    editText.setText(datumTekst)
                },
                kalender.get(Calendar.YEAR),
                kalender.get(Calendar.MONTH),
                kalender.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.show()
        }
    }
}