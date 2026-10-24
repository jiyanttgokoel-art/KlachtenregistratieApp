package com.example.klachtenregistratieapp.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
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
import com.example.klachtenregistratieapp.model.KlachtStatusEnOplossing
import com.example.klachtenregistratieapp.model.ProductAankoopBijVerkoper
import kotlin.concurrent.thread

class StatusOplossingActivity : AppCompatActivity() {

    private var oudeStatus: String? = null
    private var oudOplossingstype: String? = null
    private var origineelSerienummer: String? = null
    private var origineleDatum: String? = null

    private var productenLijst: List<ProductAankoopBijVerkoper> = emptyList()
    private var klachtenVanGekozenProduct: List<KlachtBijProductOpDatum> = emptyList()

    private lateinit var tvTitelStatus: TextView
    private lateinit var tvGeenProducten: TextView
    private lateinit var tvGeenKlachten: TextView
    private lateinit var btnGaNaarKlacht: Button
    private lateinit var spinnerProduct: Spinner
    private lateinit var spinnerDatumKlacht: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var spinnerOplossing: Spinner
    private lateinit var btnOpslaan: Button
    private lateinit var btnBijwerken: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_status_oplossing)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvTitelStatus = findViewById(R.id.tvTitelStatus)
        tvGeenProducten = findViewById(R.id.tvGeenProducten)
        tvGeenKlachten = findViewById(R.id.tvGeenKlachten)
        btnGaNaarKlacht = findViewById(R.id.btnGaNaarKlacht)
        spinnerProduct = findViewById(R.id.spinnerProduct)
        spinnerDatumKlacht = findViewById(R.id.spinnerDatumKlacht)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        spinnerOplossing = findViewById(R.id.spinnerOplossing)
        btnOpslaan = findViewById(R.id.btnOpslaanStatus)
        btnBijwerken = findViewById(R.id.btnBijwerkenStatus)

        btnGaNaarKlacht.setOnClickListener {
            val intent = Intent(this, KlachtToevoegenActivity::class.java)
            intent.putExtra("terugNaarStatus", true)
            startActivity(intent)
        }

        val statusOpties = listOf("Open", "In behandeling", "Afgehandeld")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOpties)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter

        val oplossingOpties = listOf("Geen keuze", "Herstel", "Vervanging", "Geld terug")
        val oplossingAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, oplossingOpties)
        oplossingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerOplossing.adapter = oplossingAdapter

        val bewerkModus = intent.getBooleanExtra("bewerkModus", false)

        if (bewerkModus) {
            tvTitelStatus.text = "Status / oplossing bewerken"
            oudeStatus = intent.getStringExtra("status")
            oudOplossingstype = intent.getStringExtra("oplossingstype")
            origineelSerienummer = intent.getStringExtra("serienummer")
            origineleDatum = intent.getStringExtra("datum")

            btnOpslaan.visibility = View.GONE
            btnBijwerken.visibility = View.VISIBLE
        } else {
            tvTitelStatus.text = "Status / oplossing"
            btnOpslaan.visibility = View.VISIBLE
            btnBijwerken.visibility = View.GONE
        }

        spinnerProduct.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (productenLijst.isEmpty()) {
                        toonLegeKlachtenState()
                        return
                    }

                    if (position < 0 || position >= productenLijst.size) {
                        toonLegeKlachtenState()
                        return
                    }

                    val gekozenProduct = productenLijst[position]
                    laadKlachtenVoorProduct(
                        serienummer = gekozenProduct.serienummer,
                        gewensteDatum = if (gekozenProduct.serienummer == origineelSerienummer) origineleDatum else null
                    )
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                }
            }

        btnOpslaan.setOnClickListener {
            val geselecteerdProduct = spinnerProduct.selectedItem?.toString() ?: ""
            val geselecteerdProductPositie = spinnerProduct.selectedItemPosition
            val geselecteerdeDatumPositie = spinnerDatumKlacht.selectedItemPosition
            val status = spinnerStatus.selectedItem?.toString() ?: ""
            val oplossing = spinnerOplossing.selectedItem?.toString() ?: ""

            if (productenLijst.isEmpty()) {
                Toast.makeText(
                    this,
                    "Er zijn nog geen producten beschikbaar.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (klachtenVanGekozenProduct.isEmpty()) {
                Toast.makeText(
                    this,
                    "Voor dit product zijn nog geen klachten geregistreerd.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (!geselecteerdProduct.contains(" - ")) {
                Toast.makeText(
                    this,
                    "Kon product niet correct uitlezen.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (geselecteerdProductPositie < 0 || geselecteerdProductPositie >= productenLijst.size ||
                geselecteerdeDatumPositie < 0 || geselecteerdeDatumPositie >= klachtenVanGekozenProduct.size
            ) {
                Toast.makeText(
                    this,
                    "Kies eerst een geldig product en een geldige klacht-datum.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val gekozenProduct = productenLijst[geselecteerdProductPositie]
            val gekozenKlacht = klachtenVanGekozenProduct[geselecteerdeDatumPositie]

            val nieuw = KlachtStatusEnOplossing(
                serienummer = gekozenProduct.serienummer,
                datum = gekozenKlacht.datum,
                status = status,
                oplossingstype = oplossing
            )

            thread {
                try {
                    val db = AppDatabase.getDatabase(this)

                    val bestaatAl = db.statusDao().getByStatusEnOplossing(
                        status = status,
                        oplossingstype = oplossing
                    )

                    if (bestaatAl != null) {
                        runOnUiThread {
                            Toast.makeText(
                                this,
                                "Deze status / oplossing bestaat al. Bewerk deze via het overzicht.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        db.statusDao().insert(nieuw)

                        runOnUiThread {
                            Toast.makeText(
                                this,
                                "Status / oplossing succesvol opgeslagen.",
                                Toast.LENGTH_SHORT
                            ).show()

                            spinnerStatus.setSelection(0)
                            spinnerOplossing.setSelection(0)

                            if (spinnerDatumKlacht.adapter != null && spinnerDatumKlacht.adapter.count > 0) {
                                spinnerDatumKlacht.setSelection(0)
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

        btnBijwerken.setOnClickListener {
            val geselecteerdProduct = spinnerProduct.selectedItem?.toString() ?: ""
            val geselecteerdProductPositie = spinnerProduct.selectedItemPosition
            val geselecteerdeDatumPositie = spinnerDatumKlacht.selectedItemPosition
            val status = spinnerStatus.selectedItem?.toString() ?: ""
            val oplossing = spinnerOplossing.selectedItem?.toString() ?: ""

            if (productenLijst.isEmpty()) {
                Toast.makeText(this, "Geen producten beschikbaar.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (klachtenVanGekozenProduct.isEmpty()) {
                Toast.makeText(this, "Geen klachten beschikbaar voor dit product.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!geselecteerdProduct.contains(" - ")) {
                Toast.makeText(
                    this,
                    "Kon product niet correct uitlezen.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (oudeStatus.isNullOrEmpty() || oudOplossingstype.isNullOrEmpty()) {
                Toast.makeText(this, "Bijwerken mislukt: oude gegevens ontbreken.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (geselecteerdProductPositie < 0 || geselecteerdProductPositie >= productenLijst.size ||
                geselecteerdeDatumPositie < 0 || geselecteerdeDatumPositie >= klachtenVanGekozenProduct.size
            ) {
                Toast.makeText(
                    this,
                    "Kies eerst een geldig product en een geldige klacht-datum.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val gekozenProduct = productenLijst[geselecteerdProductPositie]
            val gekozenKlacht = klachtenVanGekozenProduct[geselecteerdeDatumPositie]

            thread {
                try {
                    val db = AppDatabase.getDatabase(this)
                    val aantalAangepast = db.statusDao().update(
                        oudeStatus = oudeStatus!!,
                        oudOplossingstype = oudOplossingstype!!,
                        nieuwSerienummer = gekozenProduct.serienummer,
                        nieuweDatum = gekozenKlacht.datum,
                        nieuweStatus = status,
                        nieuwOplossingstype = oplossing
                    )

                    runOnUiThread {
                        if (aantalAangepast > 0) {
                            Toast.makeText(
                                this,
                                "Status / oplossing succesvol bijgewerkt.",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Geen registratie gevonden om bij te werken.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Bijwerken mislukt: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        laadProducten()
    }

    private fun laadProducten() {
        thread {
            val db = AppDatabase.getDatabase(this)
            productenLijst = db.productDao().getAll()

            val productWeergaveLijst = if (productenLijst.isEmpty()) {
                emptyList()
            } else {
                productenLijst.map { "${it.serienummer} - ${it.productNaam}" }
            }

            runOnUiThread {
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    productWeergaveLijst
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerProduct.adapter = adapter

                val geenProducten = productenLijst.isEmpty()
                tvGeenProducten.visibility = if (geenProducten) View.VISIBLE else View.GONE
                spinnerProduct.visibility = if (geenProducten) View.GONE else View.VISIBLE

                if (geenProducten) {
                    toonLegeKlachtenState()
                    btnOpslaan.isEnabled = false
                    btnBijwerken.isEnabled = false
                    btnGaNaarKlacht.visibility = View.GONE
                } else {
                    val index = if (!origineelSerienummer.isNullOrEmpty()) {
                        productenLijst.indexOfFirst { it.serienummer == origineelSerienummer }
                    } else {
                        0
                    }

                    val geldigeIndex = if (index >= 0) index else 0
                    spinnerProduct.setSelection(geldigeIndex)
                }
            }
        }
    }

    private fun laadKlachtenVoorProduct(serienummer: String, gewensteDatum: String?) {
        thread {
            val db = AppDatabase.getDatabase(this)
            klachtenVanGekozenProduct = db.klachtDao().getBySerienummer(serienummer)

            val datumLijst = klachtenVanGekozenProduct.map { it.datum }

            runOnUiThread {
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    datumLijst
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerDatumKlacht.adapter = adapter

                val geenKlachten = klachtenVanGekozenProduct.isEmpty()
                tvGeenKlachten.visibility = if (geenKlachten) View.VISIBLE else View.GONE
                spinnerDatumKlacht.visibility = if (geenKlachten) View.GONE else View.VISIBLE
                btnGaNaarKlacht.visibility = if (geenKlachten) View.VISIBLE else View.GONE

                btnOpslaan.isEnabled = !geenKlachten
                btnBijwerken.isEnabled = !geenKlachten

                if (!geenKlachten && !gewensteDatum.isNullOrEmpty()) {
                    val index = klachtenVanGekozenProduct.indexOfFirst { it.datum == gewensteDatum }
                    if (index >= 0) {
                        spinnerDatumKlacht.setSelection(index)
                    }
                }
            }
        }
    }

    private fun toonLegeKlachtenState() {
        klachtenVanGekozenProduct = emptyList()

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            emptyList<String>()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDatumKlacht.adapter = adapter

        tvGeenKlachten.visibility = View.GONE
        spinnerDatumKlacht.visibility = View.GONE
    }
}