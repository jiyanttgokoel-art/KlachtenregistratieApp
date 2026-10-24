package com.example.klachtenregistratieapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "product_aankoop_bij_verkoper")
data class ProductAankoopBijVerkoper(

    @PrimaryKey
    val productNaam: String,

    val serienummer: String,
    val datum: String,
    val verkoperNaam: String
)