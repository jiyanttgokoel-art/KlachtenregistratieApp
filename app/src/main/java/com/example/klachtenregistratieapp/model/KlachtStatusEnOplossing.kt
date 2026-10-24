package com.example.klachtenregistratieapp.model

import androidx.room.Entity

@Entity(
    tableName = "klacht_status_en_oplossing",
    primaryKeys = ["status", "oplossingstype"]
)
data class KlachtStatusEnOplossing(

    val serienummer: String,
    val datum: String,
    val status: String,
    val oplossingstype: String
)