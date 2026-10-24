package com.example.klachtenregistratieapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "consument_met_superklasse_partij")
data class ConsumentMetSuperklassePartij(

    @PrimaryKey
    val consumentNaam: String,

    val partijID: Int,
    val partijNaam: String,
    val partijContact: String
)