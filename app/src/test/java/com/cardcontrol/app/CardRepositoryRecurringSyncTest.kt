package com.cardcontrol.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.cardcontrol.app.data.local.AppDatabase
import com.cardcontrol.app.data.local.CreditCardEntity
import com.cardcontrol.app.data.local.HolderEntity
import com.cardcontrol.app.data.local.RecurringChargeEntity
import com.cardcontrol.app.data.local.TransactionEntity
import com.cardcontrol.app.data.repository.CardRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class CardRepositoryRecurringSyncTest {

    @Test
    fun `sync nao duplica recorrencia em variacoes de caixa do mesmo comercio`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        try {
            val dao = database.appDao()
            val holderId = dao.insertHolder(HolderEntity(name = "Titular teste", whatsapp = ""))
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
            val now = System.currentTimeMillis()
            dao.insertTransaction(
                TransactionEntity(
                    cardId = cardId,
                    holderId = holderId,
                    merchant = "NETFLIX.COM",
                    totalAmount = 5_590L,
                    purchaseTimestamp = now - TimeUnit.DAYS.toMillis(30)
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    cardId = cardId,
                    holderId = holderId,
                    merchant = "Netflix.com",
                    totalAmount = 5_590L,
                    purchaseTimestamp = now
                )
            )
            val existingId = dao.insertRecurringCharge(
                RecurringChargeEntity(
                    cardId = cardId,
                    holderId = holderId,
                    merchant = "netflix.com",
                    expectedAmount = 5_590L,
                    lastAmount = 5_590L,
                    lastChargedTimestamp = now - TimeUnit.DAYS.toMillis(30)
                )
            )

            val repository = CardRepository(dao)
            repository.syncRecurringCharges()
            repository.syncRecurringCharges()

            val persisted = dao.getAllRecurringChargesSnapshot()
            assertEquals("Reexecucoes nao podem duplicar o mesmo comercio/cartao", 1, persisted.size)
            assertEquals(existingId, persisted.single().id)
            assertEquals("netflix.com", persisted.single().merchant)
            assertEquals(now, persisted.single().lastChargedTimestamp)
        } finally {
            database.close()
        }
    }
}
