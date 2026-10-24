package com.example.klachtenregistratieapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.klachtenregistratieapp.model.BewijsVanKlachtMetType

@Dao
interface BewijsVanKlachtMetTypeDao {

    @Insert
    fun insert(bewijs: BewijsVanKlachtMetType)

    @Query("SELECT * FROM bewijs_van_klacht_met_type")
    fun getAll(): List<BewijsVanKlachtMetType>

    @Query("""
        SELECT * FROM bewijs_van_klacht_met_type
        WHERE bewijstype = :bewijstype
          AND serienummer = :serienummer
          AND datum = :datum
        LIMIT 1
    """)
    fun getByCombinatie(
        bewijstype: String,
        serienummer: String,
        datum: String
    ): BewijsVanKlachtMetType?

    @Query("""
        UPDATE bewijs_van_klacht_met_type
        SET bewijsregistratie = :bewijsregistratie
        WHERE bewijstype = :bewijstype
          AND serienummer = :serienummer
          AND datum = :datum
    """)
    fun update(
        bewijstype: String,
        serienummer: String,
        datum: String,
        bewijsregistratie: String
    ): Int
}