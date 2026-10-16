package com.cardcontrol.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.cardcontrol.app.data.local.AppDatabase
import com.cardcontrol.app.data.local.CreditCardEntity
import com.cardcontrol.app.data.local.HolderEntity
import com.cardcontrol.app.data.local.TransactionEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecurringTransactionGuardTest {

    @Test
    fun `bloqueia recorrencia manual em compra parcelada e permite compra unica`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        try {
            val dao = database.appDao()
            val holderId = dao.insertHolder(
                HolderEntity(name = "Titular teste", whatsapp = "")
            )
            val cardId = dao.insertCard(
                CreditCardEntity(
                    name = "Cartao teste",
                    lastFourDigits = "1234",
                    totalLimit = 100_000L,
                    closingDay = 10,
                    dueDay = 17,
                    defaultHolderId = holderId
                )
            )
            val installmentId = dao.insertTransaction(
                TransactionEntity(
                    cardId = cardId,
                    holderId = holderId,
                    merchant = "Compra parcelada",
                    totalAmount = 120_000L,
                    installmentsCount = 12
                )
            )
            val singleChargeId = dao.insertTransaction(
                TransactionEntity(
                    cardId = cardId,
                    holderId = holderId,
                    merchant = "Assinatura",
                    totalAmount = 5_990L,
                    installmentsCount = 1
                )
            )

            assertEquals(0, dao.setTransactionRecurring(installmentId, true))
            assertFalse(dao.getAllTransactionsSnapshot().single { it.id == installmentId }.isRecurring)

            assertEquals(1, dao.setTransactionRecurring(singleChargeId, true))
            assertTrue(dao.getAllTransactionsSnapshot().single { it.id == singleChargeId }.isRecurring)
        } finally {
            database.close()
        }
    }
}
