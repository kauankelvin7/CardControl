package com.cardcontrol.app

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.cardcontrol.app.data.local.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

@RunWith(RobolectricTestRunner::class)
class RoomV4ToV5MigrationTest {

    @Test
    fun `migration 4 para 5 executa em SQLite real e preserva dados e indices`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "migration-v4-v5-test.db"
        context.deleteDatabase(databaseName)

        val callback = object : SupportSQLiteOpenHelper.Callback(4) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `merchant` TEXT NOT NULL)"
                )
                db.execSQL("CREATE TABLE `credit_cards` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")
                db.execSQL("CREATE TABLE `holders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")
                db.execSQL("INSERT INTO `transactions` (`id`, `merchant`) VALUES (1, 'Netflix.com')")
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                throw AssertionError("A fixture deve iniciar no schema v4, sem upgrade automatico")
            }
        }

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(callback)
                .build()
        )

        try {
            val db = helper.writableDatabase
            // This executes each migration statement against SQLite, rather than a proxy that
            // only records SQL strings.
            AppDatabase.MIGRATION_4_5.migrate(db)

            db.query("PRAGMA table_info(`transactions`)").use { cursor ->
                val columns = mutableSetOf<String>()
                while (cursor.moveToNext()) columns += cursor.getString(cursor.getColumnIndexOrThrow("name"))
                assertTrue("transactions deve ganhar isRecurring", "isRecurring" in columns)
            }

            db.query("SELECT id, merchant, isRecurring FROM transactions WHERE id = 1").use { cursor ->
                assertTrue("A transacao anterior deve continuar presente", cursor.moveToFirst())
                assertEquals("Netflix.com", cursor.getString(cursor.getColumnIndexOrThrow("merchant")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("isRecurring")))
            }

            db.query("SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'recurring_charges'").use { cursor ->
                assertTrue("recurring_charges deve ser criada", cursor.moveToFirst())
            }

            db.query("PRAGMA index_list('recurring_charges')").use { cursor ->
                val indices = mutableSetOf<String>()
                while (cursor.moveToNext()) indices += cursor.getString(cursor.getColumnIndexOrThrow("name"))
                assertTrue("Indice por cardId ausente", "index_recurring_charges_cardId" in indices)
                assertTrue("Indice por holderId ausente", "index_recurring_charges_holderId" in indices)
                assertTrue("Indice por merchant ausente", "index_recurring_charges_merchant" in indices)
            }

            db.query("PRAGMA foreign_key_list('recurring_charges')").use { cursor ->
                val targets = mutableSetOf<String>()
                while (cursor.moveToNext()) targets += cursor.getString(cursor.getColumnIndexOrThrow("table"))
                assertTrue("FK de credit_cards ausente", "credit_cards" in targets)
                assertTrue("FK de holders ausente", "holders" in targets)
            }
        } finally {
            helper.close()
            context.deleteDatabase(databaseName)
        }
    }
}
