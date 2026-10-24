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
import com.example.klachtenregistratieapp.model.ProductAankoopBijVerkoper
import com.example.klachtenregistratieapp.model.VerkoperMetSuperklassePartij
import java.util.Calendar
import java.util.Locale
import kotlin.concurrent.thread

class ProductToevoegenActivity : AppCompatActivity() {

    private var origineleProductNaam: String? = null
    private var verkopersLijst: List<VerkoperMetSuperklassePartij> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product_toevoegen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvTitelProduct = findViewById<TextView>(R.id.tvTitelProduct)
        val tvGeenVerkopers = findViewById<TextView>(R.id.tvGeenVerkopers)
        val btnGaNaarVerkoper = findViewById<Button>(R.id.btnGaNaarVerkoper)

        val etSerienummer = findViewById<EditText>(R.id.etSerienummer)
        val etProductNaam = findViewById<EditText>(R.id.etProductNaam)
        val etDatum = findViewById<EditText>(R.id.etDatum)
        val spinnerVerkoper = findViewById<Spinner>(R.id.spinnerVerkoper)

        val btnOpslaanProduct = findViewById<Button>(R.id.btnOpslaanProduct)
        val btnBijwerkenProduct = findViewById<Button>(R.id.btnBijwerkenProduct)

        instellenDatePicker(etDatum)

        btnGaNaarVerkoper.setOnClickListener {
            val intent = Intent(this, VerkoperToevoegenActivity::class.java)
            intent.putExtra("terugNaarProduct", true)
            startActivity(intent)
        }

        val bewerkModus = intent.getBooleanExtra("bewerkModus", false)

        if (bewerkModus) {
            origineleProductNaam = intent.getStringExtra("productNaam")
            val serienummer = intent.getStringExtra("serienummer") ?: ""
            val productNaam = intent.getStringExtra("productNaam") ?: ""
            val datum = intent.getStringExtra("datum") ?: ""
            val verkoperNaam = intent.getStringExtra("verkoperNaam") ?: ""

            tvTitelProduct.text = "Product bewerken"
            etSerienummer.setText(serienummer)
            etProductNaam.setText(productNaam)
            etDatum.setText(datum)

            btnOpslaanProduct.visibility = View.GONE
            btnBijwerkenProduct.visibility = View.VISIBLE

            laadVerkopersInSpinner(
                spinnerVerkoper = spinnerVerkoper,
                tvGeenVerkopers = tvGeenVerkopers,
                btnGaNaarVerkoper = btnGaNaarVerkoper,
                btnOpslaanProduct = btnOpslaanProduct,
                btnBijwerkenProduct = btnBijwerkenProduct,
                geselecteerdeVerkoperNaam = verkoperNaam
            )
        } else {
            tvTitelProduct.text = "Product registreren"
            btnOpslaanProduct.visibility = View.VISIBLE
            btnBijwerkenProduct.visibility = View.GONE

            laadVerkopersInSpinner(
                spinnerVerkoper = spinnerVerkoper,
                tvGeenVerkopers = tvGeenVerkopers,
                btnGaNaarVerkoper = btnGaNaarVerkoper,
                btnOpslaanProduct = btnOpslaanProduct,
                btnBijwerkenProduct = btnBijwerkenProduct,
                geselecteerdeVerkoperNaam = null
            )
        }

        btnOpslaanProduct.setOnClickListener {
            val serienummer = etSerienummer.text.toString().trim()
            val productNaam = etProductNaam.text.toString().trim()
            val datum = etDatum.text.toString().trim()
            val geselecteerdeVerkoper = spinnerVerkoper.selectedItem?.toString() ?: ""

            if (verkopersLijst.isEmpty()) {
                Toast.makeText(
                    this,
                    "Registreer eerst een verkoper voordat je een product toevoegt.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (geselecteerdeVerkoper.startsWith("Geen verkopers")) {
                Toast.makeText(
                    this,
                    "Kies eerst een geldige verkoper.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (serienummer.isEmpty() || productNaam.isEmpty() || datum.isEmpty() || geselecteerdeVerkoper.isEmpty()) {
                Toast.makeText(this, "Vul alle velden in.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val verkoperNaam = geselecteerdeVerkoper.substringBefore(" - ")

            val product = ProductAankoopBijVerkoper(
                productNaam = productNaam,
                serienummer = serienummer,
                datum = datum,
                verkoperNaam = verkoperNaam
            )

            thread {
                try {
                    val db = AppDatabase.getDatabase(this)
                    db.productDao().insert(product)

                    runOnUiThread {
                        Toast.makeText(this, "Product succesvol opgeslagen.", Toast.LENGTH_SHORT).show()

                        val terugNaarKlacht = intent.getBooleanExtra("terugNaarKlacht", false)
                        val terugNaarBewijs = intent.getBooleanExtra("terugNaarBewijs", false)

                        if (terugNaarKlacht || terugNaarBewijs) {
                            finish()
                        } else {
                            etSerienummer.text.clear()
                            etProductNaam.text.clear()

                            val kalender = Calendar.getInstance()
                            val datumVandaag = String.format(
                                Locale.getDefault(),
                                "%02d-%02d-%04d",
                                kalender.get(Calendar.DAY_OF_MONTH),
                                kalender.get(Calendar.MONTH) + 1,
                                kalender.get(Calendar.YEAR)
                            )
                            etDatum.setText(datumVandaag)

                            if (spinnerVerkoper.adapter.count > 0) {
                                spinnerVerkoper.setSelection(0)
                            }
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this, "Opslaan mislukt: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        btnBijwerkenProduct.setOnClickListener {
            val serienummer = etSerienummer.text.toString().trim()
            val nieuweProductNaam = etProductNaam.text.toString().trim()
            val datum = etDatum.text.toString().trim()
            val geselecteerdeVerkoper = spinnerVerkoper.selectedItem?.toString() ?: ""
            val oudeProductNaam = origineleProductNaam

            if (verkopersLijst.isEmpty()) {
                Toast.makeText(this, "Geen verkopers beschikbaar.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (geselecteerdeVerkoper.startsWith("Geen verkopers")) {
                Toast.makeText(this, "Kies eerst een geldige verkoper.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (serienummer.isEmpty() || nieuweProductNaam.isEmpty() || datum.isEmpty() || geselecteerdeVerkoper.isEmpty() || oudeProductNaam.isNullOrEmpty()) {
                Toast.makeText(this, "Bijwerken mislukt: onvolledige gegevens.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val verkoperNaam = geselecteerdeVerkoper.substringBefore(" - ")

            thread {
                try {
                    val db = AppDatabase.getDatabase(this)
                    val aantalAangepast = db.productDao().update(
                        oudeProductNaam = oudeProductNaam,
                        nieuweProductNaam = nieuweProductNaam,
                        serienummer = serienummer,
                        datum = datum,
                        verkoperNaam = verkoperNaam
                    )

                    runOnUiThread {
                        if (aantalAangepast > 0) {
                            Toast.makeText(this, "Product succesvol bijgewerkt.", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Geen product gevonden om bij te werken.", Toast.LENGTH_LONG).show()
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

        val spinnerVerkoper = findViewById<Spinner>(R.id.spinnerVerkoper)
        val tvGeenVerkopers = findViewById<TextView>(R.id.tvGeenVerkopers)
        val btnGaNaarVerkoper = findViewById<Button>(R.id.btnGaNaarVerkoper)
        val btnOpslaanProduct = findViewById<Button>(R.id.btnOpslaanProduct)
        val btnBijwerkenProduct = findViewById<Button>(R.id.btnBijwerkenProduct)

        laadVerkopersInSpinner(
            spinnerVerkoper = spinnerVerkoper,
            tvGeenVerkopers = tvGeenVerkopers,
            btnGaNaarVerkoper = btnGaNaarVerkoper,
            btnOpslaanProduct = btnOpslaanProduct,
            btnBijwerkenProduct = btnBijwerkenProduct,
            geselecteerdeVerkoperNaam = null
        )
    }

    private fun laadVerkopersInSpinner(
        spinnerVerkoper: Spinner,
        tvGeenVerkopers: TextView,
        btnGaNaarVerkoper: Button,
        btnOpslaanProduct: Button,
        btnBijwerkenProduct: Button,
        geselecteerdeVerkoperNaam: String?
    ) {
        thread {
            val db = AppDatabase.getDatabase(this)
            verkopersLijst = db.verkoperDao().getAll()

            val weergaveLijst = if (verkopersLijst.isEmpty()) {
                listOf("Geen verkopers beschikbaar")
            } else {
                verkopersLijst.map { "${it.verkoperNaam} - ${it.partijNaam}" }
            }

            runOnUiThread {
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    weergaveLijst
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerVerkoper.adapter = adapter

                val geenVerkopers = verkopersLijst.isEmpty()
                tvGeenVerkopers.visibility = if (geenVerkopers) View.VISIBLE else View.GONE
                spinnerVerkoper.visibility = if (geenVerkopers) View.GONE else View.VISIBLE

                btnGaNaarVerkoper.visibility = View.VISIBLE

                if (btnOpslaanProduct.visibility == View.VISIBLE) {
                    btnOpslaanProduct.isEnabled = !geenVerkopers
                }
                if (btnBijwerkenProduct.visibility == View.VISIBLE) {
                    btnBijwerkenProduct.isEnabled = !geenVerkopers
                }

                if (!geselecteerdeVerkoperNaam.isNullOrEmpty()) {
                    val index = verkopersLijst.indexOfFirst { it.verkoperNaam == geselecteerdeVerkoperNaam }
                    if (index >= 0) {
                        spinnerVerkoper.setSelection(index)
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