package com.example.klachtenregistratieapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.klachtenregistratieapp.model.VerkoperMetSuperklassePartij

@Dao
interface VerkoperMetSuperklassePartijDao {

    @Insert
    fun insert(verkoper: VerkoperMetSuperklassePartij)

    @Query("SELECT * FROM verkoper_met_superklasse_partij")
    fun getAll(): List<VerkoperMetSuperklassePartij>
}