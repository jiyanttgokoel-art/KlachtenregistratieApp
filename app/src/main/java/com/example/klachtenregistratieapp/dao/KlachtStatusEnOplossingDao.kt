package com.example.klachtenregistratieapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.klachtenregistratieapp.model.KlachtStatusEnOplossing

@Dao
interface KlachtStatusEnOplossingDao {

    @Insert
    fun insert(statusOplossing: KlachtStatusEnOplossing)

    @Query("SELECT * FROM klacht_status_en_oplossing")
    fun getAll(): List<KlachtStatusEnOplossing>

    @Query("""
        UPDATE klacht_status_en_oplossing
        SET serienummer = :nieuwSerienummer,
            datum = :nieuweDatum,
            status = :nieuweStatus,
            oplossingstype = :nieuwOplossingstype
        WHERE status = :oudeStatus
          AND oplossingstype = :oudOplossingstype
    """)
    fun update(
        oudeStatus: String,
        oudOplossingstype: String,
        nieuwSerienummer: String,
        nieuweDatum: String,
        nieuweStatus: String,
        nieuwOplossingstype: String
    ): Int

    @Query("""
        DELETE FROM klacht_status_en_oplossing
        WHERE status = :status
          AND oplossingstype = :oplossingstype
    """)
    fun deleteByStatusEnOplossingstype(
        status: String,
        oplossingstype: String
    ): Int

    @Query("""
    SELECT * FROM klacht_status_en_oplossing
    WHERE status = :status AND oplossingstype = :oplossingstype
    LIMIT 1
""")
    fun getByStatusEnOplossing(
        status: String,
        oplossingstype: String
    ): KlachtStatusEnOplossing?
}