package com.example.klachtenregistratieapp.model

import androidx.room.Entity

@Entity(
    tableName = "bewijs_van_klacht_met_type",
    primaryKeys = ["bewijstype", "serienummer", "datum"]
// Er is bewust gekozen voor een samengestelde primary key:
// (bewijstype, serienummer, datum).
// In plaats van alleen bewijstype als sleutel, omdat anders
// slechts één bewijs per type mogelijk zou zijn in de hele tabel.
// Deze combinatie zorgt ervoor dat bewijs uniek is per product en datum,
// wat beter aansluit bij de gebruikersflow en analyse van de applicatie.
)
data class BewijsVanKlachtMetType(
    val bewijstype: String,
    val bewijsregistratie: String,
    val serienummer: String,
    val datum: String
)