package com.example.klachtenregistratieapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.klachtenregistratieapp.model.ProductAankoopBijVerkoper

@Dao
interface ProductAankoopBijVerkoperDao {

    @Insert
    fun insert(product: ProductAankoopBijVerkoper)

    @Query("SELECT * FROM product_aankoop_bij_verkoper")
    fun getAll(): List<ProductAankoopBijVerkoper>

    @Query("""
        SELECT * FROM product_aankoop_bij_verkoper
        WHERE productNaam = :productNaam
        LIMIT 1
    """)
    fun getByProductNaam(productNaam: String): ProductAankoopBijVerkoper?

    @Query("""
        UPDATE product_aankoop_bij_verkoper
        SET productNaam = :nieuweProductNaam,
            serienummer = :serienummer,
            datum = :datum,
            verkoperNaam = :verkoperNaam
        WHERE productNaam = :oudeProductNaam
    """)
    fun update(
        oudeProductNaam: String,
        nieuweProductNaam: String,
        serienummer: String,
        datum: String,
        verkoperNaam: String
    ): Int
}