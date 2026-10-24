package com.example.klachtenregistratieapp.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.klachtenregistratieapp.R
import com.example.klachtenregistratieapp.database.AppDatabase
import com.example.klachtenregistratieapp.model.BewijsVanKlachtMetType
import com.example.klachtenregistratieapp.model.ProductAankoopBijVerkoper
import java.io.ByteArrayOutputStream
import java.util.Calendar
import java.util.Locale
import kotlin.concurrent.thread

class BewijsToevoegenActivity : AppCompatActivity() {

    private var productenLijst: List<ProductAankoopBijVerkoper> = emptyList()
    private var huidigeFotoBitmap: Bitmap? = null

    private lateinit var tvGeenProducten: TextView
    private lateinit var btnGaNaarProduct: Button
    private lateinit var spinnerProduct: Spinner
    private lateinit var etDatum: EditText
    private lateinit var spinnerBewijstype: Spinner
    private lateinit var btnMaakFoto: Button
    private lateinit var imgBewijsPreview: ImageView
    private lateinit var btnOpslaanBewijs: Button

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                huidigeFotoBitmap = bitmap
                imgBewijsPreview.setImageBitmap(bitmap)
                imgBewijsPreview.visibility = View.VISIBLE

                Toast.makeText(
                    this,
                    "Foto succesvol gemaakt.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Geen foto ontvangen.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bewijs_toevoegen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvGeenProducten = findViewById(R.id.tvGeenProducten)
        btnGaNaarProduct = findViewById(R.id.btnGaNaarProduct)
        spinnerProduct = findViewById(R.id.spinnerProduct)
        etDatum = findViewById(R.id.etDatum)
        spinnerBewijstype = findViewById(R.id.spinnerBewijstype)
        btnMaakFoto = findViewById(R.id.btnMaakFoto)
        imgBewijsPreview = findViewById(R.id.imgBewijsPreview)
        btnOpslaanBewijs = findViewById(R.id.btnOpslaanBewijs)

        etDatum.setText(maakDatumVandaag())
        instellenDatePicker(etDatum)

        btnGaNaarProduct.setOnClickListener {
            val intent = Intent(this, ProductToevoegenActivity::class.java)
            intent.putExtra("terugNaarBewijs", true)
            startActivity(intent)
        }

        val bewijsTypeOpties = listOf("Foto defect", "Foto bon")
        val bewijsTypeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            bewijsTypeOpties
        )
        bewijsTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBewijstype.adapter = bewijsTypeAdapter

        btnMaakFoto.setOnClickListener {
            if (productenLijst.isEmpty()) {
                Toast.makeText(
                    this,
                    "Registreer eerst een product voordat je bewijs toevoegt.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                cameraLauncher.launch(null)
            }
        }

        btnOpslaanBewijs.setOnClickListener {
            val geselecteerdProduct = spinnerProduct.selectedItem?.toString() ?: ""
            val datum = etDatum.text.toString().trim()
            val bewijstype = spinnerBewijstype.selectedItem?.toString() ?: ""

            if (productenLijst.isEmpty()) {
                Toast.makeText(
                    this,
                    "Registreer eerst een product voordat je bewijs toevoegt.",
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

            if (datum.isEmpty() || bewijstype.isEmpty()) {
                Toast.makeText(
                    this,
                    "Vul alle velden in.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (huidigeFotoBitmap == null) {
                Toast.makeText(
                    this,
                    "Maak eerst een foto voor dit bewijstype.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val serienummer = geselecteerdProduct.substringBefore(" - ")
            val bewijsregistratie = bitmapNaarBase64(huidigeFotoBitmap!!)

            thread {
                try {
                    val db = AppDatabase.getDatabase(this)
                    val bestaandBewijs = db.bewijsDao().getByCombinatie(
                        bewijstype = bewijstype,
                        serienummer = serienummer,
                        datum = datum
                    )

                    if (bestaandBewijs == null) {
                        val bewijs = BewijsVanKlachtMetType(
                            bewijstype = bewijstype,
                            bewijsregistratie = bewijsregistratie,
                            serienummer = serienummer,
                            datum = datum
                        )
                        db.bewijsDao().insert(bewijs)

                        runOnUiThread {
                            Toast.makeText(
                                this,
                                "Bewijs succesvol opgeslagen.",
                                Toast.LENGTH_SHORT
                            ).show()
                            resetScherm()
                        }
                    } else {
                        db.bewijsDao().update(
                            bewijstype = bewijstype,
                            serienummer = serienummer,
                            datum = datum,
                            bewijsregistratie = bewijsregistratie
                        )

                        runOnUiThread {
                            Toast.makeText(
                                this,
                                "Bewijs voor deze combinatie bestond al en is bijgewerkt.",
                                Toast.LENGTH_LONG
                            ).show()
                            resetScherm()
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

    override fun onResume() {
        super.onResume()
        laadProductenInSpinner()
    }

    private fun resetScherm() {
        etDatum.setText(maakDatumVandaag())

        if (spinnerProduct.adapter != null && spinnerProduct.adapter.count > 0) {
            spinnerProduct.setSelection(0)
        }

        spinnerBewijstype.setSelection(0)
        huidigeFotoBitmap = null
        imgBewijsPreview.setImageDrawable(null)
        imgBewijsPreview.visibility = View.GONE
        btnMaakFoto.visibility = if (productenLijst.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun laadProductenInSpinner() {
        thread {
            val db = AppDatabase.getDatabase(this)
            productenLijst = db.productDao().getAll()

            val weergaveLijst = productenLijst.map { "${it.serienummer} - ${it.productNaam}" }

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

                btnOpslaanBewijs.isEnabled = !geenProducten
                btnMaakFoto.visibility = if (geenProducten) View.GONE else View.VISIBLE

                if (geenProducten) {
                    huidigeFotoBitmap = null
                    imgBewijsPreview.setImageDrawable(null)
                    imgBewijsPreview.visibility = View.GONE
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

    private fun maakDatumVandaag(): String {
        val kalender = Calendar.getInstance()
        return String.format(
            Locale.getDefault(),
            "%02d-%02d-%04d",
            kalender.get(Calendar.DAY_OF_MONTH),
            kalender.get(Calendar.MONTH) + 1,
            kalender.get(Calendar.YEAR)
        )
    }

    private fun bitmapNaarBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}