package com.example.klachtenregistratieapp.model

import androidx.room.Entity

// Samengestelde primary key zodat een klacht uniek is per
// combinatie van serienummer en datum.
// Hierdoor kunnen meerdere producten op dezelfde datum een klacht hebben.
@Entity(
    tableName = "klacht_bij_product_op_datum",
    primaryKeys = ["serienummer", "datum"]
)
data class KlachtBijProductOpDatum(
    val datum: String,
    val serienummer: String,
    val omschrijving: String
)
//eerst had ik het volgende: De afwijkingen zijn dus bewust :)
//@PrimaryKey
//val datum: String,
//met deze code voorheen net zoals bij BewijsVanKlachtMetType moet ik afwijken van UML, omdat de database nu denkt er mag maar 1 klacht per datum bestaan en zorgt voor issues in de app:
//klacht op 07-04-2026 voor product A = oké
//nog een klacht op 07-04-2026 voor product B = fout