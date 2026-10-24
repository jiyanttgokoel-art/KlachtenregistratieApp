package com.example.klachtenregistratieapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.klachtenregistratieapp.dao.BewijsVanKlachtMetTypeDao
import com.example.klachtenregistratieapp.dao.ConsumentMetSuperklassePartijDao
import com.example.klachtenregistratieapp.dao.KlachtBijProductOpDatumDao
import com.example.klachtenregistratieapp.dao.KlachtStatusEnOplossingDao
import com.example.klachtenregistratieapp.dao.ProductAankoopBijVerkoperDao
import com.example.klachtenregistratieapp.dao.VerkoperMetSuperklassePartijDao
import com.example.klachtenregistratieapp.model.BewijsVanKlachtMetType
import com.example.klachtenregistratieapp.model.ConsumentMetSuperklassePartij
import com.example.klachtenregistratieapp.model.KlachtBijProductOpDatum
import com.example.klachtenregistratieapp.model.KlachtStatusEnOplossing
import com.example.klachtenregistratieapp.model.ProductAankoopBijVerkoper
import com.example.klachtenregistratieapp.model.VerkoperMetSuperklassePartij

@Database(
    entities = [
        ConsumentMetSuperklassePartij::class,
        VerkoperMetSuperklassePartij::class,
        ProductAankoopBijVerkoper::class,
        KlachtBijProductOpDatum::class,
        KlachtStatusEnOplossing::class,
        BewijsVanKlachtMetType::class
    ],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun consumentDao(): ConsumentMetSuperklassePartijDao
    abstract fun verkoperDao(): VerkoperMetSuperklassePartijDao
    abstract fun productDao(): ProductAankoopBijVerkoperDao
    abstract fun klachtDao(): KlachtBijProductOpDatumDao
    abstract fun statusDao(): KlachtStatusEnOplossingDao
    abstract fun bewijsDao(): BewijsVanKlachtMetTypeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "klachtenregistratieapp_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}