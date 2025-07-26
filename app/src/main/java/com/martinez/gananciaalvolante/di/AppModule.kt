package com.martinez.gananciaalvolante.di

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.android.gms.location.LocationServices
import com.martinez.gananciaalvolante.data.local.AppDatabase
import com.martinez.gananciaalvolante.data.local.dao.GananciaDao
import com.martinez.gananciaalvolante.data.local.dao.GastoDao
import com.martinez.gananciaalvolante.data.local.dao.RecorridoDao
import com.martinez.gananciaalvolante.data.local.dao.TurnoDao
import com.martinez.gananciaalvolante.data.local.dao.VehiculoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Estas dependencias vivirán mientras la app viva.
object AppModule {

    @Provides
    @Singleton // Solo queremos una instancia de la base de datos en toda la app.
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "ganancia_al_volante_db"
        ).fallbackToDestructiveMigration().build()
    }
    @Provides
    @Singleton
    fun provideSharedPreferences(app: Application): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(app)
    }
    @Provides
    @Singleton
    fun provideVehiculoDao(db: AppDatabase): VehiculoDao {
        return db.vehiculoDao()
    }

    @Provides
    @Singleton
    fun provideRecorridoDao(db: AppDatabase): RecorridoDao {
        return db.recorridoDao()
    }


    @Provides
    @Singleton
    fun provideTurnoDao(db: AppDatabase): TurnoDao {
        return db.turnoDao()
    }

    @Provides
    @Singleton
    fun provideGananciaDao(db: AppDatabase): GananciaDao {
        return db.gananciaDao()
    }
    @Provides
    @Singleton
    fun provideGastoDao(db: AppDatabase): GastoDao {
        return db.gastoDao()
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(app: Application) =
        LocationServices.getFusedLocationProviderClient(app)


    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Le dices a Room el comando SQL exacto para crear la nueva tabla
            db.execSQL("CREATE TABLE IF NOT EXISTS `gastos` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `vehiculoId` INTEGER NOT NULL, ...)")
        }
    }
}