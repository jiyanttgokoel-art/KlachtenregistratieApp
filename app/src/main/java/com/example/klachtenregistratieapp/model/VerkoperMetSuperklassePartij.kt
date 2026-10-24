package com.example.klachtenregistratieapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "verkoper_met_superklasse_partij")
data class VerkoperMetSuperklassePartij(

    @PrimaryKey
    val verkoperNaam: String,

    val partijID: Int,
    val partijNaam: String,
    val partijContact: String
)