package com.example.klachtenregistratieapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.klachtenregistratieapp.model.KlachtBijProductOpDatum

@Dao
interface KlachtBijProductOpDatumDao {

    @Insert
    fun insert(klacht: KlachtBijProductOpDatum)

    @Query("SELECT * FROM klacht_bij_product_op_datum")
    fun getAll(): List<KlachtBijProductOpDatum>

    @Query("""
        SELECT * FROM klacht_bij_product_op_datum
        WHERE serienummer = :serienummer
        ORDER BY datum
    """)
    fun getBySerienummer(serienummer: String): List<KlachtBijProductOpDatum>

    @Query("""
        SELECT * FROM klacht_bij_product_op_datum
        WHERE serienummer = :serienummer
          AND datum = :datum
        LIMIT 1
    """)
    fun getByCombinatie(
        serienummer: String,
        datum: String
    ): KlachtBijProductOpDatum?

    @Query("""
        UPDATE klacht_bij_product_op_datum
        SET serienummer = :nieuwSerienummer,
            datum = :nieuweDatum,
            omschrijving = :omschrijving
        WHERE serienummer = :oudSerienummer
          AND datum = :oudeDatum
    """)
    fun update(
        oudSerienummer: String,
        oudeDatum: String,
        nieuwSerienummer: String,
        nieuweDatum: String,
        omschrijving: String
    ): Int

    @Query("""
        DELETE FROM klacht_bij_product_op_datum
        WHERE serienummer = :serienummer
          AND datum = :datum
    """)
    fun deleteByCombinatie(
        serienummer: String,
        datum: String
    ): Int
}