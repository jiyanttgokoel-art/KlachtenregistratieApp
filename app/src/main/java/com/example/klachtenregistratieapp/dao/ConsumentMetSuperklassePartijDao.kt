package com.example.klachtenregistratieapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.klachtenregistratieapp.model.ConsumentMetSuperklassePartij

@Dao
interface ConsumentMetSuperklassePartijDao {

    @Insert
    fun insert(consument: ConsumentMetSuperklassePartij)

    @Query("SELECT * FROM consument_met_superklasse_partij")
    fun getAll(): List<ConsumentMetSuperklassePartij>
}