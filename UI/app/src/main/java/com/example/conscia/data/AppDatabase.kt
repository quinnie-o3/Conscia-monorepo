package com.example.conscia.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.conscia.data.rule.RuleDao
import com.example.conscia.data.rule.RuleEntity

@Database(entities = [RuleEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ruleDao(): RuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "conscia_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE rules ADD COLUMN extensionMinutes INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE rules ADD COLUMN extensionCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE rules ADD COLUMN lastExtensionDate TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
